
package pl.pykmcitems.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.ThreadLocalRandom;

public class KlataMeduzy implements Listener {

    private final JavaPlugin plugin;
    private final NamespacedKey klataKey;
    private final BukkitTask silaTask;

    public KlataMeduzy(JavaPlugin plugin) {
        this.plugin = plugin;
        this.klataKey = new NamespacedKey(plugin, "klata_meduzy");

        // Odnawianie Siły I co sekundę
        this.silaTask = plugin.getServer().getScheduler().runTaskTimer(
                plugin,
                this::odnawiajSile,
                0L,
                20L
        );
    }

    // SPRAWDZANIE KLATY MEDUZY

    private boolean maKl ate(Player player) {
        ItemStack chestplate = player.getInventory().getChestplate();

        if (chestplate == null || chestplate.getType() != Material.NETHERITE_CHESTPLATE) {
            return false;
        }

        if (!chestplate.hasItemMeta()) {
            return false;
        }

        Byte value = chestplate.getItemMeta()
                .getPersistentDataContainer()
                .get(klataKey, PersistentDataType.BYTE);

        return value != null && value == (byte) 1;
    }

    // STAŁA SIŁA I

    private void odnawiajSile() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {

            if (!maKl ate(player)) {
                continue;
            }

            PotionEffect aktualny = player.getPotionEffect(PotionEffectType.STRENGTH);

            // Nie obniżamy Siły II ani wyższej
            if (aktualny != null && aktualny.getAmplifier() > 0) {
                continue;
            }

            // Nie skracamy istniejącej Siły I
            if (aktualny != null && aktualny.getDuration() > 40) {
                continue;
            }

            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.STRENGTH,
                    40,
                    0,
                    true,
                    false,
                    true
            ));
        }
    }

    // PIORUN MEDUZY

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {

        if (!(event.getDamager() instanceof Player attacker)) {
            return;
        }

        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        if (!maKl ate(attacker)) {
            return;
        }

        // 3% szansy
        if (ThreadLocalRandom.current().nextDouble() >= 0.03) {
            return;
        }

        // Efekt wizualny pioruna bez jego standardowych obrażeń
        victim.getWorld().strikeLightningEffect(victim.getLocation());

        // Dodatkowe 4 serca obrażeń
        victim.damage(8.0, attacker);

        attacker.sendActionBar(
                Component.text(
                        "Uderzyłeś piorunem przeciwnika",
                        NamedTextColor.GREEN
                )
        );

        victim.sendActionBar(
                Component.text(
                        "Zostałeś uderzony piorunem",
                        NamedTextColor.RED
                )
        );
    }

    // NIEWIDZIALNOŚĆ PO OTRZYMANIU OBRAŻEŃ

    @EventHandler
    public void onDamage(EntityDamageEvent event) {

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (!maKl ate(player)) {
            return;
        }

        if (event.isCancelled() || event.getFinalDamage() <= 0) {
            return;
        }

        // 2% szansy
        if (ThreadLocalRandom.current().nextDouble() >= 0.02) {
            return;
        }

        // Niewidzialność na 30 sekund.
        // Efekt mikstury pozostawia widoczną zbroję i przedmioty w rękach.
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.INVISIBILITY,
                600,
                0,
                true,
                false,
                true
        ));
    }

    // WYŁĄCZANIE

    public void cleanup() {
        if (silaTask != null) {
            silaTask.cancel();
        }
    }
}
