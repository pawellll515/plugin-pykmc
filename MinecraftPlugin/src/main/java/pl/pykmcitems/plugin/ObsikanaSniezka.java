package pl.pykmcitems.plugin;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerLaunchProjectileEvent;
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

    public ObsikanaSniezka(JavaPlugin plugin) {
        this.plugin = plugin;

        this.itemKey = new NamespacedKey(plugin, "obsikana_sniezka");
        this.projectileKey = new NamespacedKey(plugin, "obsikana_sniezka_projectile");
    }

    // Oznaczamy śnieżkę, jeśli została wyrzucona z naszego przedmiotu
    @EventHandler
    public void onProjectileLaunch(PlayerLaunchProjectileEvent event) {
        if (!(event.getProjectile() instanceof Snowball snowball)) {
            return;
        }

        if (!event.getItemStack().hasItemMeta()) {
            return;
        }

        if (!event.getItemStack().getItemMeta()
                .getPersistentDataContainer()
                .has(itemKey, PersistentDataType.BYTE)) {
            return;
        }

        snowball.getPersistentDataContainer().set(
                projectileKey,
                PersistentDataType.BYTE,
                (byte) 1
        );
    }

    // Obsługa trafienia przeciwnika
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();

        if (!(projectile instanceof Snowball)) {
            return;
        }

        if (!projectile.getPersistentDataContainer()
                .has(projectileKey, PersistentDataType.BYTE)) {
            return;
        }

        if (!(event.getHitEntity() instanceof Player target)) {
            return;
        }

        if (projectile.getShooter() instanceof Player shooter
                && shooter.getUniqueId().equals(target.getUniqueId())) {
            return;
        }

        zamroz(target);
    }

    private void zamroz(Player target) {
        UUID uuid = target.getUniqueId();

        // Jeśli gracz jest już zamrożony, odnawiamy czas efektu
        BukkitTask poprzednie = zamrozeni.remove(uuid);

        if (poprzednie != null) {
            poprzednie.cancel();
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
                            zakoncz(target);
                            return;
                        }

                        Location freezeLocation = miejscaTrafienia.get(uuid);

                        if (freezeLocation != null) {
                            target.teleport(freezeLocation);
                        }

                        // Przerywamy używanie przedmiotu, jeśli gracz próbuje jeść
                        if (target.isHandRaised()) {
                            target.clearActiveItem();
                        }

                        tick++;

                        if (tick >= 50) {
                            zakoncz(target);
                        }
                    }
                },
                0L,
                1L
        );

        zamrozeni.put(uuid, task);
    }

    private void zakoncz(Player target) {
        UUID uuid = target.getUniqueId();

        BukkitTask task = zamrozeni.remove(uuid);

        if (task != null) {
            task.cancel();
        }

        miejscaTrafienia.remove(uuid);

        if (target.isOnline()) {
            target.sendMessage("§aEfekt zamrożenia zakończył się!");
        }
    }

    // Dodatkowe zabezpieczenie: nie pozwalamy zakończyć jedzenia
    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        if (zamrozeni.containsKey(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            event.getPlayer().clearActiveItem();
        }
    }

    // Czyszczenie zadań przy wyłączaniu pluginu
    public void cleanup() {
        for (BukkitTask task : zamrozeni.values()) {
            task.cancel();
        }

        zamrozeni.clear();
        miejscaTrafienia.clear();
    }
}
