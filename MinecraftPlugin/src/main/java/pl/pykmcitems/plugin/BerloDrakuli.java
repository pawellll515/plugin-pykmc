
package pl.pykmcitems.plugin;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BerloDrakuli implements Listener {

    private final Main plugin;

    private final Map<UUID, Double> originalScales = new HashMap<>();
    private final Map<UUID, BukkitTask> activeTasks = new HashMap<>();
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public BerloDrakuli(Main plugin) {
        this.plugin = plugin;
    }

    public static ItemStack createItem() {
        ItemStack item = new ItemStack(org.bukkit.Material.STICK);
        ItemMeta meta = item.getItemMeta();

        meta.setCustomModelData(1);
        meta.setDisplayName("§x§E§A§0§0§0§0Berło Drakuli");

        meta.setLore(java.util.Arrays.asList(
                "",
                "§7Po użyciu zmniejszasz swoją postać",
                "§7o §e50% §7na §e15s",
                "",
                "§7Cooldown: §e2min"
        ));

        item.setItemMeta(meta);
        return item;
    }

    private boolean isBerlo(ItemStack item) {
        if (item == null || item.getType() != org.bukkit.Material.STICK) {
            return false;
        }

        if (!item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        return meta.hasCustomModelData()
                && meta.getCustomModelData() == 1
                && meta.hasDisplayName()
                && meta.getDisplayName().equals("§x§E§A§0§0§0§0Berło Drakuli");
    }

    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();

        if (!isBerlo(event.getItem())) {
            return;
        }

        UUID uuid = player.getUniqueId();

        if (cooldowns.containsKey(uuid)) {
            long remaining = cooldowns.get(uuid) - System.currentTimeMillis();

            if (remaining > 0) {
                player.sendMessage("§cBerło Drakuli jest jeszcze na cooldownie!");
                return;
            }

            cooldowns.remove(uuid);
        }

        AttributeInstance scale = player.getAttribute(Attribute.SCALE);

        if (scale == null) {
            player.sendMessage("§cNie można zmienić skali Twojej postaci!");
            return;
        }

        originalScales.put(uuid, scale.getBaseValue());
        scale.setBaseValue(0.5);

        cooldowns.put(uuid, System.currentTimeMillis() + 120_000);
        ((Main) plugin).getCooldownScoreboard().startCooldown(player, "berlo", "&x&E&A&0&0&0&0Berło Drakuli", 120_000L);

        player.sendMessage("§x§E§A§0§0§0§0Berło Drakuli §7zmniejszyło Twoją postać!");

        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            resetPlayer(player);
        }, 15 * 20L);

        activeTasks.put(uuid, task);
    }

    private void resetPlayer(Player player) {
        UUID uuid = player.getUniqueId();

        AttributeInstance scale = player.getAttribute(Attribute.SCALE);

        if (scale != null && originalScales.containsKey(uuid)) {
            scale.setBaseValue(originalScales.get(uuid));
        }

        originalScales.remove(uuid);

        BukkitTask task = activeTasks.remove(uuid);

        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }

    private void resetEverything(Player player) {
        UUID uuid = player.getUniqueId();

        resetPlayer(player);
        cooldowns.remove(uuid);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        resetEverything(event.getEntity());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        resetEverything(event.getPlayer());
    }

    public void cleanup() {
        for (UUID uuid : new java.util.HashSet<>(originalScales.keySet())) {
            Player player = plugin.getServer().getPlayer(uuid);

            if (player != null) {
                resetPlayer(player);
            }
        }

        for (BukkitTask task : activeTasks.values()) {
            task.cancel();
        }

        activeTasks.clear();
        originalScales.clear();
        cooldowns.clear();
    }
}
