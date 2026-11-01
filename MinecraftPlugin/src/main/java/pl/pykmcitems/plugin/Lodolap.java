package pl.pykmcitems.plugin;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Lodolap implements Listener {

    private final JavaPlugin plugin;

    private final NamespacedKey itemKey;
    private final NamespacedKey projectileKey;

    // Cooldown rzucającego
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    // Czas zakończenia blokady elytry
    private final Map<UUID, Long> elytraBlocks = new ConcurrentHashMap<>();

    public Lodolap(JavaPlugin plugin) {
        this.plugin = plugin;

        itemKey = new NamespacedKey(plugin, "lodolap_item");
        projectileKey = new NamespacedKey(plugin, "lodolap_projectile");
    }

    private boolean isLodolap(ItemStack item) {
        if (item == null || item.getType() != Material.SNOWBALL) {
            return false;
        }

        if (!item.hasItemMeta()) {
            return false;
        }

        return item.getItemMeta()
                .getPersistentDataContainer()
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
        cooldowns.put(
                player.getUniqueId(),
                System.currentTimeMillis() + sekundy * 1000L
        );

        player.sendMessage(
                "§bLodołap §8» §7Cooldown: §e" + sekundy + " sekund."
        );
    }

    @EventHandler(priority = EventPriority.HIGHEST)
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
            long pozostalo = cooldowns.get(player.getUniqueId())
                    - System.currentTimeMillis();

            player.sendMessage(
                    "§cMusisz poczekać jeszcze §e"
                            + Math.max(1, (pozostalo + 999) / 1000)
                            + "s §czanim użyjesz Lodołapa!"
            );
            return;
        }

        // Cooldown 3 sekundy po rzuceniu
        ustawCooldown(player, 3);

        // Tworzymy pocisk ręcznie, więc przedmiot nie jest zużywany
        Snowball snowball = player.getWorld().spawn(
                player.getEyeLocation(),
                Snowball.class
        );

        snowball.setShooter(player);
        snowball.setVelocity(
                player.getEyeLocation().getDirection().multiply(1.5)
        );

        snowball.getPersistentDataContainer().set(
                projectileKey,
                PersistentDataType.BYTE,
                (byte) 1
        );

        player.swingMainHand();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onProjectileHit(ProjectileHitEvent event) {

        if (!(event.getEntity() instanceof Snowball snowball)) {
            return;
        }

        if (!snowball.getPersistentDataContainer().has(
                projectileKey,
                PersistentDataType.BYTE
        )) {
            return;
        }

        if (!(snowball.getShooter() instanceof Player shooter)) {
            return;
        }

        if (!(event.getHitEntity() instanceof Player target)) {
            return;
        }

        // Trafienie gracza zmienia cooldown na 60 sekund
        ustawCooldown(shooter, 60);

        UUID targetUUID = target.getUniqueId();

        // Blokada elytry na 10 sekund
        elytraBlocks.put(
                targetUUID,
                System.currentTimeMillis() + 10_000L
        );

        // Natychmiast wyłączamy szybowanie
        target.setGliding(false);

        target.sendTitle(
                "§4§lTwoja elytra została zablokowana!",
                "§7Nie możesz latać przez 10 sekund",
                5, 40, 10
        );

        // Po 10 sekundach usuwamy blokadę
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {

            Long koniec = elytraBlocks.get(targetUUID);

            if (koniec == null || System.currentTimeMillis() < koniec) {
                return;
            }

            elytraBlocks.remove(targetUUID);

            Player onlineTarget = plugin.getServer().getPlayer(targetUUID);

            if (onlineTarget != null) {
                onlineTarget.sendTitle(
                        "§a§lBlokada elytry zakończona!",
                        "§7Możesz ponownie używać elytry",
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

        if (!event.isGliding()) {
            return;
        }

        Long koniec = elytraBlocks.get(player.getUniqueId());

        if (koniec == null) {
            return;
        }

        if (System.currentTimeMillis() >= koniec) {
            elytraBlocks.remove(player.getUniqueId());
            return;
        }

        // Blokujemy każdą próbę rozpoczęcia szybowania
        event.setCancelled(true);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        cooldowns.remove(uuid);
        elytraBlocks.remove(uuid);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        UUID uuid = event.getEntity().getUniqueId();

        cooldowns.remove(uuid);
        elytraBlocks.remove(uuid);
    }
}
