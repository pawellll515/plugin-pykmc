
package pl.pykmcitems.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SwiatecznaKatana implements Listener {

    private final JavaPlugin plugin;
    private final NamespacedKey katanaKey;

    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, BukkitRunnable> activeFreezes = new HashMap<>();

    public SwiatecznaKatana(JavaPlugin plugin) {
        this.plugin = plugin;
        this.katanaKey = new NamespacedKey(plugin, "katana");
    }

    private boolean isKatana(ItemStack item) {
        if (item == null || item.getType() != Material.NETHERITE_SWORD
                || !item.hasItemMeta()) {
            return false;
        }

        return item.getItemMeta().getPersistentDataContainer()
                .has(katanaKey, PersistentDataType.BYTE);
    }

    private boolean hasKatana(Player player) {
        return isKatana(player.getInventory().getItemInMainHand())
                || isKatana(player.getInventory().getItemInOffHand());
    }

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

        RayTraceResult result = player.getWorld().rayTraceEntities(
                player.getEyeLocation(),
                player.getEyeLocation().getDirection(),
                10.0,
                0.5,
                entity -> entity instanceof Player && entity != player
        );

        if (result == null || !(result.getHitEntity() instanceof Player target)) {
            return;
        }

        event.setCancelled(true);

        UUID uuid = player.getUniqueId();

        long now = System.currentTimeMillis();
        long cooldownEnd = cooldowns.getOrDefault(uuid, 0L);

        if (cooldownEnd > now) {
            player.sendActionBar(Component.text(
                    "Świąteczna Katana jest na cooldownie!",
                    NamedTextColor.RED
            ));
            return;
        }

        Location destination = getSafeTeleportLocation(player, target);

        if (destination == null) {
            player.sendActionBar(Component.text(
                    "Nie ma bezpiecznego miejsca do teleportacji!",
                    NamedTextColor.RED
            ));
            return;
        }

        if (!player.teleport(destination)) {
            return;
        }

        cooldowns.put(uuid, now + 90_000L);
        player.setCooldown(Material.NETHERITE_SWORD, 1800);

        if (target.isOnline() && !target.isDead()) {
            target.damage(8.0, player);
        }

        player.sendActionBar(Component.text(
                "Użyłeś Świątecznej Katany!",
                NamedTextColor.GREEN
        ));

        target.sendActionBar(Component.text(
                "Zostałeś trafiony Świąteczną Kataną!",
                NamedTextColor.RED
        ));

        zamrozGracza(target);
    }

    private Location getBehindLocation(Player target, double distance) {

        Location location = target.getLocation().clone();

        double yaw = Math.toRadians(location.getYaw());

        double x = Math.sin(yaw) * distance;
        double z = -Math.cos(yaw) * distance;

        location.add(x, 0, z);

        return location;
    }

    private Location getSafeTeleportLocation(Player player, Player target) {

        Location targetLocation = target.getLocation();
        World world = target.getWorld();

        Vector backwards = new Vector(
                Math.sin(Math.toRadians(targetLocation.getYaw())),
                0,
                -Math.cos(Math.toRadians(targetLocation.getYaw()))
        );

        // Szukamy miejsca od 2 bloków do 0 bloków za przeciwnikiem.
        for (double distance = 2.0; distance >= 0.0; distance -= 0.25) {

            Location candidate = getBehindLocation(target, distance);

            candidate.setYaw(player.getLocation().getYaw());
            candidate.setPitch(player.getLocation().getPitch());

            if (!isSafeLocation(player, candidate)) {
                continue;
            }

            // Sprawdzamy, czy droga od gracza do miejsca docelowego
            // nie przechodzi przez pełne bloki.
            if (!hasClearPath(player, candidate)) {
                continue;
            }

            return candidate;
        }

        return null;
    }

    private boolean hasClearPath(Player player, Location destination) {

        World world = player.getWorld();

        Location start = player.getLocation().clone().add(0, 1, 0);
        Location end = destination.clone().add(0, 1, 0);

        Vector direction = end.toVector().subtract(start.toVector());
        double distance = direction.length();

        if (distance <= 0.1) {
            return true;
        }

        RayTraceResult result = world.rayTraceBlocks(
                start,
                direction.normalize(),
                distance,
                FluidCollisionMode.NEVER,
                true
        );

        return result == null || result.getHitBlock() == null;
    }

    private boolean isSafeLocation(Player player, Location location) {

        World world = location.getWorld();

        if (world == null) {
            return false;
        }

        // Sprawdzamy przestrzeń na wysokości nóg, tułowia i głowy.
        Location feet = location.clone();
        Location body = location.clone().add(0, 0.9, 0);
        Location head = location.clone().add(0, 1.6, 0);

        if (!isPassableForPlayer(world, feet)
                || !isPassableForPlayer(world, body)
                || !isPassableForPlayer(world, head)) {
            return false;
        }

        // Sprawdzamy, czy pod graczem znajduje się podłoże.
        Location ground = location.clone().subtract(0, 0.1, 0);

        if (ground.getBlock().isPassable()) {
            return false;
        }

        // Nie teleportujemy gracza w innego gracza.
        for (Entity entity : world.getNearbyEntities(
                location, 0.3, 0.9, 0.3)) {

            if (entity instanceof Player && entity != player) {
                return false;
            }
        }

        return true;
    }

    private boolean isPassableForPlayer(World world, Location location) {

        Material material = location.getBlock().getType();

        // Bloki bez kolizji, np. trawa i kwiaty, nie blokują.
        if (location.getBlock().isPassable()) {
            return true;
        }

        return false;
    }

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

                if (ticks >= 60) {
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

        cooldowns.remove(uuid);

        BukkitRunnable freeze = activeFreezes.remove(uuid);

        if (freeze != null) {
            freeze.cancel();
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {

        UUID uuid = event.getEntity().getUniqueId();

        cooldowns.remove(uuid);

        BukkitRunnable freeze = activeFreezes.remove(uuid);

        if (freeze != null) {
            freeze.cancel();
        }
    }
}
