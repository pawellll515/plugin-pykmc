
package pl.pykmcitems.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
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

    private final Map<UUID, BukkitRunnable> activeDashes = new HashMap<>();

    public SwiatecznaKatana(JavaPlugin plugin) {
        this.plugin = plugin;
        this.katanaKey = new NamespacedKey(plugin, "katana");
    }

    private boolean isKatana(ItemStack item) {
        if (item == null || item.getType() != Material.NETHERITE_SWORD) {
            return false;
        }

        if (!item.hasItemMeta()) {
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

        if (!player.isSneaking()) {
            return;
        }

        if (!hasKatana(player)) {
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

        if (activeDashes.containsKey(uuid)) {
            return;
        }

        long now = System.currentTimeMillis();
        long cooldownEnd = cooldowns.getOrDefault(uuid, 0L);

        if (cooldownEnd > now) {
            player.sendActionBar(Component.text(
                    "Świąteczna Katana jest na cooldownie!",
                    NamedTextColor.RED
            ));
            return;
        }

        cooldowns.put(uuid, now + 90_000L);
        player.setCooldown(Material.NETHERITE_SWORD, 1800);

        // Obrażenia przypisane do gracza używającego Katany.
        target.damage(8.0, player);

        player.sendActionBar(Component.text(
                "Użyłeś Świątecznej Katany!",
                NamedTextColor.GREEN
        ));

        target.sendActionBar(Component.text(
                "Zostałeś trafiony Świąteczną Kataną!",
                NamedTextColor.RED
        ));

        // Zamrożenie przeciwnika na 3 sekundy.
        zamrozGracza(target);

        // Wyznaczenie miejsca 2 bloki za przeciwnikiem.
        Location destination = getBehindLocation(target);

        // Rozpoczęcie dasha.
        startDash(player, destination);
    }

    private Location getBehindLocation(Player target) {

        Location targetLocation = target.getLocation().clone();

        double yaw = Math.toRadians(targetLocation.getYaw());

        double x = Math.sin(yaw) * 2.0;
        double z = -Math.cos(yaw) * 2.0;

        targetLocation.add(x, 0, z);

        return targetLocation;
    }

    private void zamrozGracza(Player target) {

        Location frozenLocation = target.getLocation().clone();

        new BukkitRunnable() {

            int ticks = 0;

            @Override
            public void run() {

                if (!target.isOnline() || target.isDead()) {
                    cancel();
                    return;
                }

                if (ticks >= 60) {
                    cancel();
                    return;
                }

                target.teleport(frozenLocation);
                target.setVelocity(new Vector(0, 0, 0));

                ticks++;
            }

        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void startDash(Player player, Location destination) {

        UUID uuid = player.getUniqueId();

        BukkitRunnable dash = new BukkitRunnable() {

            int ticks = 0;

            @Override
            public void run() {

                if (!player.isOnline() || player.isDead()) {
                    stopDash(player);
                    cancel();
                    return;
                }

                if (!player.getWorld().equals(destination.getWorld())) {
                    stopDash(player);
                    cancel();
                    return;
                }

                Location current = player.getLocation();

                Vector difference = destination.toVector()
                        .subtract(current.toVector());

                double distance = difference.length();

                // Dash zakończony.
                if (distance <= 0.7 || ticks >= 15) {
                    stopDash(player);
                    cancel();
                    return;
                }

                Vector direction = difference.normalize();

                // Sprawdzenie przeszkód na wysokości nóg, tułowia i głowy.
                if (isBlocked(player, direction)) {
                    stopDash(player);
                    cancel();
                    return;
                }

                // Szybki dash.
                player.setVelocity(direction.multiply(1.15));

                // Śnieżne particle przy nogach.
                Location particleLocation = player.getLocation().clone();
                particleLocation.add(0, 0.1, 0);

                player.getWorld().spawnParticle(
                        Particle.SNOWFLAKE,
                        particleLocation,
                        5,
                        0.25,
                        0.05,
                        0.25,
                        0.01
                );

                ticks++;
            }

        };

        activeDashes.put(uuid, dash);
        dash.runTaskTimer(plugin, 0L, 1L);
    }

    private boolean isBlocked(Player player, Vector direction) {

        Location base = player.getLocation();
        World world = player.getWorld();

        double distance = 1.25;

        double[] heights = {0.2, 0.9, 1.6};

        for (double height : heights) {

            Location start = base.clone().add(0, height, 0);

            RayTraceResult result = world.rayTraceBlocks(
                    start,
                    direction,
                    distance,
                    FluidCollisionMode.NEVER,
                    true
            );

            if (result != null && result.getHitBlock() != null) {
                return true;
            }
        }

        return false;
    }

    private void stopDash(Player player) {

        UUID uuid = player.getUniqueId();

        player.setVelocity(new Vector(0, 0, 0));

        activeDashes.remove(uuid);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        Player player = event.getPlayer();

        cooldowns.remove(player.getUniqueId());

        BukkitRunnable dash = activeDashes.remove(player.getUniqueId());

        if (dash != null) {
            dash.cancel();
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {

        Player player = event.getEntity();

        cooldowns.remove(player.getUniqueId());

        BukkitRunnable dash = activeDashes.remove(player.getUniqueId());

        if (dash != null) {
            dash.cancel();
        }
    }
}
