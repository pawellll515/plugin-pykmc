
package pl.pykmcitems.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LukWiedzmy implements Listener {

    private final JavaPlugin plugin;
    private final NamespacedKey itemKey;

    private final Map<UUID, UUID> naszeStrzaly = new HashMap<>();

    private final Map<UUID, BukkitTask> zamrozeni = new HashMap<>();
    private final Map<UUID, Long> cooldowny = new HashMap<>();

    private static final long COOLDOWN = 60_000L;

    // Tak samo jak w Obsikanej Śnieżce: 50 ticków = 2,5 sekundy.
    private static final int CZAS_ZAMROZENIA = 50;

    // Oślepienie: 2 sekundy.
    private static final int CZAS_OSLEPIENIA = 40;

    public LukWiedzmy(JavaPlugin plugin) {
        this.plugin = plugin;
        this.itemKey = new NamespacedKey(plugin, "luk_wiedzmy");
    }

    // Tworzenie przedmiotu
    public ItemStack createItem() {

        ItemStack item = new ItemStack(Material.BOW);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(
                Component.text("Łuk Wiedźmy")
                        .color(NamedTextColor.LIGHT_PURPLE)
                        .decoration(TextDecoration.ITALIC, false)
        );

        meta.lore(java.util.List.of(
                Component.text("Po trafieniu, twój przeciwnik dostaje")
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),

                Component.text("efekt oślepienia oraz zamraża go na 2.5s")
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),

                Component.empty(),

                Component.text("Cooldown: 60s")
                        .color(NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false),

                Component.text("Przedmiot pochodzi z eventu Halloween 2024")
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));

        // CustomModelData 2
        meta.setCustomModelData(2);

        meta.getPersistentDataContainer().set(
                itemKey,
                PersistentDataType.BYTE,
                (byte) 1
        );

        item.setItemMeta(meta);

        return item;
    }

    // Sprawdzanie, czy to Łuk Wiedźmy
    private boolean isLukWiedzmy(ItemStack item) {

        if (item == null || item.getType() != Material.BOW
                || !item.hasItemMeta()) {
            return false;
        }

        return item.getItemMeta().getPersistentDataContainer()
                .has(itemKey, PersistentDataType.BYTE);
    }

    // Strzelanie z łuku
    @EventHandler
    public void onShoot(EntityShootBowEvent event) {

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (!isLukWiedzmy(event.getBow())) {
            return;
        }

        long teraz = System.currentTimeMillis();

        long koniec = cooldowny.getOrDefault(
                player.getUniqueId(), 0L
        );

        // Sprawdzanie cooldownu
        if (teraz < koniec) {

            long pozostalo = (koniec - teraz + 999) / 1000;

            player.sendMessage(
                    "§cŁuk Wiedźmy jest na cooldownie! Pozostało: §e"
                            + pozostalo + "s"
            );

            event.setCancelled(true);
            return;
        }

        // Zapamiętanie strzały
        if (event.getProjectile() instanceof Arrow arrow) {
            naszeStrzaly.put(
                    arrow.getUniqueId(),
                    player.getUniqueId()
            );
        }

        // Cooldown 60 sekund
        cooldowny.put(
                player.getUniqueId(),
                teraz + COOLDOWN
        );

        ((Main) plugin).getCooldownScoreboard().startCooldown(
                player,
                "luk_wiedzmy",
                "&dŁuk Wiedźmy",
                COOLDOWN
        );
    }

    // Wykrywanie trafienia strzałą
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {

        if (!(event.getEntity() instanceof Arrow arrow)) {
            return;
        }

        UUID shooterUUID = naszeStrzaly.remove(
                arrow.getUniqueId()
        );

        if (shooterUUID == null) {
            return;
        }

        if (!(event.getHitEntity() instanceof Player target)) {
            return;
        }

        // Nie trafiamy samego siebie
        if (shooterUUID.equals(target.getUniqueId())) {
            return;
        }

        // Oślepienie na 2 sekundy
        target.addPotionEffect(
                new PotionEffect(
                        PotionEffectType.BLINDNESS,
                        CZAS_OSLEPIENIA,
                        0,
                        false,
                        false
                )
        );

        // Zamrożenie
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

        target.sendMessage(
                "§bZostałeś zamrożony na §e2.5 sekundy§b!"
        );

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

        if (!zamrozeni.containsKey(player.getUniqueId())) {
            return;
        }

        if (event.getTo() == null) {
            return;
        }

        // Blokujemy zmianę pozycji, ale pozwalamy obracać kamerą
        if (event.getFrom().getX() != event.getTo().getX()
                || event.getFrom().getY() != event.getTo().getY()
                || event.getFrom().getZ() != event.getTo().getZ()) {

            event.setTo(event.getFrom().clone());
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
            target.sendMessage(
                    "§aEfekt zamrożenia zakończył się!"
            );
        }
    }

    // Wyjście z serwera
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        zakoncz(player, false);

        cooldowny.remove(uuid);

        naszeStrzaly.values().removeIf(
                shooter -> shooter.equals(uuid)
        );
    }

    // Śmierć gracza
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {

        Player player = event.getEntity();

        zakoncz(player, false);

        cooldowny.remove(player.getUniqueId());

        naszeStrzaly.values().removeIf(
                shooter -> shooter.equals(player.getUniqueId())
        );
    }

    // Czyszczenie przy wyłączeniu pluginu
    public void cleanup() {

        for (BukkitTask task : zamrozeni.values()) {
            task.cancel();
        }

        zamrozeni.clear();
        cooldowny.clear();
        naszeStrzaly.clear();
    }
}
