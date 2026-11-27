
package pl.pykmcitems.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.FluidCollisionMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SwiatecznaKatana implements Listener {

    private static final double TELEPORT_DISTANCE = 5.0;
    private static final double SWEEP_RADIUS = 2.5;
    private static final double SWEEP_DAMAGE_PERCENT = 0.08;
    private static final int FREEZE_TICKS = 40;

    private final JavaPlugin plugin;
    private final NamespacedKey katanaKey;
    private final ItemCooldownManager cooldownManager;

    private final Map<UUID, BukkitRunnable> activeFreezes = new HashMap<>();
    private final Set<UUID> sweepProcessing = new HashSet<>();

    public SwiatecznaKatana(JavaPlugin plugin) {
        this.plugin = plugin;
        this.katanaKey = new NamespacedKey(plugin, "katana");
        this.cooldownManager = ((Main) plugin).getItemCooldownManager();
    }

    private boolean isKatana(ItemStack item) {
        if (item == null
                || item.getType() != Material.NETHERITE_SWORD
                || !item.hasItemMeta()) {
            return false;
        }

        return item.getItemMeta()
                .getPersistentDataContainer()
                .has(katanaKey, PersistentDataType.BYTE);
    }

    private boolean hasKatana(Player player) {
        return isKatana(player.getInventory().getItemInMainHand())
                || isKatana(player.getInventory().getItemInOffHand());
    }

    // SHIFT + PRAWY KLIK - MAINHAND I OFFHAND
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {

        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();

        if (!player.isSneaking()) {
            return;
        }

        EquipmentSlot hand = event.getHand();

        if (hand == null) {
            return;
        }

        ItemStack usedItem;

        if (hand == EquipmentSlot.HAND) {
            usedItem = player.getInventory().getItemInMainHand();
        } else {
            usedItem = player.getInventory().getItemInOffHand();
        }

        if (!isKatana(usedItem)) {
            return;
        }

        if (cooldownManager.isOnCooldown(player, "katana")) {
            return;
        }

        event.setCancelled(true);

        World world = player.getWorld();

        Location eyeLocation = player.getEyeLocation();
        Vector direction = eyeLocation.getDirection().normalize();

        TeleportResult teleportResult = findTeleportDestination(
                player,
                direction
        );

        double travelDistance = teleportResult.distance;
        Location destination = teleportResult.destination;

        destination.setYaw(player.getLocation().getYaw());
        destination.setPitch(player.getLocation().getPitch());

        showTeleportTrail(
                world,
                eyeLocation,
                direction,
                travelDistance,
                player
        );

        if (!player.teleport(destination)) {
            return;
        }

        if (!cooldownManager.tryStartCooldown(
                player,
                "katana",
                "&a&lŚwiąteczna Katana",
                40_000L
        )) {
            return;
        }

        for (int i = 0; i < 3; i++) {
            final int delay = i * 3;

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    player.playSound(
                            player.getLocation(),
                            Sound.ENTITY_PLAYER_ATTACK_SWEEP,
                            1.0f,
                            1.0f
                    );
                }
            }, delay);
        }

        player.sendActionBar(Component.text(
                "Użyłeś Świątecznej Katany!",
                NamedTextColor.GREEN
        ));
    }

    private static class TeleportResult {

        private final Location destination;
        private final double distance;

        private TeleportResult(Location destination, double distance) {
            this.destination = destination;
            this.distance = distance;
        }
    }

    // Oblicza, o ile trzeba podnieść gracza, aby ominąć kolidujące bloki.
    private double getRequiredRaise(World world, BoundingBox box) {

        double requiredRaise = 0.0;

        int minX = (int) Math.floor(box.getMinX());
        int minY = (int) Math.floor(box.getMinY());
        int minZ = (int) Math.floor(box.getMinZ());

        int maxX = (int) Math.floor(box.getMaxX());
        int maxY = (int) Math.floor(box.getMaxY());
        int maxZ = (int) Math.floor(box.getMaxZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {

                    Block block = world.getBlockAt(x, y, z);

                    if (!block.getType().isSolid()) {
                        continue;
                    }

                    BoundingBox blockBox = block.getBoundingBox();

                    if (!blockBox.overlaps(box)) {
                        continue;
                    }

                    double raise = blockBox.getMaxY()
                            - box.getMinY()
                            + 0.02;

                    requiredRaise = Math.max(
                            requiredRaise,
                            raise
                    );
                }
            }
        }

        return requiredRaise;
    }

    // Górna powierzchnia bloku = teleport na blok.
    // Boczna lub dolna powierzchnia = teleport przed blok.
    private TeleportResult findTeleportDestination(
            Player player,
            Vector direction
    ) {
        Location originalLocation = player.getLocation().clone();
        Location eyeLocation = player.getEyeLocation().clone();
        World world = player.getWorld();

        Vector normalizedDirection = direction.clone().normalize();

        org.bukkit.util.RayTraceResult rayTrace = world.rayTraceBlocks(
                eyeLocation,
                normalizedDirection,
                TELEPORT_DISTANCE,
                FluidCollisionMode.NEVER,
                true
        );

        // Trafienie w górną powierzchnię bloku:
        // próbujemy przenieść gracza na środek górnej powierzchni.
        if (rayTrace != null
                && rayTrace.getHitBlock() != null
                && rayTrace.getHitBlockFace() == BlockFace.UP) {

            Block hitBlock = rayTrace.getHitBlock();

            Location topDestination = new Location(
                    world,
                    hitBlock.getX() + 0.5,
                    hitBlock.getY() + 1.01,
                    hitBlock.getZ() + 0.5,
                    originalLocation.getYaw(),
                    originalLocation.getPitch()
            );

            Vector movement = topDestination.toVector()
                    .subtract(originalLocation.toVector());

            BoundingBox destinationBox = player.getBoundingBox()
                    .clone()
                    .shift(movement);

            if (isSafeDestination(world, destinationBox)) {
                double trailDistance = eyeLocation.toVector()
                        .distance(rayTrace.getHitPosition());

                return new TeleportResult(topDestination, trailDistance);
            }
        }

        /*
         * Trafienie w bok/spód (albo brak poprawnego miejsca na górze):
         * teleportujemy poziomo, zachowując aktualne Y gracza.
         *
         * Przesuwanie całego hitboxa w dół powodowało kolizję z podłożem,
         * szczególnie gdy celownik był kilka pikseli pod górną krawędzią bloku.
         */
        Vector horizontalDirection = new Vector(
                normalizedDirection.getX(),
                0.0,
                normalizedDirection.getZ()
        );

        if (horizontalDirection.lengthSquared() >= 1.0E-8) {
            horizontalDirection.normalize();
        }

        double maxDistance = TELEPORT_DISTANCE;

        if (rayTrace != null && rayTrace.getHitPosition() != null) {
            Vector toHit = rayTrace.getHitPosition().clone()
                    .subtract(eyeLocation.toVector());

            // Odległość pozioma do punktu trafienia.
            maxDistance = Math.max(
                    0.0,
                    Math.min(
                            TELEPORT_DISTANCE,
                            toHit.getX() * horizontalDirection.getX()
                                    + toHit.getZ() * horizontalDirection.getZ()
                    )
            );
        }

        // Cofamy się od punktu trafienia, aż cały hitbox będzie bezpieczny.
        // Wysokość gracza pozostaje niezmieniona.
        double step = 0.025;

        for (double distance = maxDistance;
             distance >= -0.0001;
             distance -= step) {

            double checkedDistance = Math.max(0.0, distance);

            Location candidate = originalLocation.clone()
                    .add(horizontalDirection.clone().multiply(checkedDistance));

            Vector movement = candidate.toVector()
                    .subtract(originalLocation.toVector());

            BoundingBox movedBox = player.getBoundingBox()
                    .clone()
                    .shift(movement);

            if (isSafeDestination(world, movedBox)) {
                return new TeleportResult(candidate, checkedDistance);
            }
        }

        return new TeleportResult(originalLocation, 0.0);
    }

    // Sprawdzanie rzeczywistych hitboxów bloków.
    private boolean isSafeDestination(World world, BoundingBox box) {

        int minX = (int) Math.floor(box.getMinX());
        int minY = (int) Math.floor(box.getMinY());
        int minZ = (int) Math.floor(box.getMinZ());

        int maxX = (int) Math.floor(box.getMaxX());
        int maxY = (int) Math.floor(box.getMaxY());
        int maxZ = (int) Math.floor(box.getMaxZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {

                    Block block = world.getBlockAt(x, y, z);

                    if (!block.getType().isSolid()) {
                        continue;
                    }

                    BoundingBox blockBox = block.getBoundingBox();

                    if (blockBox.overlaps(box)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private double getWeaponDamage(Player player) {

        AttributeInstance attribute = player.getAttribute(
                Attribute.ATTACK_DAMAGE
        );

        return attribute == null
                ? 1.0
                : Math.max(1.0, attribute.getValue());
    }

    // Śnieżne particles, zamrażanie i obrażenia graczy na trasie.
    private void showTeleportTrail(
            World world,
            Location start,
            Vector direction,
            double distance,
            Player user
    ) {

        Set<UUID> playersOnPath = new HashSet<>();

        double step = 0.25;

        for (double current = 0.0;
             current <= distance;
             current += step) {

            Location point = start.clone().add(
                    direction.clone().multiply(current)
            );

            world.spawnParticle(
                    Particle.SNOWFLAKE,
                    point,
                    3,
                    0.15,
                    0.15,
                    0.15,
                    0.01
            );

            for (Entity entity : world.getNearbyEntities(
                    point,
                    0.8,
                    1.0,
                    0.8
            )) {

                if (!(entity instanceof Player target)) {
                    continue;
                }

                if (target.equals(user)) {
                    continue;
                }

                playersOnPath.add(target.getUniqueId());
            }
        }

        double abilityDamage = getWeaponDamage(user);

        for (UUID uuid : playersOnPath) {

            Player target = Bukkit.getPlayer(uuid);

            if (target == null || !target.isOnline() || target.isDead()) {
                continue;
            }

            zamrozGracza(target);
            target.damage(abilityDamage, user);
        }
    }

    // Dodatkowy sweep po normalnym uderzeniu.
    @EventHandler
    public void onKatanaHit(EntityDamageByEntityEvent event) {

        if (!(event.getDamager() instanceof Player attacker)) {
            return;
        }

        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        if (!isKatana(attacker.getInventory().getItemInMainHand())) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            return;
        }

        UUID attackerUUID = attacker.getUniqueId();

        if (sweepProcessing.contains(attackerUUID)) {
            return;
        }

        double originalDamage = event.getDamage();
        double sweepDamage = originalDamage * SWEEP_DAMAGE_PERCENT;

        if (sweepDamage <= 0.0) {
            return;
        }

        Location center = victim.getLocation().clone().add(0, 1.0, 0);

        victim.getWorld().spawnParticle(
                Particle.SWEEP_ATTACK,
                center,
                1,
                0.3,
                0.2,
                0.3,
                0.0
        );

        victim.getWorld().playSound(
                victim.getLocation(),
                Sound.ENTITY_PLAYER_ATTACK_SWEEP,
                0.8f,
                1.2f
        );

        // Dodatkowe 8% w ramach tego samego uderzenia.
        event.setDamage(originalDamage + sweepDamage);

        sweepProcessing.add(attackerUUID);

        try {

            for (Entity entity : victim.getNearbyEntities(
                    SWEEP_RADIUS,
                    SWEEP_RADIUS,
                    SWEEP_RADIUS
            )) {

                if (!(entity instanceof Player target)) {
                    continue;
                }

                if (target.equals(attacker) || target.equals(victim)) {
                    continue;
                }

                if (target.getLocation().distanceSquared(
                        victim.getLocation()
                ) > SWEEP_RADIUS * SWEEP_RADIUS) {
                    continue;
                }

                target.damage(sweepDamage, attacker);
            }

        } finally {
            sweepProcessing.remove(attackerUUID);
        }
    }

    // Zamrażanie gracza przez 2 sekundy.
    private void zamrozGracza(Player target) {

        UUID uuid = target.getUniqueId();

        BukkitRunnable previous = activeFreezes.remove(uuid);

        if (previous != null) {
            previous.cancel();
        }

        Location frozenLocation = target.getLocation().clone();

        BukkitRunnable freeze = new BukkitRunnable() {

            int ticks = 0;

            @Override
            public void run() {

                if (!target.isOnline() || target.isDead()) {
                    activeFreezes.remove(uuid);
                    cancel();
                    return;
                }

                if (ticks >= FREEZE_TICKS) {
                    activeFreezes.remove(uuid);
                    cancel();
                    return;
                }

                target.teleport(frozenLocation);
                target.setVelocity(new Vector(0, 0, 0));

                ticks++;
            }
        };

        activeFreezes.put(uuid, freeze);
        freeze.runTaskTimer(plugin, 0L, 1L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        UUID uuid = event.getPlayer().getUniqueId();

        BukkitRunnable freeze = activeFreezes.remove(uuid);

        if (freeze != null) {
            freeze.cancel();
        }

        sweepProcessing.remove(uuid);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {

        UUID uuid = event.getEntity().getUniqueId();

        BukkitRunnable freeze = activeFreezes.remove(uuid);

        if (freeze != null) {
            freeze.cancel();
        }
    }
}
