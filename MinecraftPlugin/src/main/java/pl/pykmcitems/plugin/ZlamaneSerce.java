
package pl.pykmcitems.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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

public class ZlamaneSerce implements Listener {

    private final JavaPlugin plugin;
    private final NamespacedKey itemKey;

    // Cooldown używającego gracza
    private final Set<UUID> cooldowns = new HashSet<>();

    // Blokada pereł: UUID gracza -> pozostałe sekundy
    private final Map<UUID, Integer> pearlBlocks = new HashMap<>();

    // Kupidyni: UUID Vexa -> UUID wybranego celu
    private final Map<UUID, UUID> cupids = new HashMap<>();

    // Czas zniknięcia Kupidyna
    private final Map<UUID, Long> cupidExpiry = new HashMap<>();

    public ZlamaneSerce(JavaPlugin plugin) {
        this.plugin = plugin;
        this.itemKey = new NamespacedKey(plugin, "zlamane_serce");

        Bukkit.getPluginManager().registerEvents(this, plugin);

        startPearlBlockTask();
        startCupidTask();
    }

    // =====================================================
    // TWORZENIE PRZEDMIOTU
    // =====================================================

    public static ItemStack createItem(JavaPlugin plugin) {
        ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Złamane Serce")
                .color(net.kyori.adventure.text.format.TextColor.fromHexString("#E547D0"))
                .decoration(TextDecoration.ITALIC, false));

