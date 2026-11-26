
package pl.pykmcitems.plugin;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ItemCooldownManager implements Listener {

    private final JavaPlugin plugin;
    private final CooldownScoreboard scoreboard;

    private final File file;
    private final YamlConfiguration config;

    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public ItemCooldownManager(JavaPlugin plugin, CooldownScoreboard scoreboard) {
        this.plugin = plugin;
        this.scoreboard = scoreboard;

        this.file = new File(plugin.getDataFolder(), "cooldowns.yml");
        this.config = YamlConfiguration.loadConfiguration(file);

        loadCooldowns();
    }

    // =====================================================
    // WCZYTYWANIE COOLDOWNÓW
    // =====================================================

    private void loadCooldowns() {
        cooldowns.clear();

        long now = System.currentTimeMillis();
        boolean changed = false;

        for (String uuidString : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidString);

                ConfigurationSection section =
                        config.getConfigurationSection(uuidString);

                if (section == null) {
                    continue;
                }

                Map<String, Long> playerCooldowns = new HashMap<>();

                for (String itemId : section.getKeys(false)) {
                    long endTime = config.getLong(
                            uuidString + "." + itemId
                    );

                    if (endTime > now) {
                        playerCooldowns.put(itemId, endTime);
                    } else {
                        config.set(uuidString + "." + itemId, null);
                        changed = true;
                    }
                }

                if (!playerCooldowns.isEmpty()) {
                    cooldowns.put(uuid, playerCooldowns);
                } else {
                    config.set(uuidString, null);
                    changed = true;
                }

            } catch (IllegalArgumentException exception) {
                plugin.getLogger().warning(
                        "Nieprawidlowy wpis cooldownu: " + uuidString
                );
            }
        }

        if (changed) {
            save();
        }
    }

    // =====================================================
    // REDUKCJA COOLDOWNÓW Z TALIZMANU
    // =====================================================

    public double getReductionPercent(Player player) {
        if (!(plugin instanceof Main main)) {
            return 0.0;
        }

        TalizmanyManager manager = main.getTalizmanyManager();

        if (manager == null) {
            return 0.0;
        }

        return manager.getValue(player, "swiateczny_czas");
    }

    // =====================================================
    // ROZPOCZYNANIE COOLDOWNU
    // =====================================================

    public boolean tryStartCooldown(
            Player player,
            String itemId,
            String displayName,
            long baseDurationMillis
    ) {
        if (isOnCooldown(player, itemId)) {
            return false;
        }

        setCooldown(
                player,
                itemId,
                displayName,
                baseDurationMillis
        );

        return true;
    }

    // =====================================================
    // USTAWIANIE / NADPISYWANIE COOLDOWNU
    // =====================================================

    public void setCooldown(
            Player player,
            String itemId,
            String displayName,
            long baseDurationMillis
    ) {
        UUID uuid = player.getUniqueId();

        double reduction = getReductionPercent(player);
        reduction = Math.max(0.0, Math.min(100.0, reduction));

        long finalDuration = Math.max(
                1L,
                Math.round(
                        baseDurationMillis * (1.0 - reduction / 100.0)
                )
        );

        long endTime = System.currentTimeMillis() + finalDuration;

        Map<String, Long> playerCooldowns =
                cooldowns.computeIfAbsent(
                        uuid,
                        key -> new HashMap<>()
                );

        playerCooldowns.put(itemId, endTime);

        config.set(uuid + "." + itemId, endTime);
        save();

        scoreboard.startCooldown(
                player,
                itemId,
                displayName,
                finalDuration
        );
    }

    // =====================================================
    // SPRAWDZANIE COOLDOWNU
    // =====================================================

    public boolean isOnCooldown(Player player, String itemId) {
        return getRemainingCooldown(player, itemId) > 0;
    }

    public long getRemainingCooldown(Player player, String itemId) {
        UUID uuid = player.getUniqueId();

        Map<String, Long> playerCooldowns = cooldowns.get(uuid);

        if (playerCooldowns == null) {
            return 0L;
        }

        Long endTime = playerCooldowns.get(itemId);

        if (endTime == null) {
            return 0L;
        }

        long remaining = endTime - System.currentTimeMillis();

        if (remaining <= 0) {
            playerCooldowns.remove(itemId);
            config.set(uuid + "." + itemId, null);

            if (playerCooldowns.isEmpty()) {
                cooldowns.remove(uuid);
            }

            save();
            return 0L;
        }

        return remaining;
    }

    // =====================================================
    // PRZYWRACANIE COOLDOWNÓW PO WEJŚCIU
    // =====================================================

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Map<String, Long> playerCooldowns =
                cooldowns.get(player.getUniqueId());

        if (playerCooldowns == null) {
            return;
        }

        long now = System.currentTimeMillis();

        for (Map.Entry<String, Long> entry : playerCooldowns.entrySet()) {
            long remaining = entry.getValue() - now;

            if (remaining > 0) {
                scoreboard.startCooldown(
                        player,
                        entry.getKey(),
                        getDisplayName(entry.getKey()),
                        remaining
                );
            }
        }
    }

    // =====================================================
    // NAZWY PRZEDMIOTÓW NA SCOREBOARDZIE
    // =====================================================

    private String getDisplayName(String itemId) {
        return switch (itemId) {
            case "koniczynka" -> "&a&lKoniczynka";
            case "rog" -> "&b&lRóg Niebios";
            case "glodomiot" -> "&6&lGłodomiot";
            case "rozpadacz" -> "&4&lRozpadacz";
            case "rozga" -> "&7&lRózga";
            case "katana" -> "&a&lŚwiąteczna Katana";
            case "topor" -> "&6&lTopór Wykluczenia";
            case "lodolap" -> "&b&lLodołap";
            case "buda" -> "&e&lBuda";
            case "zlamane" -> "&c&lZłamane Serce";
            case "berlo" -> "&x&E&A&0&0&0&0Berło Drakuli";
            case "luk_wiedzmy" -> "&dŁuk Wiedźmy";
            default -> "&f" + itemId;
        };
    }

    // =====================================================
    // ZAPIS DO PLIKU
    // =====================================================

    public void save() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }

            config.save(file);

        } catch (IOException exception) {
            plugin.getLogger().severe(
                    "Nie udalo sie zapisac cooldownow: "
                            + exception.getMessage()
            );
        }
    }
}
