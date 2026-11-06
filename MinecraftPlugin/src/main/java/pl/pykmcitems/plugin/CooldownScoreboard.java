
package pl.pykmcitems.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

public final class CooldownScoreboard implements Listener {

    private static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.legacySection();

    private static final int MAX_COOLDOWN_LINES = 11;
    private static final int MAX_LINES = 15;

    private final JavaPlugin plugin;

    private final Map<UUID, Map<String, CooldownEntry>> cooldowns =
            new HashMap<>();

    private final Set<UUID> disabled = new HashSet<>();

    private final Map<UUID, BoardData> boards = new HashMap<>();

    private final BukkitTask task;

    public CooldownScoreboard(JavaPlugin plugin) {
        this.plugin = plugin;

        Bukkit.getPluginManager().registerEvents(this, plugin);

        for (Player player : Bukkit.getOnlinePlayers()) {
            createBoard(player);
        }

        task = Bukkit.getScheduler().runTaskTimer(
                plugin,
                this::updateAll,
                20L,
                20L
        );
    }

    public void startCooldown(
            Player player,
            String id,
            String displayName,
            long durationMillis
    ) {
        cooldowns.computeIfAbsent(
                player.getUniqueId(),
                ignored -> new HashMap<>()
        ).put(
                id,
                new CooldownEntry(
                        displayName,
                        System.currentTimeMillis() + durationMillis
                )
        );
    }

    public void toggle(Player player) {
        UUID uuid = player.getUniqueId();

        if (disabled.remove(uuid)) {
            createBoard(player);
            player.sendMessage("§aScoreboard został włączony.");
        } else {
            disabled.add(uuid);
            boards.remove(uuid);

            player.setScoreboard(
                    Bukkit.getScoreboardManager().getMainScoreboard()
            );

            player.sendMessage("§cScoreboard został wyłączony.");
        }
    }

    private void createBoard(Player player) {
        UUID uuid = player.getUniqueId();

        if (disabled.contains(uuid)) {
            return;
        }

        Scoreboard board =
                Bukkit.getScoreboardManager().getNewScoreboard();

        Objective objective = board.registerNewObjective(
                "pykmc",
                Criteria.DUMMY,
                LEGACY.deserialize(
                        ChatColor.translateAlternateColorCodes(
                                '&',
                                "&e&lPyk&d&lMC &r&bPractice"
                        )
                )
        );

        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        Team[] teams = new Team[MAX_LINES];
        String[] entries = new String[MAX_LINES];

        for (int i = 0; i < MAX_LINES; i++) {
            String teamName = "pykmcline" + i;

            // Każda linia otrzymuje unikalny, niewidoczny wpis.
            String entry = ChatColor.values()[i].toString();

            Team team = board.registerNewTeam(teamName);
            team.addEntry(entry);

            teams[i] = team;
            entries[i] = entry;
        }

        BoardData data = new BoardData(
                board,
                objective,
                teams,
                entries
        );

        boards.put(uuid, data);

        player.setScoreboard(board);

        updatePlayer(player);
    }

    private void updateAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();

            if (disabled.contains(uuid)) {
                continue;
            }

            if (!boards.containsKey(uuid)) {
                createBoard(player);
            }

            updatePlayer(player);
        }
    }

    private void updatePlayer(Player player) {
        UUID uuid = player.getUniqueId();

        if (disabled.contains(uuid)) {
            return;
        }

        BoardData data = boards.get(uuid);

        if (data == null) {
            return;
        }

        // Przywrócenie scoreboardu, jeśli inny plugin go nadpisał.
        if (player.getScoreboard() != data.board) {
            player.setScoreboard(data.board);
        }

        long now = System.currentTimeMillis();

        Map<String, CooldownEntry> playerCooldowns =
                cooldowns.computeIfAbsent(
                        uuid,
                        ignored -> new HashMap<>()
                );

        playerCooldowns.entrySet().removeIf(
                entry -> entry.getValue().endTime <= now
        );

        List<String> lines = new ArrayList<>();

        lines.add("&7Komendy:");
        lines.add("&7/losuj-eve");
        lines.add("&7/losuj-zwoje");
        lines.add("&7/kit");
        lines.add(" ");
        lines.add("&7Wył. &escoreboard &e/sb");
        lines.add(" ");

        List<CooldownEntry> active =
                new ArrayList<>(playerCooldowns.values());

        active.sort(
                Comparator.comparingLong(entry -> entry.endTime)
        );

        for (int i = 0; i < Math.min(
                MAX_COOLDOWN_LINES,
                active.size()
        ); i++) {

            CooldownEntry entry = active.get(i);

            long seconds = Math.max(
                    0,
                    (entry.endTime - now + 999) / 1000
            );

            String time = String.format(
                    Locale.ROOT,
                    "%02d:%02d",
                    seconds / 60,
                    seconds % 60
            );

            lines.add(
                    entry.displayName + "&7: " + time
            );
        }

        // Aktualizujemy istniejące drużyny zamiast usuwać je i tworzyć ponownie.
        for (int i = 0; i < MAX_LINES; i++) {
            Team team = data.teams[i];
            String entry = data.entries[i];

            if (i < lines.size()) {
                team.prefix(
                        LEGACY.deserialize(
                                ChatColor.translateAlternateColorCodes(
                                        '&',
                                        lines.get(i)
                                )
                        )
                );

                data.objective.getScore(entry).setScore(
                        MAX_LINES - i
                );
            } else {
                team.prefix(Component.empty());
                data.board.resetScores(entry);
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            if (disabled.contains(player.getUniqueId())) return;

            createBoard(player);
        }, 2L);
    }
    
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        boards.remove(uuid);
        disabled.remove(uuid);
        cooldowns.remove(uuid);
    }

    public void shutdown() {
        task.cancel();

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(
                    Bukkit.getScoreboardManager().getMainScoreboard()
            );
        }

        boards.clear();
        cooldowns.clear();
    }

    private record CooldownEntry(
            String displayName,
            long endTime
    ) {}

    private record BoardData(
            Scoreboard board,
            Objective objective,
            Team[] teams,
            String[] entries
    ) {}
}
