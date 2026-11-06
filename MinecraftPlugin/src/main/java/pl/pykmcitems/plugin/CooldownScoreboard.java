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
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();
    private static final int MAX_COOLDOWN_LINES = 11; // Minecraft sidebar ma limit 15 linii.

    private final JavaPlugin plugin;
    private final Map<UUID, Map<String, CooldownEntry>> cooldowns = new HashMap<>();
    private final Set<UUID> disabled = new HashSet<>();
    private final Map<UUID, Scoreboard> boards = new HashMap<>();
    private final BukkitTask task;

    public CooldownScoreboard(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        for (Player player : Bukkit.getOnlinePlayers()) createBoard(player);
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::updateAll, 20L, 20L);
    }

    public void startCooldown(Player player, String id, String displayName, long durationMillis) {
        cooldowns.computeIfAbsent(player.getUniqueId(), ignored -> new HashMap<>())
                .put(id, new CooldownEntry(displayName, System.currentTimeMillis() + durationMillis));
    }

    public void toggle(Player player) {
        UUID uuid = player.getUniqueId();
        if (disabled.remove(uuid)) {
            createBoard(player);
            player.sendMessage("§aScoreboard został włączony.");
        } else {
            disabled.add(uuid);
            boards.remove(uuid);
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            player.sendMessage("§cScoreboard został wyłączony.");
        }
    }

    private void createBoard(Player player) {
        if (disabled.contains(player.getUniqueId())) return;
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = board.registerNewObjective("pykmc", Criteria.DUMMY,
                LEGACY.deserialize(ChatColor.translateAlternateColorCodes('&', "&e&lPyk&dMC &r&bPractice")));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        boards.put(player.getUniqueId(), board);
        player.setScoreboard(board);
        updatePlayer(player);
    }

    private void updateAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!disabled.contains(player.getUniqueId()) && !boards.containsKey(player.getUniqueId())) createBoard(player);
            updatePlayer(player);
        }
    }

    private void updatePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        if (disabled.contains(uuid)) return;
        Scoreboard board = boards.get(uuid);
        if (board == null) return;
        Objective objective = board.getObjective("pykmc");
        if (objective == null) return;

        for (String entry : new HashSet<>(board.getEntries())) board.resetScores(entry);
        for (Team team : new ArrayList<>(board.getTeams())) team.unregister();

        List<String> lines = new ArrayList<>();
        lines.add("&7Serwer practice zrobiony przez pawelek");
        lines.add("&7żeby wyłączyć &escoreboard &7wpisz &e/sb");
        lines.add(" ");
        lines.add("&6&lcooldowny");

        Map<String, CooldownEntry> playerCooldowns = cooldowns.getOrDefault(uuid, Collections.emptyMap());
        long now = System.currentTimeMillis();
        playerCooldowns.entrySet().removeIf(entry -> entry.getValue().endTime <= now);
        List<CooldownEntry> active = new ArrayList<>(playerCooldowns.values());
        active.sort(Comparator.comparingLong(entry -> entry.endTime));
        for (int i = 0; i < Math.min(MAX_COOLDOWN_LINES, active.size()); i++) {
            CooldownEntry entry = active.get(i);
            long seconds = Math.max(0, (entry.endTime - now + 999) / 1000);
            lines.add(entry.displayName + "&7: " + String.format(Locale.ROOT, "%02d:%02d", seconds / 60, seconds % 60));
        }

        int score = lines.size();
        int index = 0;
        for (String line : lines) {
            String entry = ChatColor.values()[index++].toString();
            Team team = board.registerNewTeam("line" + index);
            team.addEntry(entry);
            team.prefix(LEGACY.deserialize(ChatColor.translateAlternateColorCodes('&', line)));
            objective.getScore(entry).setScore(score--);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!disabled.contains(event.getPlayer().getUniqueId())) createBoard(event.getPlayer());
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
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
        boards.clear();
        cooldowns.clear();
    }

    private record CooldownEntry(String displayName, long endTime) {}
}
