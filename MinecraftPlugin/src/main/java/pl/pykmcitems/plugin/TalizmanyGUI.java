
package pl.pykmcitems.plugin;

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
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

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

    private void openMain(Player player) {
        Inventory inventory = createInventory(Menu.MAIN, 3, "talizmany");

        inventory.setItem(11, button(
                Material.BOOK, "§6Lista talizmanów"
        ));

        inventory.setItem(15, button(
                Material.WHITE_DYE, "§fZarządzaj talizmanami"
        ));

        player.openInventory(inventory);
    }

    private void openList(Player player) {
        Inventory inventory = createInventory(
                Menu.LIST, 6, "lista talizmanów"
        );

        inventory.setItem(11, Itemy.talizmanOdpornosciI(plugin));
        inventory.setItem(20, Itemy.talizmanOdpornosciII(plugin));
        inventory.setItem(29, Itemy.talizmanOdpornosciIII(
                plugin, losuj(10, 15)
        ));

        inventory.setItem(13, Itemy.talizmanSilyI(plugin));
        inventory.setItem(22, Itemy.talizmanSilyII(plugin));
        inventory.setItem(31, Itemy.talizmanSilyIII(
                plugin, losuj(8, 15)
        ));

        inventory.setItem(15, Itemy.talizmanSlodkosciIII(plugin));
        inventory.setItem(24, Itemy.talizmanDropu(plugin));

        inventory.setItem(33, Itemy.talizmanSwiatecznegoCzasu(
                plugin, losuj(5, 20)
        ));

        inventory.setItem(53, button(
                Material.RED_STAINED_GLASS_PANE, "§cPowrót"
        ));

        player.openInventory(inventory);
    }

    private int losuj(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    private void openOwn(Player player) {
        Inventory inventory = createInventory(
                Menu.OWN, 6, "twoje talizmany"
        );

        inventory.setItem(4, button(Material.OAK_SIGN, "§6Talizmany"));

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
        if (!(event.getView().getTopInventory().getHolder()
                instanceof MenuHolder holder)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() != event.getView().getTopInventory()) return;

        // PPM nic nie robi.
        if (event.isRightClick()) return;

        int slot = event.getRawSlot();

        switch (holder.menu) {
            case MAIN -> {
                if (slot == 11) openList(player);
                if (slot == 15) openOwn(player);
            }

            case LIST -> {
                if (slot == 53) {
                    openMain(player);
                    return;
                }

                ItemStack item = event.getCurrentItem();

                if (item == null || item.getType() == Material.AIR) return;

                if (manager.equip(player, item)) {
                    player.sendMessage("§aZałożono talizman!");
                    openOwn(player);
                }
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
                        player.sendMessage("§aZdjęto talizman!");
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
        }
    }
}
