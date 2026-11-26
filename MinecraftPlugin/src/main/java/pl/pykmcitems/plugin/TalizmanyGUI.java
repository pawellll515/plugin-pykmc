
package pl.pykmcitems.plugin;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class TalizmanyGUI implements Listener {

    private final JavaPlugin plugin;
    private final TalizmanyManager manager;

    private enum Menu {
        MAIN, LIST, OWN
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

    public TalizmanyGUI(JavaPlugin plugin, TalizmanyManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    private Inventory createInventory(Menu menu, int rows, String title) {
        MenuHolder holder = new MenuHolder(menu);
        Inventory inventory = Bukkit.createInventory(
                holder, rows * 9, title
        );
        holder.inventory = inventory;
        return inventory;
    }

    private ItemStack button(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    public void openMain(Player player) {
        Inventory inventory = createInventory(Menu.MAIN, 3, "talizmany");

        inventory.setItem(11, button(
                Material.BOOK, "§6Lista talizmanów"
        ));

        ItemStack zarzadzanie = new ItemStack(Material.WHITE_DYE);
        ItemMeta meta = zarzadzanie.getItemMeta();
        meta.setDisplayName("§fZarządzaj talizmanami");
        meta.setCustomModelData(5);
        zarzadzanie.setItemMeta(meta);

        inventory.setItem(15, zarzadzanie);

        player.openInventory(inventory);
    }

    private ItemStack ukryjWartosc(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return item;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        if (meta.hasDisplayName()) {
            String name = meta.getDisplayName();

            if (name.contains("%")) {
                meta.setDisplayName(name.replaceAll("[0-9]+([,.][0-9]+)?%", "???"));
            }
        }

        if (meta.hasLore()) {
            List<String> lore = new ArrayList<>();

            for (String line : meta.getLore()) {
                lore.add(line.replaceAll("[0-9]+([,.][0-9]+)?%", "???"));
            }

            meta.setLore(lore);
        }

        item.setItemMeta(meta);
        return item;
    }

    private void openList(Player player) {
        Inventory inventory = createInventory(
                Menu.LIST, 6, "lista talizmanów"
        );

        inventory.setItem(11, Itemy.talizmanOdpornosciI(plugin));
        inventory.setItem(20, Itemy.talizmanOdpornosciII(plugin));
        inventory.setItem(29, ukryjWartosc(
                Itemy.talizmanOdpornosciIII(plugin, 10.0)
        ));

        inventory.setItem(13, Itemy.talizmanSilyI(plugin));
        inventory.setItem(22, Itemy.talizmanSilyII(plugin));
        inventory.setItem(31, ukryjWartosc(
                Itemy.talizmanSilyIII(plugin, 8.0)
        ));

        inventory.setItem(15, Itemy.talizmanSlodkosciIII(plugin));
        inventory.setItem(24, Itemy.talizmanDropu(plugin));

        inventory.setItem(33, ukryjWartosc(
                Itemy.talizmanSwiatecznegoCzasu(plugin, 5.0)
        ));

        inventory.setItem(53, button(
                Material.RED_STAINED_GLASS_PANE, "§cPowrót"
        ));

        player.openInventory(inventory);
    }

    private void openOwn(Player player) {
        Inventory inventory = createInventory(
                Menu.OWN, 6, "twoje talizmany"
        );

        inventory.setItem(4, button(
                Material.OAK_SIGN, "§6Talizmany"
        ));

        int[] slots = {20, 22, 24};
        List<ItemStack> equipped = manager.getEquipped(player);

        for (int i = 0; i < slots.length; i++) {
            if (i < equipped.size()) {
                inventory.setItem(slots[i], equipped.get(i).clone());
            } else {
                inventory.setItem(slots[i], button(
                        Material.BARRIER, "§cWolne miejsce"
                ));
            }
        }

        inventory.setItem(45, button(
                Material.RED_STAINED_GLASS_PANE, "§cPowrót"
        ));

        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();

        if (!(top.getHolder() instanceof MenuHolder holder)) {
            return;
        }

        event.setCancelled(true);
        event.setResult(Event.Result.DENY);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (event.getClickedInventory() != top) {
            return;
        }

        if (event.getClick() != ClickType.LEFT) {
            return;
        }

        InventoryAction action = event.getAction();

        if (action != InventoryAction.NOTHING
                && action != InventoryAction.PICKUP_ALL) {
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
                // Klikanie talizmanów na liście nie wykonuje żadnej akcji.
            }

            case OWN -> {
                if (slot == 45) {
                    openMain(player);
                    return;
                }

                int talismanSlot = switch (slot) {
                    case 20 -> 0;
                    case 22 -> 1;
                    case 24 -> 2;
                    default -> -1;
                };

                if (talismanSlot >= 0) {
                    if (manager.unequip(player, talismanSlot)) {
                        openOwn(player);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder()
                instanceof MenuHolder) {

            event.setCancelled(true);
            event.setResult(Event.Result.DENY);
        }
    }
}
