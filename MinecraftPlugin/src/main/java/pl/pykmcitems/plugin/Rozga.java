
package pl.pykmcitems.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Rozga implements Listener {

    private final JavaPlugin plugin;

    private final NamespacedKey rozgaKey;
    private final NamespacedKey temporaryHeadKey;

    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, ItemStack> savedHelmets = new HashMap<>();

    public Rozga(JavaPlugin plugin) {
        this.plugin = plugin;

        this.rozgaKey = new NamespacedKey(plugin, "rozga");
        this.temporaryHeadKey = new NamespacedKey(plugin, "rozga_glowka");
    }

    // SPRAWDZANIE RÓZGI

    private boolean isRozga(ItemStack item) {

        if (item == null || item.getType() != Material.STICK) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        return meta != null
                && meta.getPersistentDataContainer().has(
                        rozgaKey,
                        PersistentDataType.BYTE
                );
    }

    // SPRAWDZANIE TYMCZASOWEJ GŁÓWKI

    private boolean isTemporaryHead(ItemStack item) {

        if (item == null || item.getType() != Material.PLAYER_HEAD) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        return meta != null
                && meta.getPersistentDataContainer().has(
                        temporaryHeadKey,
                        PersistentDataType.BYTE
                );
    }

    // SPRAWDZANIE PRZEDMIOTÓW, KTÓRE MOŻNA ZAŁOŻYĆ NA GŁOWĘ

    private boolean isHeadItem(ItemStack item) {

        if (item == null || item.getType() == Material.AIR) {
            return false;
        }

        Material type = item.getType();

        return type.name().endsWith("_HELMET")
                || type == Material.CARVED_PUMPKIN
                || type == Material.JACK_O_LANTERN
                || type == Material.PLAYER_HEAD
                || type == Material.SKELETON_SKULL
                || type == Material.WITHER_SKELETON_SKULL
                || type == Material.ZOMBIE_HEAD
                || type == Material.CREEPER_HEAD
                || type == Material.DRAGON_HEAD
                || type == Material.PIGLIN_HEAD;
    }

    // TWORZENIE GŁÓWKI ZAPISANEJ W CONFIGU

    private ItemStack createTemporaryHead() {

        ItemStack configuredHead =
                plugin.getConfig().getItemStack("rozga.head");

        if (configuredHead == null
                || configuredHead.getType() != Material.PLAYER_HEAD) {
            return null;
        }

        ItemStack head = configuredHead.clone();
        head.setAmount(1);

        ItemMeta meta = head.getItemMeta();

        if (meta == null) {
            return null;
        }

        meta.getPersistentDataContainer().set(
                temporaryHeadKey,
                PersistentDataType.BYTE,
                (byte) 1
        );

        head.setItemMeta(meta);

        return head;
    }

    // PRZYWRACANIE ORYGINALNEGO HEŁMU

    private void restoreHelmet(Player player) {

        UUID uuid = player.getUniqueId();

        ItemStack originalHelmet = savedHelmets.remove(uuid);

        if (originalHelmet == null) {
            return;
        }

        ItemStack currentHelmet =
                player.getInventory().getHelmet();

        if (isTemporaryHead(currentHelmet)) {

            player.getInventory().setHelmet(originalHelmet);

        } else {

            Map<Integer, ItemStack> leftovers =
                    player.getInventory().addItem(originalHelmet);

            for (ItemStack leftover : leftovers.values()) {

                player.getWorld().dropItemNaturally(
                        player.getLocation(),
                        leftover
                );
            }
        }
    }

    // UDERZENIE RÓZGĄ

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {

        if (!(event.getDamager() instanceof Player attacker)) {
            return;
        }

        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        ItemStack weapon =
                attacker.getInventory().getItemInMainHand();

        if (!isRozga(weapon)) {
            return;
        }

        // SPRAWDZANIE, CZY USTAWIONO GŁÓWKĘ

        ItemStack temporaryHead = createTemporaryHead();

        if (temporaryHead == null) {

            attacker.sendActionBar(
                    Component.text(
                            "Najpierw ustaw glowke komenda /ustawglowke!",
                            NamedTextColor.RED
                    )
            );

            return;
        }

        UUID attackerUUID = attacker.getUniqueId();

        long now = System.currentTimeMillis();

        // SPRAWDZANIE COOLDOWNU

        if (cooldowns.containsKey(attackerUUID)
                && cooldowns.get(attackerUUID) > now) {

            attacker.sendActionBar(
                    Component.text(
                            "Rózga jest na cooldownie!",
                            NamedTextColor.RED
                    )
            );

            return;
        }

        // SPRAWDZANIE HEŁMU

        ItemStack victimHelmet =
                victim.getInventory().getHelmet();

        if (victimHelmet == null
                || victimHelmet.getType() == Material.AIR) {

            attacker.sendActionBar(
                    Component.text(
                            "Ten gracz nie ma hełmu!",
                            NamedTextColor.RED
                    )
            );

            return;
        }

        // SPRAWDZANIE, CZY RÓZGA JUŻ DZIAŁA NA GRACZU

        if (savedHelmets.containsKey(victim.getUniqueId())) {

            attacker.sendActionBar(
                    Component.text(
                            "Ten gracz ma już zdjęty hełm!",
                            NamedTextColor.RED
                    )
            );

            return;
        }

        // COOLDOWN 120 SEKUND

        cooldowns.put(attackerUUID, now + 120_000);
        ((Main) plugin).getCooldownScoreboard().startCooldown(attacker, "rozga", "&7&lRózga", 120_000L);

        // ZAPISANIE ORYGINALNEGO HEŁMU

        ItemStack originalHelmet = victimHelmet.clone();

        savedHelmets.put(
                victim.getUniqueId(),
                originalHelmet
        );

        // ZAŁOŻENIE ZAPISANEJ GŁÓWKI

        victim.getInventory().setHelmet(temporaryHead);

        // KOMUNIKAT DLA ATAKUJĄCEGO

        attacker.sendActionBar(
                Component.text("Rózga ", NamedTextColor.GRAY)
                        .decorate(TextDecoration.BOLD)
                        .append(Component.text(
                                "zdjęła hełm!",
                                NamedTextColor.WHITE
                        ))
        );

        // KOMUNIKAT DLA TRAFIONEGO

        victim.sendActionBar(
                Component.text(
                        "Twój hełm został zdjęty na ",
                        NamedTextColor.RED
                ).append(Component.text(
                        "3.5 sekundy!",
                        NamedTextColor.YELLOW
                ))
        );

        // PRZYWRÓCENIE HEŁMU PO 3.5 SEKUNDY

        Bukkit.getScheduler().runTaskLater(plugin, () -> {

            if (!victim.isOnline()) {
                return;
            }

            restoreHelmet(victim);

        }, 70L);
    }

    // BLOKOWANIE ZDEJMOWANIA GŁÓWKI W EKWIPUNKU

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (!savedHelmets.containsKey(player.getUniqueId())) {
            return;
        }

        // BLOKOWANIE KLIKNIĘCIA W SLOT HEŁMU

        if (event.getView().getType() == InventoryType.CRAFTING
                && event.getRawSlot() == 5) {

            event.setCancelled(true);
            return;
        }

        // BLOKOWANIE ZAKŁADANIA HEŁMU PRZEZ SHIFT-CLICK

        if (event.isShiftClick()
                && isHeadItem(event.getCurrentItem())) {

            event.setCancelled(true);
        }
    }

    // BLOKOWANIE PRZECIĄGANIA PRZEDMIOTÓW NA SLOT HEŁMU

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (!savedHelmets.containsKey(player.getUniqueId())) {
            return;
        }

        if (event.getView().getType() == InventoryType.CRAFTING
                && event.getRawSlots().contains(5)) {

            event.setCancelled(true);
        }
    }

    // BLOKOWANIE ZAKŁADANIA HEŁMU PPM

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();

        if (!savedHelmets.containsKey(player.getUniqueId())) {
            return;
        }

        Action action = event.getAction();

        if (action != Action.RIGHT_CLICK_AIR
                && action != Action.RIGHT_CLICK_BLOCK) {

            return;
        }

        ItemStack usedItem = event.getItem();

        if (isHeadItem(usedItem)) {
            event.setCancelled(true);
        }
    }

    // ŚMIERĆ PODCZAS DZIAŁANIA RÓZGI

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {

        Player victim = event.getEntity();

        UUID uuid = victim.getUniqueId();

        if (!savedHelmets.containsKey(uuid)) {
            return;
        }

        ItemStack originalHelmet = savedHelmets.remove(uuid);

        // USUNIĘCIE TYMCZASOWEJ GŁÓWKI Z DROPU

        event.getDrops().removeIf(this::isTemporaryHead);

        // WYRZUCENIE ORYGINALNEGO HEŁMU

        event.getDrops().add(originalHelmet);

        victim.getInventory().setHelmet(
                new ItemStack(Material.AIR)
        );
    }

    // WYJŚCIE GRACZA Z SERWERA

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        Player player = event.getPlayer();

        // PRZYWRÓCENIE ORYGINALNEGO HEŁMU

        restoreHelmet(player);

        // USUNIĘCIE COOLDOWNU

        cooldowns.remove(player.getUniqueId());
    }
}
