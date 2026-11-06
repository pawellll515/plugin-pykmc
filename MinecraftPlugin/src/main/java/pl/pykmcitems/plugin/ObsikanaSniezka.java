
package pl.pykmcitems.plugin;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ObsikanaSniezka implements Listener {

    private final JavaPlugin plugin;
    private final NamespacedKey itemKey;

    private final Set<UUID> naszeSniezki = new HashSet<>();

    private final Map<UUID, BukkitTask> zamrozeni = new HashMap<>();
    private final Map<UUID, Long> cooldowny = new HashMap<>();

    private static final long COOLDOWN = 5_000L;
    private static final int CZAS_ZAMROZENIA = 50;

    public ObsikanaSniezka(JavaPlugin plugin) {
        this.plugin = plugin;
        this.itemKey = new NamespacedKey(plugin, "obsikana_sniezka");
    }

    // Rzucanie Obsikanej Śnieżki
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {

        if (event.getHand() != EquipmentSlot.HAND) return;

        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || !item.hasItemMeta()) return;

        if (!item.getItemMeta().getPersistentDataContainer()
                .has(itemKey, PersistentDataType.BYTE)) {
            return;
        }

        // Sprawdzanie cooldownu
        long teraz = System.currentTimeMillis();
        long koniec = cooldowny.getOrDefault(player.getUniqueId(), 0L);

        if (teraz < koniec) {
            long pozostalo = (koniec - teraz + 999) / 1000;

            player.sendMessage(
                    "§cMusisz poczekać jeszcze §e" + pozostalo + "s§c!"
            );

            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);

        // Rzucenie śnieżki
        Snowball snowball = player.launchProjectile(Snowball.class);

        // Zapamiętanie UUID naszej śnieżki
        naszeSniezki.add(snowball.getUniqueId());

        // Cooldown 5 sekund po rzuceniu
        cooldowny.put(player.getUniqueId(), teraz + COOLDOWN);
        ((Main) plugin).getCooldownScoreboard().startCooldown(player, "obsikana", "&eObsikana Śnieżka", COOLDOWN);

        // Zużycie jednej śnieżki
        if (item.getAmount() <= 1) {
            player.getInventory().setItemInMainHand(null);
        } else {
            item.setAmount(item.getAmount() - 1);
        }
    }

    // Wykrywanie trafienia
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {

        if (!(event.getEntity() instanceof Snowball snowball)) return;

        // Sprawdzamy, czy to nasza śnieżka
        if (!naszeSniezki.remove(snowball.getUniqueId())) return;

        // Sprawdzamy, czy trafiono gracza
        if (!(event.getHitEntity() instanceof Player target)) return;

        // Nie zamrażamy rzucającego
        if (snowball.getShooter() instanceof Player shooter
                && shooter.getUniqueId().equals(target.getUniqueId())) {
            return;
        }

        zamroz(target);
    }

    // Zamrażanie gracza
    private void zamroz(Player target) {

        UUID uuid = target.getUniqueId();

        // Usuwanie poprzedniego efektu
        BukkitTask previous = zamrozeni.remove(uuid);

        if (previous != null) {
            previous.cancel();
        }

        target.setVelocity(new Vector(0, 0, 0));
        target.setFallDistance(0);
        target.clearActiveItem();

        target.sendMessage("§bZostałeś zamrożony na §e2.5 sekundy§b!");

        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(
                plugin,
                new Runnable() {

                    int tick = 0;

                    @Override
                    public void run() {

                        if (!target.isOnline() || target.isDead()) {
                            zakoncz(target, false);
                            return;
                        }

                        // Zatrzymanie ruchu
                        target.setVelocity(new Vector(0, 0, 0));
                        target.setFallDistance(0);

                        // Przerywanie jedzenia
                        target.clearActiveItem();

                        tick++;

                        // 50 ticków = 2.5 sekundy
                        if (tick >= CZAS_ZAMROZENIA) {
                            zakoncz(target, true);
                        }
                    }
                },
                0L,
                1L
        );

        zamrozeni.put(uuid, task);
    }

    // Blokowanie poruszania się
    @EventHandler
    public void onMove(PlayerMoveEvent event) {

        Player player = event.getPlayer();

        if (!zamrozeni.containsKey(player.getUniqueId())) return;

        if (event.getTo() == null) return;

        // Blokujemy zmianę pozycji, ale pozwalamy obracać kamerą
        if (event.getFrom().getX() != event.getTo().getX()
                || event.getFrom().getY() != event.getTo().getY()
                || event.getFrom().getZ() != event.getTo().getZ()) {

            event.setTo(event.getFrom().clone());
        }
    }

    // Blokowanie jedzenia
    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {

        if (!zamrozeni.containsKey(event.getPlayer().getUniqueId())) {
            return;
        }

        event.setCancelled(true);
        event.getPlayer().clearActiveItem();
    }

    // Blokowanie rozpoczynania jedzenia
    @EventHandler
    public void onEatInteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();

        if (!zamrozeni.containsKey(player.getUniqueId())) return;

        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();

        if (item != null && item.getType().isEdible()) {
            event.setCancelled(true);
            player.clearActiveItem();
        }
    }

    // Zakończenie zamrożenia
    private void zakoncz(Player target, boolean notify) {

        UUID uuid = target.getUniqueId();

        BukkitTask task = zamrozeni.remove(uuid);

        if (task != null) {
            task.cancel();
        }

        if (notify && target.isOnline()) {
            target.sendMessage("§aEfekt zamrożenia zakończył się!");
        }
    }

    // Wyjście z serwera
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        zakoncz(event.getPlayer(), false);
    }

    // Śmierć gracza
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        zakoncz(event.getEntity(), false);
    }

    // Czyszczenie przy wyłączeniu pluginu
    public void cleanup() {

        for (BukkitTask task : zamrozeni.values()) {
            task.cancel();
        }

        zamrozeni.clear();
        cooldowny.clear();
        naszeSniezki.clear();
    }
}
