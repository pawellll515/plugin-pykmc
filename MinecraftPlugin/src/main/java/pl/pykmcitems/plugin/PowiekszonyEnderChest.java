
package pl.pykmcitems.plugin;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class PowiekszonyEnderChest implements Listener {

    private final JavaPlugin plugin;
    private final org.bukkit.configuration.file.YamlConfiguration config;
    private final java.io.File file;

    public PowiekszonyEnderChest(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new java.io.File(plugin.getDataFolder(), "enderchesty.yml");

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        this.config = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file);
    }

    public void open(Player player) {
        Inventory inventory = getInventory(player);
        player.openInventory(inventory);
    }

    private Inventory getInventory(Player player) {
        String path = player.getUniqueId().toString();

        Inventory inventory = Bukkit.createInventory(
                new ChestHolder(player.getUniqueId()),
                54,
                "§8Powiększony Ender Chest"
        );

        if (config.contains(path)) {
            for (int slot = 0; slot < 54; slot++) {
                ItemStack item = config.getItemStack(path + "." + slot);

                if (item != null && !item.getType().isAir()) {
                    inventory.setItem(slot, item);
                }
            }
        }

        return inventory;
    }

    @EventHandler
    public void onEnderChestClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) {
            return;
        }

        if (event.getClickedBlock() == null
                || event.getClickedBlock().getType() != Material.ENDER_CHEST) {
            return;
        }

        event.setCancelled(true);
        open(event.getPlayer());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof ChestHolder holder)) {
            return;
        }

        saveInventory(holder.getOwner(), event.getInventory());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        Inventory top = player.getOpenInventory().getTopInventory();

        if (top.getHolder() instanceof ChestHolder holder) {
            saveInventory(holder.getOwner(), top);
        }
    }

    public List<ItemStack> addItems(Player player, Collection<ItemStack> items) {
        Inventory inventory = getInventory(player);
        List<ItemStack> leftovers = new ArrayList<>();

        for (ItemStack item : items) {
            if (item == null || item.getType().isAir()) {
                continue;
            }

            Map<Integer, ItemStack> result = inventory.addItem(item.clone());

            for (ItemStack leftover : result.values()) {
                leftovers.add(leftover.clone());
            }
        }

        saveInventory(player.getUniqueId(), inventory);
        return leftovers;
    }

    public void clear(Player player) {
        Inventory inventory = getInventory(player);
        inventory.clear();
        saveInventory(player.getUniqueId(), inventory);
    }

    public void saveAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Inventory top = player.getOpenInventory().getTopInventory();

            if (top.getHolder() instanceof ChestHolder holder) {
                saveInventory(holder.getOwner(), top);
            }
        }
    }

    private void saveInventory(UUID uuid, Inventory inventory) {
        String path = uuid.toString();

        config.set(path, null);

        for (int slot = 0; slot < 54; slot++) {
            ItemStack item = inventory.getItem(slot);

            if (item != null && !item.getType().isAir()) {
                config.set(path + "." + slot, item);
            }
        }

        saveConfig();
    }

    private void saveConfig() {
        try {
            config.save(file);
        } catch (java.io.IOException exception) {
            plugin.getLogger().severe("Nie udało się zapisać Ender Chestów!");
            exception.printStackTrace();
        }
    }

    private static final class ChestHolder implements InventoryHolder {

        private final UUID owner;

        private ChestHolder(UUID owner) {
            this.owner = owner;
        }

        public UUID getOwner() {
            return owner;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
