
package pl.pykmcitems.plugin;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vex;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ZlamaneSerce implements Listener, CommandExecutor {

    private final JavaPlugin plugin;
    private final NamespacedKey itemKey;

    private final Set<UUID> cooldowns = new HashSet<>();
    private final Map<UUID, Integer> pearlBlocks = new HashMap<>();

    // UUID Kupidyna -> UUID jego celu
    private final Map<UUID, UUID> cupids = new HashMap<>();

    public ZlamaneSerce(JavaPlugin plugin) {
        this.plugin = plugin;
        this.itemKey = new NamespacedKey(plugin, "zlamane_serce");

        Bukkit.getPluginManager().registerEvents(this, plugin);

        startCupidTask();
        startPearlBlockTask();
    }

    public static ItemStack createItem(JavaPlugin plugin) {
        ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                "&c&lZłamane Serce"));

        meta.setLore(Arrays.asList(
                ChatColor.translateAlternateColorCodes('&',
                        "&7Po &aPPM &cblokuje &7przeciwnikowi perły na &e10 sekund"),
                ChatColor.translateAlternateColorCodes('&',
                        "&7oraz przywołuje &c5 kupidynów &7którzy go biją"),
                "",
                ChatColor.translateAlternateColorCodes('&',
                        "&7Cooldown: &e1 minuta 30 sekund")
        ));

        meta.setCustomModelData(3);
        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "zlamane_serce"),
                PersistentDataType.BYTE,
                (byte) 1
        );

        item.setItemMeta(meta);
        return item;
    }

    private boolean isZlamaneSerce(ItemStack item) {
        if (item == null || item.getType() != Material.AMETHYST_SHARD) {
            return false;
        }

        if (!item.hasItemMeta()) {
            return false;
        }

        Byte value = item.getItemMeta().getPersistentDataContainer().get(
                itemKey,
                PersistentDataType.BYTE
        );

        return value != null && value == (byte) 1;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Tej komendy może użyć tylko gracz.");
            return true;
        }

        if (!player.hasPermission("pykmcitems.zlamane")) {
            player.sendMessage(ChatColor.RED + "Nie masz uprawnień!");
            return true;
        }

        player.getInventory().addItem(createItem(plugin));
        player.sendMessage(ChatColor.GREEN + "Otrzymałeś Złamane Serce!");
        return true;
    }

    // Użycie PPM na graczu
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        if (!(event.getRightClicked() instanceof Player target)) {
            return;
        }

        Player attacker = event.getPlayer();

        if (!isZlamaneSerce(attacker.getInventory().getItemInMainHand())) {
            return;
        }

        event.setCancelled(true);

        if (cooldowns.contains(attacker.getUniqueId())) {
            attacker.sendActionBar(Component.text(
                    ChatColor.RED + "Złamane Serce jest na cooldownie!"
            ));
            return;
        }

        if (pearlBlocks.containsKey(target.getUniqueId())) {
            attacker.sendActionBar(Component.text(
                    ChatColor.RED + "Ten gracz ma już zablokowane perły!"
            ));
            return;
        }

        if (attacker.equals(target)) {
            return;
        }

        cooldowns.add(attacker.getUniqueId());

        attacker.setCooldown(Material.AMETHYST_SHARD, 90 * 20);

        pearlBlocks.put(target.getUniqueId(), 10);

        attacker.sendActionBar(Component.text(
                ChatColor.RED + "" + ChatColor.BOLD
                        + "Złamane Serce " + ChatColor.GRAY + "zablokowało perły!"
        ));

        target.sendActionBar(Component.text(
                ChatColor.RED + "Twoje perły zostały zablokowane na "
                        + ChatColor.YELLOW + "10 sekund!"
        ));

        spawnCupids(target);

        // Cooldown 90 sekund
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            cooldowns.remove(attacker.getUniqueId());
        }, 90 * 20L);
    }

    // Blokowanie używania pereł
    @EventHandler
    public void onPearlUse(PlayerInteractEvent event) {
        if (event.getItem() == null
                || event.getItem().getType() != Material.ENDER_PEARL) {
            return;
        }

        Player player = event.getPlayer();

        if (!pearlBlocks.containsKey(player.getUniqueId())) {
            return;
        }

        event.setCancelled(true);

        player.sendActionBar(Component.text(
                ChatColor.RED + "Nie możesz teraz używać pereł!"
        ));
    }

    // Dodatkowe zabezpieczenie przed wyrzuceniem perły
    @EventHandler
    public void onPearlLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof EnderPearl pearl)) {
            return;
        }

        if (!(pearl.getShooter() instanceof Player player)) {
            return;
        }

        if (pearlBlocks.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendActionBar(Component.text(
                    ChatColor.RED + "Nie możesz teraz używać pereł!"
            ));
        }
    }

    // Przywołanie pięciu Kupidynów wokół celu
    private void spawnCupids(Player target) {
        double radius = 1.0;

        for (int i = 0; i < 5; i++) {
            double angle = (2 * Math.PI * i) / 5;

            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            var location = target.getLocation().clone().add(x, 0, z);

            Vex vex = target.getWorld().spawn(location, Vex.class);

            vex.setCustomName(ChatColor.RED + "Kupidyn");
            vex.setCustomNameVisible(true);
            vex.setPersistent(false);
            vex.setRemoveWhenFarAway(false);
            vex.setTarget(target);

            cupids.put(vex.getUniqueId(), target.getUniqueId());
        }
    }

    // Pilnowanie celu Kupidynów i ich czasu życia
    private void startCupidTask() {
        new BukkitRunnable() {
            private int seconds = 0;

            @Override
            public void run() {
                seconds++;

                Iterator<Map.Entry<UUID, UUID>> iterator =
                        cupids.entrySet().iterator();

                while (iterator.hasNext()) {
                    Map.Entry<UUID, UUID> entry = iterator.next();

                    UUID vexId = entry.getKey();
                    UUID targetId = entry.getValue();

                    Vex vex = (Vex) Bukkit.getEntity(vexId);
                    Player target = Bukkit.getPlayer(targetId);

                    if (vex == null || !vex.isValid()) {
                        iterator.remove();
                        continue;
                    }

                    if (target == null || !target.isOnline()
                            || target.isDead()) {
                        vex.remove();
                        iterator.remove();
                        continue;
                    }

                    if (seconds >= 30) {
                        vex.remove();
                        iterator.remove();
                        continue;
                    }

                    if (!target.getWorld().equals(vex.getWorld())) {
                        vex.teleport(target.getLocation());
                    }

                    if (vex.getTarget() == null
                            || !vex.getTarget().getUniqueId().equals(targetId)) {
                        vex.setTarget(target);
                    }
                }

                if (cupids.isEmpty() && seconds >= 30) {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    // Kupidyni nie mogą wybierać innych graczy
    @EventHandler
    public void onCupidTarget(EntityTargetLivingEntityEvent event) {
        if (!(event.getEntity() instanceof Vex vex)) {
            return;
        }

        UUID targetId = cupids.get(vex.getUniqueId());

        if (targetId == null) {
            return;
        }

        if (!(event.getTarget() instanceof Player target)
                || !target.getUniqueId().equals(targetId)) {
            event.setCancelled(true);
        }
    }

    // 0.5 serca true damage za trafienie Kupidyna
    @EventHandler
    public void onCupidDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Vex vex)) {
            return;
        }

        UUID targetId = cupids.get(vex.getUniqueId());

        if (targetId == null) {
            return;
        }

        if (!(event.getEntity() instanceof Player victim)
                || !victim.getUniqueId().equals(targetId)) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);

        // Bezpośrednie odjęcie 1 punktu zdrowia omija pancerz.
        if (!victim.isDead()) {
            double newHealth = Math.max(0.0, victim.getHealth() - 1.0);
            victim.setHealth(newHealth);
        }
    }

    // Odliczanie blokady pereł
    private void startPearlBlockTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Iterator<Map.Entry<UUID, Integer>> iterator =
                        pearlBlocks.entrySet().iterator();

                while (iterator.hasNext()) {
                    Map.Entry<UUID, Integer> entry = iterator.next();

                    UUID playerId = entry.getKey();
                    int remaining = entry.getValue() - 1;

                    Player player = Bukkit.getPlayer(playerId);

                    if (remaining <= 0 || player == null || !player.isOnline()) {
                        iterator.remove();

                        if (player != null && player.isOnline()) {
                            player.sendActionBar(Component.text(
                                    ChatColor.GREEN
                                            + "Możesz ponownie używać pereł!"
                            ));
                        }
                    } else {
                        entry.setValue(remaining);
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    // Czyszczenie po śmierci gracza
    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        UUID playerId = player.getUniqueId();

        cooldowns.remove(playerId);
        pearlBlocks.remove(playerId);
        removeCupidsFor(playerId);
    }

    // Czyszczenie po wyjściu gracza
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();

        cooldowns.remove(playerId);
        pearlBlocks.remove(playerId);
        removeCupidsFor(playerId);
    }

    private void removeCupidsFor(UUID targetId) {
        Iterator<Map.Entry<UUID, UUID>> iterator =
                cupids.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, UUID> entry = iterator.next();

            if (entry.getValue().equals(targetId)) {
                var entity = Bukkit.getEntity(entry.getKey());

                if (entity != null) {
                    entity.remove();
                }

                iterator.remove();
            }
        }
    }

    public void cleanup() {
        for (UUID vexId : cupids.keySet()) {
            var entity = Bukkit.getEntity(vexId);

            if (entity != null) {
                entity.remove();
            }
        }

        cupids.clear();
        cooldowns.clear();
        pearlBlocks.clear();
    }
}
