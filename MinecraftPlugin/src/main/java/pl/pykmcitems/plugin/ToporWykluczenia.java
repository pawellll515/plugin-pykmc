
package pl.pykmcitems.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ToporWykluczenia implements Listener {

    private final JavaPlugin plugin;
    private final NamespacedKey toporKey;

    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, Long> blokady = new HashMap<>();

    public ToporWykluczenia(JavaPlugin plugin) {
        this.plugin = plugin;
        this.toporKey = new NamespacedKey(plugin, "topor_wykluczenia");
    }

    private boolean isTopor(ItemStack item) {
        if (item == null || item.getType() != Material.IRON_AXE
                || !item.hasItemMeta()) {
            return false;
        }

        return item.getItemMeta().getPersistentDataContainer()
                .has(toporKey, PersistentDataType.BYTE);
    }

    private boolean isZablokowany(Player player) {
        Long koniec = blokady.get(player.getUniqueId());

        if (koniec == null) {
            return false;
        }

        if (koniec <= System.currentTimeMillis()) {
            blokady.remove(player.getUniqueId());
            return false;
        }

        return true;
    }

    private boolean isArmorOrElytra(Material material) {
        return switch (material) {
            case LEATHER_HELMET,
                 CHAINMAIL_HELMET,
                 IRON_HELMET,
                 GOLDEN_HELMET,
                 DIAMOND_HELMET,
                 NETHERITE_HELMET,
                 TURTLE_HELMET,
                 LEATHER_CHESTPLATE,
                 CHAINMAIL_CHESTPLATE,
                 IRON_CHESTPLATE,
                 GOLDEN_CHESTPLATE,
                 DIAMOND_CHESTPLATE,
                 NETHERITE_CHESTPLATE,
                 LEATHER_LEGGINGS,
                 CHAINMAIL_LEGGINGS,
                 IRON_LEGGINGS,
                 GOLDEN_LEGGINGS,
                 DIAMOND_LEGGINGS,
                 NETHERITE_LEGGINGS,
                 LEATHER_BOOTS,
                 CHAINMAIL_BOOTS,
                 IRON_BOOTS,
                 GOLDEN_BOOTS,
                 DIAMOND_BOOTS,
                 NETHERITE_BOOTS,
                 ELYTRA -> true;
            default -> false;
        };
    }

    private void wyslijBlokade(Player player) {
        player.sendActionBar(Component.text(
                "Ekwipunek zablokowany!",
                NamedTextColor.RED
        ));
    }

    // UDERZENIE TOPorem

    @EventHandler(priority = EventPriority.NORMAL)
    public void onDamage(EntityDamageByEntityEvent event) {

        if (!(event.getDamager() instanceof Player attacker)) {
            return;
        }

        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        ItemStack item = attacker.getInventory().getItemInMainHand();

        if (!isTopor(item)) {
            return;
        }

        UUID uuid = attacker.getUniqueId();
        long now = System.currentTimeMillis();

        Long cooldownEnd = cooldowns.get(uuid);

        if (cooldownEnd != null && cooldownEnd > now) {
            attacker.sendActionBar(Component.text(
                    "Topór jest na cooldownie!",
                    NamedTextColor.RED
            ));
            return;
        }

        cooldowns.put(uuid, now + 120_000L);
        attacker.setCooldown(Material.IRON_AXE, 2400);

        blokady.put(victim.getUniqueId(), now + 8_000L);

        victim.sendActionBar(Component.text(
                "Topór Wykluczenia zablokował Twój ekwipunek!",
                NamedTextColor.GOLD
        ));
    }

    // BLOKADA KLIKANIA W EKWIPUNKU

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (!isZablokowany(player)) {
            return;
        }

        event.setCancelled(true);
        wyslijBlokade(player);
    }

    // BLOKADA PRZECIAGANIA PRZEDMIOTOW

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (!isZablokowany(player)) {
            return;
        }

        event.setCancelled(true);
        wyslijBlokade(player);
    }

    // BLOKADA WYRZUCANIA

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrop(PlayerDropItemEvent event) {

        Player player = event.getPlayer();

        if (!isZablokowany(player)) {
            return;
        }

        event.setCancelled(true);
        player.sendActionBar(Component.text(
                "Nie możesz teraz ruszać przedmiotami!",
                NamedTextColor.RED
        ));
    }

    // BLOKADA PODNOSZENIA

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPickup(PlayerPickupItemEvent event) {

        Player player = event.getPlayer();

        if (isZablokowany(player)) {
            event.setCancelled(true);
        }
    }

    // BLOKADA ZMIANY RAKI

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSwap(PlayerSwapHandItemsEvent event) {

        Player player = event.getPlayer();

        if (!isZablokowany(player)) {
            return;
        }

        event.setCancelled(true);
        player.sendActionBar(Component.text(
                "Nie możesz teraz zmieniać przedmiotów!",
                NamedTextColor.RED
        ));
    }

    // BLOKADA ZAKLADANIA ZBROI NA SIEBIE

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRightClick(PlayerInteractEvent event) {

        Player player = event.getPlayer();

        if (!isZablokowany(player)) {
            return;
        }

        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();

        if (item != null && isArmorOrElytra(item.getType())) {
            event.setCancelled(true);

            player.sendActionBar(Component.text(
                    "Nie możesz teraz zakładać zbroi!",
                    NamedTextColor.RED
            ));
        }
    }

    // BLOKADA ZAKLADANIA ZBROI NA INNYCH GRACZY

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRightClickEntity(PlayerInteractEntityEvent event) {

        Player player = event.getPlayer();
        Entity clicked = event.getRightClicked();

        if (!(clicked instanceof Player target)) {
            return;
        }

        ItemStack item = event.getHand() == EquipmentSlot.HAND
                ? player.getInventory().getItemInMainHand()
                : player.getInventory().getItemInOffHand();

        if (item == null || !isArmorOrElytra(item.getType())) {
            return;
        }

        // Blokujemy zakładanie zbroi na zablokowanego gracza.
        if (isZablokowany(target)) {
            event.setCancelled(true);
            return;
        }

        // Zablokowany gracz nie może zakładać zbroi na innych.
        if (isZablokowany(player)) {
            event.setCancelled(true);
            wyslijBlokade(player);
        }
    }

    // CZYSZCZENIE PO WYJSCIU

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        UUID uuid = event.getPlayer().getUniqueId();

        cooldowns.remove(uuid);
        blokady.remove(uuid);
    }

    // CZYSZCZENIE PO SMIERCI

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {

        UUID uuid = event.getEntity().getUniqueId();

        cooldowns.remove(uuid);
        blokady.remove(uuid);
    }
}
