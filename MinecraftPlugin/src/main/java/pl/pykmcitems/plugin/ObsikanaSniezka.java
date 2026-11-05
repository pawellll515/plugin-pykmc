
package pl.pykmcitems.plugin;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ObsikanaSniezka implements Listener {

    private final JavaPlugin plugin;
    private final NamespacedKey itemKey;
    private final NamespacedKey projectileKey;

    private final Map<UUID, BukkitTask> zamrozeni = new HashMap<>();
    private final Map<UUID, Location> miejscaTrafienia = new HashMap<>();
    private final Map<UUID, Long> cooldowny = new HashMap<>();

    private static final long COOLDOWN = 45_000L;

    public ObsikanaSniezka(JavaPlugin plugin) {
        this.plugin = plugin;
        this.itemKey = new NamespacedKey(plugin, "obsikana_sniezka");
        this.projectileKey = new NamespacedKey(plugin, "obsikana_sniezka_projectile");
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

        snowball.getPersistentDataContainer().set(
                projectileKey,
                PersistentDataType.BYTE,
                (byte) 1
        );

        // Ustawienie cooldownu 45 sekund
        cooldowny.put(player.getUniqueId(), teraz + COOLDOWN);

        // Zużycie jednej śnieżki
        if (item.getAmount() <= 1) {
            player.getInventory().setItemInMainHand(null);
        } else {
            item.setAmount(item.getAmount() - 1);
        }
    }

    // Trafienie przeciwnika
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Snowball snowball)) return;

        if (!snowball.getPersistentDataContainer()
                .has(projectileKey, PersistentDataType.BYTE)) {
            return;
        }

        if (!(event.getHitEntity() instanceof Player target)) return;

        if (snowball.getShooter() instanceof Player shooter
                && shooter.getUniqueId().equals(target.getUniqueId())) {
            return;
        }

        zamroz(target);
    }

    // Zamrażanie gracza
    private void zamroz(Player target) {
        UUID uuid = target.getUniqueId();

        BukkitTask previous = zamrozeni.remove(uuid);

        if (previous != null) {
            previous.cancel();
        }

        Location miejsce = target.getLocation().clone();
        miejscaTrafienia.put(uuid, miejsce);

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

                        Location freezeLocation = miejscaTrafienia.get(uuid);

                        if (freezeLocation != null) {
                            target.teleport(freezeLocation);
                        }

                        // Przerywanie jedzenia
                        if (target.isHandRaised()) {
                            target.clearActiveItem();
                        }

                        tick++;

                        if (tick >= 50) {
                            zakoncz(target, true);
                        }
                    }
                },
                0L,
                1L
        );

        zamrozeni.put(uuid, task);
    }

    // Blokowanie jedzenia
    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        if (zamrozeni.containsKey(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            event.getPlayer().clearActiveItem();
        }
    }

    // Zakończenie zamrożenia
    private void zakoncz(Player target, boolean notify) {
        UUID uuid = target.getUniqueId();

        BukkitTask task = zamrozeni.remove(uuid);

        if (task != null) {
            task.cancel();
        }

        miejscaTrafienia.remove(uuid);

        if (notify && target.isOnline()) {
            target.sendMessage("§aEfekt zamrożenia zakończył się!");
        }
    }

    // Wyjście z serwera
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        zakoncz(event.getPlayer(), false);
    }

    // Śmierć
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
        miejscaTrafienia.clear();
        cooldowny.clear();
    }
}
