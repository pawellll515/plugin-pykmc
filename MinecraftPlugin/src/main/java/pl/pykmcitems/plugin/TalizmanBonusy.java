
package pl.pykmcitems.plugin;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class TalizmanBonusy implements Listener {

    private final Main plugin;
    private final TalizmanyManager manager;

    private final Map<UUID, Long> odpornoscCooldown = new HashMap<>();

    public TalizmanBonusy(Main plugin, TalizmanyManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    // ==========================================
    // ODPORNOŚĆ - REDUKCJA OBRAŻEŃ
    // ==========================================

    @EventHandler
    public void onDamageReceived(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        double reduction = 0.0;

        reduction = Math.max(
                reduction,
                manager.getValue(player, "odpornosc_1")
        );

        reduction = Math.max(
                reduction,
                manager.getValue(player, "odpornosc_2")
        );

        reduction = Math.max(
                reduction,
                manager.getValue(player, "odpornosc_3")
        );

        if (reduction > 0.0) {
            event.setDamage(
                    event.getDamage() * (1.0 - reduction / 100.0)
            );
        }

        // Odporność III - 5% szansy na 2 serca absorpcji.
        if (manager.has(player, "odpornosc_3")) {
            if (losuj(5.0) && mozeUzycAbsorpcji(player)) {
                player.setAbsorptionAmount(
                        player.getAbsorptionAmount() + 4.0
                );

                odpornoscCooldown.put(
                        player.getUniqueId(),
                        System.currentTimeMillis() + 60_000L
                );

                player.sendMessage(
                        "§e✦ §aOdporność III §7aktywowała 2 serca absorpcji!"
                );
            }
        }
    }

    private boolean mozeUzycAbsorpcji(Player player) {
        Long koniec = odpornoscCooldown.get(player.getUniqueId());

        return koniec == null || System.currentTimeMillis() >= koniec;
    }

    // ==========================================
    // SIŁA - DODATKOWE OBRAŻENIA
    // ==========================================

    @EventHandler
    public void onDamageDealt(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) {
            return;
        }

        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        double bonus = 0.0;

        bonus = Math.max(
                bonus,
                manager.getValue(attacker, "sila_1")
        );

        bonus = Math.max(
                bonus,
                manager.getValue(attacker, "sila_2")
        );

        bonus = Math.max(
                bonus,
                manager.getValue(attacker, "sila_3")
        );

        if (bonus > 0.0) {
            event.setDamage(
                    event.getDamage() * (1.0 + bonus / 100.0)
            );
        }

        // Słodkości III - dodatkowe 10% obrażeń krytycznych.
        if (manager.has(attacker, "slodkosci_3")
                && czyKrytyczneUderzenie(attacker)) {

            event.setDamage(event.getDamage() * 1.10);
        }
    }

    private boolean czyKrytyczneUderzenie(Player player) {
        return player.getFallDistance() > 0.0F
                && !player.isOnGround()
                && !player.isSprinting()
                && !player.isInsideVehicle()
                && !player.isInWater()
                && !player.isClimbing()
                && !player.hasPotionEffect(PotionEffectType.BLINDNESS);
    }

    // ==========================================
    // SIŁA III - 5% SZANSY NA SZYBKOŚĆ II
    // ==========================================

    @EventHandler
    public void onEat(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();

        if (!manager.has(player, "sila_3")) {
            return;
        }

        if (!losuj(5.0)) {
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }

            player.addPotionEffect(
                    new PotionEffect(
                            PotionEffectType.SPEED,
                            200,
                            1,
                            false,
                            true,
                            true
                    ),
                    true
            );

            player.sendMessage(
                    "§e✦ §aSiła III §7aktywowała Szybkość II na 10 sekund!"
            );
        });
    }

    // ==========================================
    // SŁODKOŚCI III - DŁUŻSZA REGENERACJA
    // ==========================================

    @EventHandler
    public void onGoldenAppleEat(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        Material material = event.getItem().getType();

        if (material != Material.GOLDEN_APPLE
                && material != Material.ENCHANTED_GOLDEN_APPLE) {
            return;
        }

        if (!manager.has(player, "slodkosci_3")) {
            return;
        }

        // Efekt regeneracji jest dodawany przez Minecraft po evencie.
        // Dlatego sprawdzamy go jeden tick później.
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }

            PotionEffect regeneration =
                    player.getPotionEffect(PotionEffectType.REGENERATION);

            if (regeneration == null) {
                return;
            }

            player.addPotionEffect(
                    new PotionEffect(
                            PotionEffectType.REGENERATION,
                            regeneration.getDuration() + 20,
                            regeneration.getAmplifier(),
                            regeneration.isAmbient(),
                            regeneration.hasParticles(),
                            regeneration.hasIcon()
                    ),
                    true
            );
        }, 1L);
    }

    // ==========================================
    // LOSOWANIE SZANS
    // ==========================================

    private boolean losuj(double procent) {
        return ThreadLocalRandom.current().nextDouble(100.0) < procent;
    }
}
