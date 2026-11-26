package pl.pykmcitems.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class TalizmanyGUI implements Listener {

    private static final LegacyComponentSerializer SERIALIZER =
            LegacyComponentSerializer.legacyAmpersand();

    private enum Menu {
        MAIN,
        LIST,
        OWN
    }

    private static class MenuHolder implements InventoryHolder {
        private final Menu menu;
        private Inventory inventory;

        private MenuHolder(Menu menu) {
            this.menu = menu;
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }

    private Component text(String text) {
        return SERIALIZER.deserialize(text)
                .decoration(TextDecoration.BOLD, false)
                .decoration(TextDecoration.ITALIC, false);
    }

    private ItemStack createItem(
            Material material,
            int customModelData,
            String name,
            String... loreLines
    ) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(text(name));

        List<Component> lore = new ArrayList<>();
        for (String line : loreLines) {
            lore.add(line.isEmpty() ? Component.empty() : text(line));
        }

        meta.lore(lore);

        if (customModelData >= 0) {
            meta.setCustomModelData(customModelData);
        }

        item.setItemMeta(meta);
        return item;
    }

    private Inventory createInventory(Menu menu, int rows, String title) {
        MenuHolder holder = new MenuHolder(menu);
        Inventory inventory = Bukkit.createInventory(
                holder,
                rows * 9,
                Component.text(title)
                        .decoration(TextDecoration.BOLD, false)
                        .decoration(TextDecoration.ITALIC, false)
        );

        holder.inventory = inventory;
        return inventory;
    }

    // ==========================================
    // GŁÓWNE MENU
    // ==========================================

    public void openMain(Player player) {
        Inventory inventory = createInventory(
                Menu.MAIN,
                3,
                "talizmany"
        );

        inventory.setItem(11, createItem(
                Material.BOOK,
                -1,
                "&6Lista talizmanów",
                "",
                "» Zobacz wszystkie dostępne talizmany",
                "» dostępne na serwerze.",
                "",
                "» &fKliknij, aby otworzyć!"
        ));

        inventory.setItem(15, createItem(
                Material.WHITE_DYE,
                5,
                "&fZarządzaj talizmanami",
                "",
                "» Zarządzaj założonymi talizmanami",
                "» i sprawdzaj ich bonusy",
                "&fKliknij, aby otworzyć!"
        ));

        player.openInventory(inventory);
    }

    // ==========================================
    // LISTA TALIZMANÓW
    // ==========================================

    private void openList(Player player) {
        Inventory inventory = createInventory(
                Menu.LIST,
                6,
                "lista talizmanów"
        );

        // ODPORNOŚĆ I
        inventory.setItem(11, createItem(
                Material.WHITE_DYE,
                13,
                "&b🛡 Talizman odporności I",
                "&b| +5% &7odporności na obrażenia."
        ));

        // ODPORNOŚĆ II
        inventory.setItem(20, createItem(
                Material.WHITE_DYE,
                12,
                "&b🛡 Talizman odporności II",
                "&b| +8% &7odporności na obrażenia."
        ));

        // ODPORNOŚĆ III
        inventory.setItem(29, createItem(
                Material.WHITE_DYE,
                11,
                "&b🛡 Talizman odporności III",
                "&b| ???% &7odporności na obrażenia.",
                "&b| &7Zakres losowania: &b+10% &7- &b15%",
                "",
                "&e☆ &bBonus maksymalnego poziomu:",
                "&7Kiedy przyjmujesz obrażenia,",
                "&7masz szansę otrzymać &ezłote serce &7absorpcji."
        ));

        // SIŁA I
        inventory.setItem(13, createItem(
                Material.WHITE_DYE,
                7,
                "&d⚔ Talizman siły I",
                "&d| +4% &7bonusowych obrażeń."
        ));

        // SIŁA II
        inventory.setItem(22, createItem(
                Material.WHITE_DYE,
                6,
                "&d⚔ Talizman siły II",
                "&d| +6% &7bonusowych obrażeń."
        ));

        // SIŁA III
        inventory.setItem(31, createItem(
                Material.WHITE_DYE,
                5,
                "&d⚔ Talizman siły III",
                "&d| ???% &7bonusowych obrażeń.",
                "&d| &7Zakres losowania: &d+8% &7- &d15%",
                "",
                "&e☆ &bBonus maksymalnego poziomu:",
                "&7Po zjedzeniu czegokolwiek istnieje szansa,",
                "&7na otrzymanie efektu &eszybkości &7na &e10 sekund."
        ));

        // SŁODKOŚCI III
        inventory.setItem(15, createItem(
                Material.ORANGE_DYE,
                1,
                "&b🍬 Talizman słodkości III",
                "&e| +1s &7czasu trwania regeneracji ze złotych jabłek.",
                "",
                "» &fZwiększa obrażenia krytyczne."
        ));

        // TALIZMAN DROPU
        inventory.setItem(24, createItem(
                Material.LAPIS_LAZULI,
                274,
                "&b✉ Talizman dropu",
                "&7» Po &czabiciu &7gracza jego przedmioty",
                "» &7trafiają do wolnego miejsca",
                "» &7w Twoim &denderchescie."
        ));

        // ŚWIĄTECZNY CZAS
        inventory.setItem(33, createItem(
                Material.WHITE_DYE,
                14,
                "&f❄ Talizman Świątecznego czasu",
                "&f| -???% &7cooldownu na użycie przedmiotów eventowych.",
                "",
                "&7» &fTalizman eventowy - posiada tylko 1 poziom.",
                "&f| &7Zakres losowania: &e+5% &7- &e20%"
        ));

        // POWRÓT
        inventory.setItem(53, createItem(
                Material.RED_STAINED_GLASS_PANE,
                -1,
                "&cPowrót"
        ));

        player.openInventory(inventory);
    }

    // ==========================================
    // TWOJE TALIZMANY
    // ==========================================

    private void openOwn(Player player) {
        Inventory inventory = createInventory(
                Menu.OWN,
                6,
                "twoje talizmany"
        );

        inventory.setItem(4, createItem(
                Material.OAK_SIGN,
                -1,
                "&6Talizmany"
        ));

        inventory.setItem(20, createItem(
                Material.BARRIER,
                -1,
                "&cWolne miejsce"
        ));

        inventory.setItem(22, createItem(
                Material.BARRIER,
                -1,
                "&cWolne miejsce"
        ));

        inventory.setItem(24, createItem(
                Material.BARRIER,
                -1,
                "&cWolne miejsce"
        ));

        inventory.setItem(45, createItem(
                Material.RED_STAINED_GLASS_PANE,
                -1,
                "&cPowrót"
        ));

        player.openInventory(inventory);
    }

    // ==========================================
    // OBSŁUGA KLIKNIĘĆ
    // ==========================================

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory topInventory = event.getView().getTopInventory();

        if (!(topInventory.getHolder() instanceof MenuHolder holder)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (event.getClickedInventory() != topInventory) {
            return;
        }

        int slot = event.getRawSlot();

        switch (holder.menu) {
            case MAIN -> {
                if (slot == 11) {
                    openList(player);
                } else if (slot == 15) {
                    openOwn(player);
                }
            }

            case LIST -> {
                if (slot == 53) {
                    openMain(player);
                }
            }

            case OWN -> {
                if (slot == 45) {
                    openMain(player);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder()
                instanceof MenuHolder) {
            event.setCancelled(true);
        }
    }
}
