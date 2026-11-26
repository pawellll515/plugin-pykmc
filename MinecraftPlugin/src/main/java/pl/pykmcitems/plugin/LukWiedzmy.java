
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

    private final Main plugin;
    private final NamespacedKey itemKey;

    private final Map<UUID, UUID> naszeStrzaly = new HashMap<>();
    private final Map<UUID, BukkitTask> zamrozeni = new HashMap<>();

    private static final long COOLDOWN = 60_000L;

    private static final int CZAS_ZAMROZENIA = 50;
    private static final int CZAS_OSLEPIENIA = 40;

    public LukWiedzmy(JavaPlugin plugin) {
        this.plugin = (Main) plugin;
        this.itemKey = new NamespacedKey(plugin, "luk_wiedzmy");
    }

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

        meta.setCustomModelData(2);

        meta.getPersistentDataContainer().set(
                itemKey,
                PersistentDataType.BYTE,
                (byte) 1
        );

        item.setItemMeta(meta);

        return item;
    }

    private boolean isLukWiedzmy(ItemStack item) {

        if (item == null || item.getType() != Material.BOW
                || !item.hasItemMeta()) {
            return false;
        }

        return item.getItemMeta().getPersistentDataContainer()
                .has(itemKey, PersistentDataType.BYTE);
    }

    @EventHandler
    public void onShoot(EntityShootBowEvent event) {

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (!isLukWiedzmy(event.getBow())) {
            return;
        }

        ItemCooldownManager cooldownManager =
                plugin.getItemCooldownManager();

        if (cooldownManager.isOnCooldown(player, "luk_wiedzmy")) {

            long remaining = cooldownManager.getRemainingCooldown(
                    player,
                    "luk_wiedzmy"
            );

            long seconds = (remaining + 999) / 1000;

            player.sendMessage(
                    "§cŁuk Wiedźmy jest na cooldownie! Pozostało: §e"
                            + seconds + "s"
            );

            event.setCancelled(true);
            return;
        }

        if (!cooldownManager.tryStartCooldown(
                player,
                "luk_wiedzmy",
                "&dŁuk Wiedźmy",
                COOLDOWN
        )) {
            event.setCancelled(true);
            return;
        }

        if (event.getProjectile() instanceof Arrow arrow) {
            naszeStrzaly.put(
                    arrow.getUniqueId(),
                    player.getUniqueId()
            );
        }
    }

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

        if (shooterUUID.equals(target.getUniqueId())) {
            return;
        }

        target.addPotionEffect(
                new PotionEffect(
                        PotionEffectType.BLINDNESS,
                        CZAS_OSLEPIENIA,
                        0,
                        false,
                        false
                )
        );

        zamroz(target);
    }

    private void zamroz(Player target) {

        UUID uuid = target.getUniqueId();

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

                        target.setVelocity(new Vector(0, 0, 0));
                        target.setFallDistance(0);

                        tick++;

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

    @EventHandler
    public void onMove(PlayerMoveEvent event) {

        Player player = event.getPlayer();

        if (!zamrozeni.containsKey(player.getUniqueId())) {
            return;
        }

        if (event.getTo() == null) {
            return;
        }

        if (event.getFrom().getX() != event.getTo().getX()
                || event.getFrom().getY() != event.getTo().getY()
                || event.getFrom().getZ() != event.getTo().getZ()) {

            event.setTo(event.getFrom().clone());
        }
    }

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

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        zakoncz(player, false);

        naszeStrzaly.values().removeIf(
                shooter -> shooter.equals(uuid)
        );
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {

        Player player = event.getEntity();

        zakoncz(player, false);

        naszeStrzaly.values().removeIf(
                shooter -> shooter.equals(player.getUniqueId())
        );
    }

    public void cleanup() {

        for (BukkitTask task : zamrozeni.values()) {
            task.cancel();
        }

        zamrozeni.clear();
        naszeStrzaly.clear();
    }
}