        meta.setLore(Arrays.asList(
                ChatColor.translateAlternateColorCodes(
                        '&', "&7Po &aPPM &cblokuje &7przeciwnikowi perły na &e10 sekund"
                ),
                ChatColor.translateAlternateColorCodes(
                        '&', "&7oraz przywołuje &c5 kupidynów &7którzy go biją"
                ),
                "",
                ChatColor.translateAlternateColorCodes(
                        '&', "&7Cooldown: &e1 minuta 30 sekund"
                )
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

        Byte value = item.getItemMeta()
                .getPersistentDataContainer()
                .get(itemKey, PersistentDataType.BYTE);

        return value != null && value == (byte) 1;
    }

    // =====================================================
    // UŻYCIE PRZEDMIOTU NA GRACZU
    // =====================================================

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

        if (attacker.equals(target)) {
            return;
        }

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

        // Cooldown 90 sekund
        cooldowns.add(attacker.getUniqueId());
        ((Main) plugin).getCooldownScoreboard().startCooldown(attacker, "zlamane", "&c&lZłamane Serce", 90_000L);

        // Blokada pereł na 10 sekund
        pearlBlocks.put(target.getUniqueId(), 10);

        attacker.sendActionBar(Component.text(
                ChatColor.RED + "" + ChatColor.BOLD
                        + "Złamane Serce " + ChatColor.GRAY
                        + "zablokowało perły!"
        ));

        target.sendActionBar(Component.text(
                ChatColor.RED + "Twoje perły zostały zablokowane na "
                        + ChatColor.YELLOW + "10 sekund!"
        ));

        // Przywołanie Kupidynów
        spawnCupids(target);

        // Koniec cooldownu po 90 sekundach
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            cooldowns.remove(attacker.getUniqueId());
        }, 90 * 20L);
    }

    // =====================================================
    // BLOKOWANIE PEREŁ
    // =====================================================

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

    @EventHandler
    public void onPearlLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof EnderPearl pearl)) {
            return;
        }

        if (!(pearl.getShooter() instanceof Player player)) {
            return;
        }

        if (!pearlBlocks.containsKey(player.getUniqueId())) {
            return;
        }

        event.setCancelled(true);

        player.sendActionBar(Component.text(
                ChatColor.RED + "Nie możesz teraz używać pereł!"
        ));
    }

    // =====================================================
    // PRZYWOŁYWANIE KUPIDYNÓW
    // =====================================================

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

            // Każdy Kupidyn ma przypisany tylko jeden cel
            vex.setTarget(target);

            UUID vexId = vex.getUniqueId();

            cupids.put(vexId, target.getUniqueId());

            // Każdy Kupidyn znika po własnych 30 sekundach
            cupidExpiry.put(
                    vexId,
                    System.currentTimeMillis() + 30_000L
            );
        }
    }

    // =====================================================
    // PILNOWANIE KUPIDYNÓW
    // =====================================================

    private void startCupidTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Iterator<Map.Entry<UUID, UUID>> iterator =
                        cupids.entrySet().iterator();

                while (iterator.hasNext()) {
                    Map.Entry<UUID, UUID> entry = iterator.next();

                    UUID vexId = entry.getKey();
                    UUID targetId = entry.getValue();

                    if (!(Bukkit.getEntity(vexId) instanceof Vex vex)
                            || !vex.isValid()) {
                        cupidExpiry.remove(vexId);
                        iterator.remove();
                        continue;
                    }

                    Player target = Bukkit.getPlayer(targetId);

                    // Cel nie istnieje, umarł lub wyszedł z serwera
                    if (target == null || !target.isOnline() || target.isDead()) {
                        vex.remove();
                        cupidExpiry.remove(vexId);
                        iterator.remove();
                        continue;
                    }

                    Long expiry = cupidExpiry.get(vexId);

                    // Minęło 30 sekund
                    if (expiry == null
                            || System.currentTimeMillis() >= expiry) {
                        vex.remove();
                        cupidExpiry.remove(vexId);
                        iterator.remove();
                        continue;
                    }

                    // Jeśli cel zmienił świat, Kupidyn podąża za nim
                    if (!target.getWorld().equals(vex.getWorld())) {
                        vex.teleport(target.getLocation());
                    }

                    // Przywracanie właściwego celu
                    if (vex.getTarget() == null
                            || !vex.getTarget().getUniqueId().equals(targetId)) {
                        vex.setTarget(target);
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 10L);
    }

    // =====================================================
    // KUPIDYNI NIE MOGĄ ATAKOWAĆ INNYCH GRACZY
    // =====================================================

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

    // =====================================================
    // OBRAŻENIA KUPIDYNÓW - TRUE DAMAGE
    // =====================================================

    @EventHandler
    public void onCupidDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Vex vex)) {
            return;
        }

        UUID targetId = cupids.get(vex.getUniqueId());

        if (targetId == null) {
            return;
        }

        // Kupidyn nie może zranić nikogo poza swoim celem
        if (!(event.getEntity() instanceof Player victim)
                || !victim.getUniqueId().equals(targetId)) {
            event.setCancelled(true);
            return;
        }

        // Zatrzymujemy zwykłe obrażenia Vexa
        event.setCancelled(true);

        if (victim.isDead()) {
            return;
        }

        // 1 punkt zdrowia = 0,5 serca
        // Bezpośrednia zmiana zdrowia omija redukcję pancerza.
        double newHealth = Math.max(0.0, victim.getHealth() - 1.0);
        victim.setHealth(newHealth);
    }

    // =====================================================
    // ODLiczanie BLOKADY PEREŁ
    // =====================================================

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

    // =====================================================
    // ŚMIERĆ GRACZA
    // =====================================================

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

    // =====================================================
    // WYJŚCIE GRACZA
    // =====================================================

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();

        cooldowns.remove(playerId);
        pearlBlocks.remove(playerId);

        removeCupidsFor(playerId);
    }

    // =====================================================
    // USUWANIE KUPIDYNÓW PRZYPISANYCH DO GRACZA
    // =====================================================

    private void removeCupidsFor(UUID targetId) {
        Iterator<Map.Entry<UUID, UUID>> iterator =
                cupids.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, UUID> entry = iterator.next();

            if (entry.getValue().equals(targetId)) {
                UUID vexId = entry.getKey();

                var entity = Bukkit.getEntity(vexId);

                if (entity != null) {
                    entity.remove();
                }

                cupidExpiry.remove(vexId);
                iterator.remove();
            }
        }
    }

    // =====================================================
    // CZYSZCZENIE PRZY WYŁĄCZANIU PLUGINU
    // =====================================================

    public void cleanup() {
        for (UUID vexId : cupids.keySet()) {
            var entity = Bukkit.getEntity(vexId);

            if (entity != null) {
                entity.remove();
            }
        }

        cupids.clear();
        cupidExpiry.clear();
        cooldowns.clear();
        pearlBlocks.clear();
    }
}
