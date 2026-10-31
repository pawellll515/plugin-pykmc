package pl.pykmcitems.plugin;

import io.papermc.paper.event.entity.EntityKnockbackEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class Hydra implements Listener {

    private static final int MAKS_LADOWANIA = 14;
    private static final long CZAS_WYGASNIECIA_TICKI = 50L; // 2,5 sekundy

    private final JavaPlugin plugin;
    private final NamespacedKey hydraKey;
    private final Map<UUID, StanHydry> stany = new ConcurrentHashMap<>();

    public Hydra(JavaPlugin plugin) {
        this.plugin = plugin;
        this.hydraKey = new NamespacedKey(plugin, "hydra");
    }

    private static class StanHydry {
        private int naladowanie;
        private int damage;
        private int speed;
        private int haste;
        private int antyKnockback;
        private long numerUderzenia;
        private BossBar bossBar;
    }

    private boolean jestHydra(ItemStack item) {
        if (item == null || item.getType() != org.bukkit.Material.NETHERITE_SWORD) {
            return false;
        }

        return item.getItemMeta() != null
                && item.getItemMeta().getPersistentDataContainer()
                .has(hydraKey, PersistentDataType.BYTE);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        // Liczy się wyłącznie bezpośredni atak gracza na gracza.
        // Ender pearl jest osobnym typem encji, więc nie może nabić Hydry.
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player)) return;
        if (!jestHydra(attacker.getInventory().getItemInMainHand())) return;

        UUID uuid = attacker.getUniqueId();
        StanHydry stan = stany.computeIfAbsent(uuid, ignored -> new StanHydry());

        if (stan.naladowanie < MAKS_LADOWANIA) {
            stan.naladowanie++;
            wylosujBonus(stan);
        }

        // Zachowuje mechanikę oryginalnego Skripta: poziom Damage
        // jest dodawany do obrażeń wyrażonych w punktach Minecrafta.
        if (stan.damage > 0) {
            event.setDamage(event.getDamage() + stan.damage);
        }

        if (stan.speed > 0) {
            attacker.addPotionEffect(new PotionEffect(
                    PotionEffectType.SPEED, 60, stan.speed - 1, false, false, true
            ));
        }

        if (stan.haste > 0) {
            attacker.addPotionEffect(new PotionEffect(
                    PotionEffectType.HASTE, 60, stan.haste - 1, false, false, true
            ));
        }

        aktualizujBossBar(attacker, stan);

        long numer = ++stan.numerUderzenia;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            StanHydry aktualny = stany.get(uuid);
            if (aktualny == null || aktualny.numerUderzenia != numer) return;

            Player gracz = Bukkit.getPlayer(uuid);
            if (gracz != null) {
                zakonczHydre(gracz, aktualny);
            } else {
                usunStan(uuid, aktualny);
            }
        }, CZAS_WYGASNIECIA_TICKI);
    }

    private void wylosujBonus(StanHydry stan) {
        List<Integer> dostepne = new ArrayList<>();

        if (stan.damage < 5) dostepne.add(1);
        if (stan.speed < 3) dostepne.add(2);
        if (stan.haste < 3) dostepne.add(3);
        if (stan.antyKnockback < 3) dostepne.add(4);

        if (dostepne.isEmpty()) return;

        int bonus = dostepne.get(
                ThreadLocalRandom.current().nextInt(dostepne.size())
        );

        switch (bonus) {
            case 1 -> stan.damage++;
            case 2 -> stan.speed++;
            case 3 -> stan.haste++;
            case 4 -> stan.antyKnockback++;
            default -> {
            }
        }
    }

    private void aktualizujBossBar(Player player, StanHydry stan) {
        if (stan.bossBar == null) {
            stan.bossBar = Bukkit.createBossBar("", BarColor.BLUE, BarStyle.SOLID);
            stan.bossBar.addPlayer(player);
        }

        StringBuilder gwiazdki = new StringBuilder();

        for (int i = 0; i < MAKS_LADOWANIA; i++) {
            gwiazdki.append(i < stan.naladowanie ? "§b★" : "§8☆");
        }

        // Wszystkie statystyki są w tej samej linii co gwiazdki.
        String tytul = "§b✦ " + gwiazdki
                + " §c⚔" + stan.damage + "/5"
                + " §b⚡" + stan.speed + "/3"
                + " §6⛏" + stan.haste + "/3"
                + " §a🛡" + (stan.antyKnockback * 10) + "%";

        stan.bossBar.setTitle(tytul);
        stan.bossBar.setProgress(Math.max(0.0, Math.min(1.0,
                (double) stan.naladowanie / MAKS_LADOWANIA)));
        stan.bossBar.setColor(BarColor.BLUE);
        stan.bossBar.setStyle(BarStyle.SOLID);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onKnockback(EntityKnockbackEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        StanHydry stan = stany.get(player.getUniqueId());
        if (stan == null || stan.antyKnockback <= 0) return;

        double mnoznik = 1.0 - (stan.antyKnockback * 0.10);
        Vector knockback = event.getKnockback().clone().multiply(mnoznik);
        event.setKnockback(knockback);
    }

    private void zakonczHydre(Player player, StanHydry stan) {
        if (stan.bossBar != null) {
            stan.bossBar.removeAll();
        }

        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.HASTE);
        stany.remove(player.getUniqueId(), stan);

        player.sendActionBar(Component.text(
                "✦ Moc Hydry wygasła!", NamedTextColor.AQUA
        ));
    }

    private void usunStan(UUID uuid, StanHydry stan) {
        if (stan.bossBar != null) {
            stan.bossBar.removeAll();
        }
        stany.remove(uuid, stan);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        StanHydry stan = stany.remove(uuid);

        if (stan != null && stan.bossBar != null) {
            stan.bossBar.removeAll();
        }
    }
}
