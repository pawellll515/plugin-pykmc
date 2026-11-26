
package pl.pykmcitems.plugin;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PowiekszonyEnderChest implements Listener {

    private static final int ROZMIAR = 54;

    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration config;

    private final Map<UUID, Inventory> inventories = new HashMap<>();

    public PowiekszonyEnderChest(JavaPlugin plugin) {
        this.plugin = plugin;

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        this.file = new File(plugin.getDataFolder(), "enderchesty.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    // ==========================================
    // WŁASNY HOLDER
    // ==========================================

    private static final class ChestHolder implements InventoryHolder {

        private Inventory inventory;

        @Override
        public Inventory getInventory() {
            return inventory;
        }

        public void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }
    }

    // ==========================================
    // POBIERANIE EKWIPUNKU ENDER CHESTA
    // ==========================================

    private Inventory getInventory(Player player) {
        UUID uuid = player.getUniqueId();

        Inventory inventory = inventories.get(uuid);

        if (inventory != null) {
            return inventory;
        }

        ChestHolder holder = new ChestHolder();

        inventory = Bukkit.createInventory(
                holder,
                ROZMIAR,
                Component.text("Ender Chest")
        );

        holder.setInventory(inventory);

        String path = uuid.toString();

        for (int slot = 0; slot < ROZMIAR; slot++) {
            ItemStack item = config.getItemStack(path + "." + slot);

            if (item != null && !item.getType().isAir()) {
                inventory.setItem(slot, item);
            }
        }

        inventories.put(uuid, inventory);

        return inventory;
    }

    // ==========================================
    // OTWIERANIE ENDER CHESTA
    // ==========================================

    public void open(Player player) {
        player.openInventory(getInventory(player));
    }

    // ==========================================
    // OTWIERANIE PO KLIKNIĘCIU BLOKU
    // ==========================================

    @EventHandler
    public void onEnderChestClick(PlayerInteractEvent event) {
        if (event.getHand() == null
                || event.getHand().name().equals("OFF_HAND")) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (event.getClickedBlock() == null
                || event.getClickedBlock().getType() != Material.ENDER_CHEST) {
            return;
        }

        event.setCancelled(true);
        open(event.getPlayer());
    }

    // ==========================================
    // ZAPIS PO ZAMKNIĘCIU
    // ==========================================

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        Inventory inventory = event.getInventory();

        if (!(inventory.getHolder(false) instanceof ChestHolder)) {
            return;
        }

        savePlayer(player.getUniqueId(), inventory);
    }

    // ==========================================
    // ZAPIS PO WYJŚCIU GRACZA
    // ==========================================

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        Inventory inventory = inventories.get(player.getUniqueId());

        if (inventory != null) {
            savePlayer(player.getUniqueId(), inventory);
        }
    }

    // ==========================================
    // DODAWANIE PRZEDMIOTÓW
    // ZWRACA PRZEDMIOTY, KTÓRE SIĘ NIE ZMIEŚCIŁY
    // ==========================================

    public Map<Integer, ItemStack> addItems(
            Player player,
            Collection<ItemStack> items
    ) {
        Inventory inventory = getInventory(player);

        Map<Integer, ItemStack> leftovers = new HashMap<>();

        for (ItemStack item : items) {
            if (item == null || item.getType().isAir()) {
                continue;
            }

            Map<Integer, ItemStack> result =
                    inventory.addItem(item.clone());

            leftovers.putAll(result);
        }

        savePlayer(player.getUniqueId(), inventory);

        return leftovers;
    }

    // ==========================================
    // CZYSZCZENIE ENDER CHESTA
    // ==========================================

    public void clear(Player player) {
        Inventory inventory = getInventory(player);

        inventory.clear();

        savePlayer(player.getUniqueId(), inventory);
    }

    // ==========================================
    // ZAPIS GRACZA
    // ==========================================

    private void savePlayer(UUID uuid, Inventory inventory) {
        String path = uuid.toString();

        config.set(path, null);

        for (int slot = 0; slot < ROZMIAR; slot++) {
            ItemStack item = inventory.getItem(slot);

            if (item != null && !item.getType().isAir()) {
                config.set(path + "." + slot, item);
            }
        }

        saveFile();
    }

    // ==========================================
    // ZAPIS PLIKU
    // ==========================================

    private void saveFile() {
        try {
            config.save(file);
        } catch (IOException exception) {
            plugin.getLogger().severe(
                    "Nie udało się zapisać Ender Chestów: "
                            + exception.getMessage()
            );
        }
    }

    // ==========================================
    // ZAPIS PRZY WYŁĄCZANIU SERWERA
    // ==========================================

    public void saveAll() {
        for (Map.Entry<UUID, Inventory> entry : inventories.entrySet()) {
            savePlayer(entry.getKey(), entry.getValue());
        }
    }
}
