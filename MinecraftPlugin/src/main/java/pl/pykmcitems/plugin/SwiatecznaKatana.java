
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
import org.bukkit.block.Block;
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
    private static final int FREEZE_TICKS = 60;

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

    // SHIFT + PRAWY KLIK
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {

        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();

        if (!player.isSneaking() || !hasKatana(player)) {
            return;
        }

        event.setCancelled(true);

        if (cooldownManager.isOnCooldown(player, "katana")) {
            player.sendActionBar(Component.text(
                    "Świąteczna Katana jest na cooldownie!",
                    NamedTextColor.RED
            ));
            return;
        }

        World world = player.getWorld();

        Location eyeLocation = player.getEyeLocation();
        Vector direction = eyeLocation.getDirection().normalize();

        double travelDistance = findTravelDistance(
                world,
                eyeLocation,
                direction
        );

        Location destination = player.getLocation().clone()
                .add(direction.clone().multiply(travelDistance));

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

        // Trzy dźwięki sweep.
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

    // Wyszukiwanie pełnego bloku na trasie.
    private double findTravelDistance(
            World world,
            Location start,
            Vector direction
    ) {

        double step = 0.05;

        for (double distance = step;
             distance <= TELEPORT_DISTANCE;
             distance += step) {

            Location point = start.clone().add(
                    direction.clone().multiply(distance)
            );

            Block block = point.getBlock();

            if (isFullSolidBlock(block)) {
                return Math.max(0.0, distance - 1.0);
            }
        }

        return TELEPORT_DISTANCE;
    }

    // Tylko pełne bloki zatrzymują teleportację.
    private boolean isFullSolidBlock(Block block) {

        if (block == null || !block.getType().isSolid()) {
            return false;
        }

        BoundingBox box = block.getBoundingBox();

        return box.getWidthX() >= 0.99
                && box.getHeight() >= 0.99
                && box.getWidthZ() >= 0.99;
    }

    // Śnieżne particles i zamrażanie graczy na trasie.
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

        for (UUID uuid : playersOnPath) {
            Player target = Bukkit.getPlayer(uuid);

            if (target != null && target.isOnline() && !target.isDead()) {
                zamrozGracza(target);
            }
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

        double sweepDamage = event.getDamage() * SWEEP_DAMAGE_PERCENT;

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

        sweepProcessing.add(attackerUUID);

        try {
            // Dodatkowe obrażenia dla pierwotnego celu.
            if (victim.isOnline() && !victim.isDead()) {
                victim.damage(sweepDamage, attacker);
            }

            // Dodatkowe obrażenia dla pozostałych graczy.
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

    // Zamrażanie gracza.
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
