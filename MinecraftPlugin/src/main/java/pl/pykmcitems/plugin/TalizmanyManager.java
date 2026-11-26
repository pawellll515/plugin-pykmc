
package pl.pykmcitems.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class TalizmanyManager implements Listener {

    private static final int MAX_TALIZMANOW = 3;

    private final JavaPlugin plugin;
    private final NamespacedKey idKey;
    private final NamespacedKey valueKey;
    private final File file;
    private final YamlConfiguration config;

    private final Map<UUID, List<ItemStack>> equipped = new HashMap<>();

    public TalizmanyManager(JavaPlugin plugin) {
        this.plugin = plugin;

        this.idKey = new NamespacedKey(plugin, "talizman_id");
        this.valueKey = new NamespacedKey(plugin, "talizman_wartosc");

        this.file = new File(plugin.getDataFolder(), "talizmany.yml");
        this.config = YamlConfiguration.loadConfiguration(file);

        load();
    }

    public boolean equip(Player player, ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        String id = getId(item);
        if (id == null) return false;

        List<ItemStack> list = equipped.computeIfAbsent(
                player.getUniqueId(),
                ignored -> new ArrayList<>()
        );

        if (list.size() >= MAX_TALIZMANOW) {
            player.sendMessage("§cMożesz mieć maksymalnie 3 talizmany!");
            return false;
        }

        String family = getFamily(id);

        for (ItemStack current : list) {
            String currentId = getId(current);

            if (currentId != null && getFamily(currentId).equals(family)) {
                player.sendMessage("§cMasz już założony talizman z tej rodziny!");
                return false;
            }
        }

        list.add(item.clone());

        save();
        updateAllIcons();

        player.sendMessage("§aZałożono talizman!");
        return true;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        if (event.getAction() != Action.LEFT_CLICK_AIR
                && event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        String id = getId(item);
        if (id == null) return;

        if (!equip(player, item)) {
            event.setCancelled(true);
            return;
        }

        if (item.getAmount() <= 1) {
            player.getInventory().setItemInMainHand(null);
        } else {
            item.setAmount(item.getAmount() - 1);
        }

        player.playSound(
                player.getLocation(),
                Sound.BLOCK_VAULT_ACTIVATE,
                1.0f,
                1.0f
        );

        event.setCancelled(true);
    }

    public boolean unequip(Player player, int slot) {
        List<ItemStack> list = equipped.get(player.getUniqueId());

        if (list == null || slot < 0 || slot >= list.size()) {
            return false;
        }

        ItemStack item = list.remove(slot).clone();

        if (list.isEmpty()) {
            equipped.remove(player.getUniqueId());
        }

        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(item);

        for (ItemStack leftover : leftovers.values()) {
            player.getWorld().dropItemNaturally(
                    player.getLocation(),
                    leftover
            );
        }

        save();
        updateAllIcons();

        player.playSound(
                player.getLocation(),
                Sound.BLOCK_VAULT_DEACTIVATE,
                1.0f,
                1.0f
        );

        player.sendMessage("§aZdjęto talizman! §7Wrócił do twojego ekwipunku.");

        return true;
    }

    public List<ItemStack> getEquipped(Player player) {
        List<ItemStack> list = equipped.get(player.getUniqueId());

        if (list == null) {
            return new ArrayList<>();
        }

        List<ItemStack> copy = new ArrayList<>();

        for (ItemStack item : list) {
            copy.add(item.clone());
        }

        return copy;
    }

    public double getValue(Player player, String id) {
        for (ItemStack item : getEquipped(player)) {
            if (id.equals(getId(item))) {
                return item.getItemMeta()
                        .getPersistentDataContainer()
                        .getOrDefault(
                                valueKey,
                                PersistentDataType.DOUBLE,
                                0.0
                        );
            }
        }

        return 0.0;
    }

    public boolean has(Player player, String id) {
        return getEquipped(player)
                .stream()
                .anyMatch(item -> id.equals(getId(item)));
    }

    public void clear(Player player, boolean drop) {
        List<ItemStack> list = equipped.remove(player.getUniqueId());

        if (list != null && drop) {
            for (ItemStack item : list) {
                player.getWorld().dropItemNaturally(
                        player.getLocation(),
                        item.clone()
                );
            }
        }

        save();
        updateAllIcons();
    }

    private Component getIcon(String id) {
        if (id == null) return null;

        if (id.startsWith("odpornosc_")) {
            return Component.text("🛡", NamedTextColor.AQUA);
        }

        if (id.startsWith("sila_")) {
            return Component.text("⚔", NamedTextColor.LIGHT_PURPLE);
        }

        if (id.equals("slodkosci_3")) {
            return Component.text("🍬", NamedTextColor.AQUA);
        }

        if (id.equals("drop")) {
            return Component.text("✉", NamedTextColor.AQUA);
        }

        if (id.equals("swiateczny_czas")) {
            return Component.text("❄", NamedTextColor.WHITE);
        }

        return null;
    }

    private String getTeamName(Player target) {
        return "taliz" + target.getEntityId();
    }

    public void updateViewerBoard(Player viewer) {
        Scoreboard board = viewer.getScoreboard();

        for (Player target : Bukkit.getOnlinePlayers()) {
            String teamName = getTeamName(target);
            Team team = board.getTeam(teamName);

            Component icons = Component.empty();
            boolean first = true;

            for (ItemStack item : getEquipped(target)) {
                Component icon = getIcon(getId(item));

                if (icon == null) continue;

                if (!first) {
                    icons = icons.append(Component.text(" "));
                }

                icons = icons.append(icon);
                first = false;
            }

            if (first) {
                if (team != null) {
                    team.removeEntry(target.getName());
                    team.unregister();
                }

                continue;
            }

            if (team == null) {
                team = board.registerNewTeam(teamName);
            }

            if (!team.hasEntry(target.getName())) {
                team.addEntry(target.getName());
            }

            team.prefix(icons.append(Component.text(" ")));
        }
    }

    public void updateAllIcons() {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            updateViewerBoard(viewer);
        }
    }

    private String getId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }

        return item.getItemMeta()
                .getPersistentDataContainer()
                .get(idKey, PersistentDataType.STRING);
    }

    private String getFamily(String id) {
        if (id.startsWith("odpornosc_")) {
            return "odpornosc";
        }

        if (id.startsWith("sila_")) {
            return "sila";
        }

        return id;
    }

    private void load() {
        equipped.clear();

        for (String key : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                List<?> stored = config.getList(key);

                if (stored == null) continue;

                List<ItemStack> items = new ArrayList<>();

                for (Object object : stored) {
                    if (object instanceof ItemStack item && getId(item) != null) {
                        items.add(item.clone());
                    }
                }

                if (!items.isEmpty()) {
                    equipped.put(uuid, items);
                }

            } catch (IllegalArgumentException ignored) {
                plugin.getLogger().warning(
                        "Nieprawidłowy UUID talizmanów: " + key
                );
            }
        }
    }

    public void save() {
        for (String key : new HashSet<>(config.getKeys(false))) {
            config.set(key, null);
        }

        for (Map.Entry<UUID, List<ItemStack>> entry : equipped.entrySet()) {
            config.set(entry.getKey().toString(), entry.getValue());
        }

        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }

            config.save(file);

        } catch (IOException exception) {
            plugin.getLogger().severe(
                    "Nie udało się zapisać talizmanów: "
                            + exception.getMessage()
            );
        }
    }
}
