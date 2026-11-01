package pl.pykmcitems.plugin;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Lodolap implements Listener {

    private final JavaPlugin plugin;

    private final NamespacedKey itemKey;
    private final NamespacedKey projectileKey;
    private final NamespacedKey elytraBlockKey;

    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> cooldownVersions = new ConcurrentHashMap<>();

    public Lodolap(JavaPlugin plugin) {
        this.plugin = plugin;

        this.itemKey = new NamespacedKey(plugin, "lodolap_item");
        this.projectileKey = new NamespacedKey(plugin, "lodolap_projectile");
        this.elytraBlockKey = new NamespacedKey(plugin, "lodolap_elytra_block");
    }

    private boolean isLodolap(ItemStack item) {
        if (item == null || item.getType() != Material.SNOWBALL) {
            return false;
        }

        return item.getItemMeta() != null
                && item.getItemMeta().getPersistentDataContainer()
                .has(itemKey, PersistentDataType.BYTE);
    }

    private boolean hasCooldown(Player player) {
        Long koniec = cooldowns.get(player.getUniqueId());

        if (koniec == null) {
            return false;
        }

        if (System.currentTimeMillis() >= koniec) {
            cooldowns.remove(player.getUniqueId());
            return false;
        }

        return true;
    }

    private void ustawCooldown(Player player, int sekundy) {
        UUID uuid = player.getUniqueId();

        int wersja = cooldownVersions.getOrDefault(uuid, 0) + 1;
        cooldownVersions.put(uuid, wersja);

        cooldowns.put(uuid, System.currentTimeMillis() + sekundy * 1000L);
        player.setCooldown(Material.SNOWBALL, sekundy * 20);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            Integer aktualnaWersja = cooldownVersions.get(uuid);

            if (aktualnaWersja != null && aktualnaWersja == wersja) {
                cooldowns.remove(uuid);
                cooldownVersions.remove(uuid);
            }
        }, sekundy * 20L);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (!isLodolap(item)) {
            return;
        }

        event.setCancelled(true);

        if (hasCooldown(player)) {
            return;
        }

        // Cooldown 3 sekundy po rzuceniu.
        ustawCooldown(player, 3);

        // Ręczne tworzenie śnieżki sprawia, że przedmiot nie jest zużywany.
        Snowball snowball = player.getWorld().spawn(
                player.getEyeLocation(),
                Snowball.class
        );

        snowball.setShooter(player);
        snowball.setVelocity(player.getEyeLocation().getDirection().multiply(1.5));

        snowball.getPersistentDataContainer().set(
                projectileKey,
                PersistentDataType.BYTE,
                (byte) 1
        );
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Snowball snowball)) {
            return;
        }

        if (!snowball.getPersistentDataContainer()
                .has(projectileKey, PersistentDataType.BYTE)) {
            return;
        }

        if (!(snowball.getShooter() instanceof Player shooter)) {
            return;
        }

        if (!(event.getHitEntity() instanceof Player target)) {
            return;
        }

        // Trafienie gracza zmienia cooldown na 60 sekund.
        ustawCooldown(shooter, 60);

        UUID targetUUID = target.getUniqueId();

        target.getPersistentDataContainer().set(
                elytraBlockKey,
                PersistentDataType.BYTE,
                (byte) 1
        );

        target.setGliding(false);

        target.sendTitle(
                "§4§lTwoja elytra została zablokowana!",
                "§7Nie możesz latać przez 10 sekund",
                5, 40, 10
        );

        // Blokada elytry trwa 10 sekund.
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            Player onlineTarget = plugin.getServer().getPlayer(targetUUID);

            if (onlineTarget != null) {
                onlineTarget.getPersistentDataContainer().remove(elytraBlockKey);

                onlineTarget.sendTitle(
                        "§a§lMożesz ponownie użyć elytry",
                        "",
                        5, 40, 10
                );
            }
        }, 200L);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGlide(EntityToggleGlideEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // Sprawdzamy tylko gracza, który właśnie próbuje rozpocząć szybowanie.
        if (!event.isGliding()) {
            return;
        }

        if (player.getPersistentDataContainer()
                .has(elytraBlockKey, PersistentDataType.BYTE)) {
            event.setCancelled(true);
            player.setGliding(false);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        cooldowns.remove(uuid);
        cooldownVersions.remove(uuid);
        player.getPersistentDataContainer().remove(elytraBlockKey);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID uuid = player.getUniqueId();

        cooldowns.remove(uuid);
        cooldownVersions.remove(uuid);
        player.getPersistentDataContainer().remove(elytraBlockKey);
    }
}
