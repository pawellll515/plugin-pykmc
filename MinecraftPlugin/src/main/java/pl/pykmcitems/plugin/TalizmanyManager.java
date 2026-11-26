package pl.pykmcitems.plugin;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class TalizmanyManager {

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
        if (item == null || !item.hasItemMeta()) return false;

        String id = item.getItemMeta()
                .getPersistentDataContainer()
                .get(idKey, PersistentDataType.STRING);

        if (id == null) return false;

        UUID uuid = player.getUniqueId();
        List<ItemStack> list = equipped.computeIfAbsent(
                uuid, ignored -> new ArrayList<>()
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

        return true;
    }

    public boolean unequip(Player player, int slot) {
        List<ItemStack> list = equipped.get(player.getUniqueId());

        if (list == null || slot < 0 || slot >= list.size()) {
            return false;
        }

        list.remove(slot);
        save();
        updateAllIcons();

        return true;
    }

    public List<ItemStack> getEquipped(Player player) {
        List<ItemStack> list = equipped.get(player.getUniqueId());

        if (list == null) return new ArrayList<>();

        return new ArrayList<>(list);
    }

    public double getValue(Player player, String id) {
        for (ItemStack item : getEquipped(player)) {
            if (id.equals(getId(item))) {
                return item.getItemMeta()
                        .getPersistentDataContainer()
                        .getOrDefault(valueKey, PersistentDataType.DOUBLE, 0.0);
            }
        }

        return 0.0;
    }

    public boolean has(Player player, String id) {
        return getEquipped(player).stream()
                .anyMatch(item -> id.equals(getId(item)));
    }

    public void clear(Player player, boolean drop) {
        List<ItemStack> list = equipped.remove(player.getUniqueId());

        if (list != null && drop) {
            for (ItemStack item : list) {
                player.getWorld().dropItemNaturally(
                        player.getLocation(), item.clone()
                );
            }
        }

        save();
        updateAllIcons();
    }

    public void updateAllIcons() {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            Scoreboard board = viewer.getScoreboard();

            for (Player target : Bukkit.getOnlinePlayers()) {
                String teamName = "taliz" + target.getEntityId();

                Team team = board.getTeam(teamName);

                if (team == null) {
                    team = board.registerNewTeam(teamName);
                }

                if (!team.hasEntry(target.getName())) {
                    team.addEntry(target.getName());
                }

                StringBuilder icons = new StringBuilder();

                for (ItemStack item : getEquipped(target)) {
                    if (item.hasItemMeta()
                            && item.getItemMeta().hasDisplayName()) {
                        String name = item.getItemMeta().getDisplayName();

                        if (name.length() >= 2) {
                            icons.append(name, 0, 2);
                        }
                    }
                }

                team.setSuffix(org.bukkit.ChatColor.translateAlternateColorCodes(
                        '&', " " + icons
                ));
            }
        }
    }

    private String getId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;

        return item.getItemMeta()
                .getPersistentDataContainer()
                .get(idKey, PersistentDataType.STRING);
    }

    private String getFamily(String id) {
        if (id.startsWith("odpornosc_")) return "odpornosc";
        if (id.startsWith("sila_")) return "sila";

        return id;
    }

    private void load() {
        for (String key : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                List<?> stored = config.getList(key);

                if (stored == null) continue;

                List<ItemStack> items = new ArrayList<>();

                for (Object object : stored) {
                    if (object instanceof ItemStack item) {
                        items.add(item);
                    }
                }

                equipped.put(uuid, items);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public void save() {
        config.getKeys(false).forEach(config::set);

        for (Map.Entry<UUID, List<ItemStack>> entry : equipped.entrySet()) {
            config.set(entry.getKey().toString(), entry.getValue());
        }

        try {
            config.save(file);
        } catch (IOException exception) {
            plugin.getLogger().severe(
                    "Nie udało się zapisać talizmanów: " + exception.getMessage()
            );
        }
    }
}
