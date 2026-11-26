
package pl.pykmcitems.plugin;

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

    // UUID gracza -> ID przedmiotu -> koniec cooldownu
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public ItemCooldownManager(JavaPlugin plugin, CooldownScoreboard scoreboard) {
        this.plugin = plugin;
        this.scoreboard = scoreboard;

        this.file = new File(plugin.getDataFolder(), "cooldowns.yml");
        this.config = YamlConfiguration.loadConfiguration(file);

        loadCooldowns();
    }

    private void loadCooldowns() {
        cooldowns.clear();

        for (String uuidString : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidString);
                Map<String, Long> playerCooldowns = new HashMap<>();

                for (String itemId : config.getConfigurationSection(uuidString).getKeys(false)) {
                    long endTime = config.getLong(uuidString + "." + itemId);

                    if (endTime > System.currentTimeMillis()) {
                        playerCooldowns.put(itemId, endTime);
                    }
                }

                if (!playerCooldowns.isEmpty()) {
                    cooldowns.put(uuid, playerCooldowns);
                }
            } catch (IllegalArgumentException | NullPointerException exception) {
                plugin.getLogger().warning("Nieprawidlowy wpis cooldownu: " + uuidString);
            }
        }
    }

    /**
     * Zwraca procent skrócenia cooldownu.
     * Na razie 0%, ponieważ system talizmanów nie jest jeszcze podłączony.
     *
     * Później podłączymy tutaj wartość Talizmanu Świątecznego Czasu.
     */
    public double getReductionPercent(Player player) {
        return 0.0;
    }

    /**
     * Próbuje rozpocząć cooldown.
     *
     * Zwraca false, jeśli gracz ma już aktywny cooldown.
     * Zwraca true, jeśli cooldown został rozpoczęty.
     */
    public boolean tryStartCooldown(
            Player player,
            String itemId,
            String displayName,
            long baseDurationMillis
    ) {
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        Map<String, Long> playerCooldowns =
                cooldowns.computeIfAbsent(uuid, key -> new HashMap<>());

        Long currentEnd = playerCooldowns.get(itemId);

        if (currentEnd != null && currentEnd > now) {
            return false;
        }

        double reduction = getReductionPercent(player);
        reduction = Math.max(0.0, Math.min(100.0, reduction));

        long finalDuration = Math.max(
                1L,
                Math.round(baseDurationMillis * (1.0 - reduction / 100.0))
        );

        long endTime = now + finalDuration;

        playerCooldowns.put(itemId, endTime);

        config.set(uuid + "." + itemId, endTime);
        save();

        scoreboard.startCooldown(
                player,
                itemId,
                displayName,
                finalDuration
        );

        return true;
    }

    public boolean isOnCooldown(Player player, String itemId) {
        return getRemainingCooldown(player, itemId) > 0;
    }

    public long getRemainingCooldown(Player player, String itemId) {
        Map<String, Long> playerCooldowns =
                cooldowns.get(player.getUniqueId());

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
            config.set(player.getUniqueId() + "." + itemId, null);

            if (playerCooldowns.isEmpty()) {
                cooldowns.remove(player.getUniqueId());
            }

            save();
            return 0L;
        }

        return remaining;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Map<String, Long> playerCooldowns =
                cooldowns.get(player.getUniqueId());

        if (playerCooldowns == null) {
            return;
        }

        for (Map.Entry<String, Long> entry : playerCooldowns.entrySet()) {
            long remaining = entry.getValue() - System.currentTimeMillis();

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

    private String getDisplayName(String itemId) {
        return switch (itemId) {
            case "koniczynka" -> "&a&lKoniczynka";
            default -> "&f" + itemId;
        };
    }

    public void save() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }

            config.save(file);
        } catch (IOException exception) {
            plugin.getLogger().severe(
                    "Nie udalo sie zapisac cooldownow: " + exception.getMessage()
            );
        }
    }
}
