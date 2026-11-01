
package pl.pykmcitems.plugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Buda implements Listener {

    private final JavaPlugin plugin;
    private final NamespacedKey budaKey;

    private final Map<UUID, Cage> cages = new HashMap<>();
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    private static final long COOLDOWN = 90_000L;
    private static final int FIGHT_TIME = 10;
    private static final double BORDER_SIZE = 10.0;

    public Buda(JavaPlugin plugin) {
        this.plugin = plugin;
        this.budaKey = new NamespacedKey(plugin, "buda");
    }

    private boolean isBuda(ItemStack item) {
        if (item == null || item.getType() != Material.CHAIN || !item.hasItemMeta()) {
            return false;
        }

        return item.getItemMeta().getPersistentDataContainer()
                .has(budaKey, PersistentDataType.BYTE);
    }

    public ItemStack createItem() {
        return Itemy.buda(plugin);
    }

    @EventHandler
    public void onUse(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player target)) {
            return;
        }

        Player attacker = event.getPlayer();

        if (!isBuda(attacker.getInventory().getItemInMainHand())
                && !isBuda(attacker.getInventory().getItemInOffHand())) {
            return;
        }

        event.setCancelled(true);

        if (attacker.equals(target)) {
            return;
        }

        if (cages.containsKey(attacker.getUniqueId())
                || cages.containsKey(target.getUniqueId())) {
            attacker.sendMessage("§cTy lub ten gracz jesteście już w klatce!");
            return;
        }

        long now = System.currentTimeMillis();
        long lastUse = cooldowns.getOrDefault(attacker.getUniqueId(), 0L);

        if (now - lastUse < COOLDOWN) {
            long remaining = (COOLDOWN - (now - lastUse)) / 1000L;
            attacker.sendMessage("§cBuda jest jeszcze na cooldownie! §7Pozostało: §e"
                    + remaining + "s");
            return;
        }

        startFight(attacker, target);
    }

    private void startFight(Player attacker, Player target) {
        Location center = target.getLocation().clone();

        double centerX = Math.floor(center.getX()) + 0.5;
        double centerZ = Math.floor(center.getZ()) + 0.5;

        double baseY = Math.floor(center.getY());

        // Podłoga 10 bloków pod stopami, dach 10 bloków nad stopami.
        double floorY = baseY - 10.0;
        double roofY = baseY + 10.0;

        WorldBorder attackerOldBorder = attacker.getWorldBorder();
        WorldBorder targetOldBorder = target.getWorldBorder();

        WorldBorder attackerBorder = Bukkit.createWorldBorder();
        attackerBorder.setCenter(centerX, centerZ);
        attackerBorder.setSize(BORDER_SIZE);

        WorldBorder targetBorder = Bukkit.createWorldBorder();
        targetBorder.setCenter(centerX, centerZ);
        targetBorder.setSize(BORDER_SIZE);

        Cage cage = new Cage(
                attacker,
                target,
                attackerOldBorder,
                targetOldBorder,
                attackerBorder,
                targetBorder,
                floorY,
                roofY
        );

        cages.put(attacker.getUniqueId(), cage);
        cages.put(target.getUniqueId(), cage);

        attacker.setWorldBorder(attackerBorder);
        target.setWorldBorder(targetBorder);

        attacker.setCooldown(Material.CHAIN, 90 * 20);
        cooldowns.put(attacker.getUniqueId(), System.currentTimeMillis());

        if (attacker.isGliding()) {
            attacker.setGliding(false);
        }

        if (target.isGliding()) {
            target.setGliding(false);
        }

        hidePlayers(cage);

        attacker.sendMessage("§eRozpoczęła się walka 1v1 z graczem §f"
                + target.getName() + "§e!");

        target.sendMessage("§eGracz §f" + attacker.getName()
                + " §eużył na tobie Budy!");

        // Sprawdzanie granic co tick, czyli 20 razy na sekundę.
        new BukkitRunnable() {
            int ticks = 0;
            int time = FIGHT_TIME;

            @Override
            public void run() {
                if (!cages.containsKey(attacker.getUniqueId())
                        || !cages.containsKey(target.getUniqueId())) {
                    cancel();
                    return;
                }

                if (!attacker.isOnline() || !target.isOnline()
                        || attacker.isDead() || target.isDead()) {
                    endFight(cage, "§cWalka została zakończona!");
                    cancel();
                    return;
                }

                applyVerticalLimits(cage);

                ticks++;

                // Odliczanie czasu walki co 20 ticków.
                if (ticks >= 20) {
                    ticks = 0;
                    time--;

                    if (time <= 0) {
                        endFight(cage, "§aWalka dobiegła końca!");
                        cancel();
                    }
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    private void applyVerticalLimits(Cage cage) {
        pushAtLimit(cage.attacker, cage.floorY, cage.roofY);
        pushAtLimit(cage.target, cage.floorY, cage.roofY);
    }

    private void pushAtLimit(Player player, double floorY, double roofY) {
        if (!player.isOnline() || player.isDead()) {
            return;
        }

        double y = player.getLocation().getY();
        double topY = y + player.getHeight();

        Vector velocity = player.getVelocity();

        // Niewidzialna podłoga 10 bloków pod graczem.
        if (y <= floorY + 0.15) {
            velocity.setY(0.45);
            player.setVelocity(velocity);
        }

        // Niewidzialny dach 10 bloków nad graczem.
        else if (topY >= roofY - 0.15) {
            velocity.setY(-0.45);
            player.setVelocity(velocity);
        }
    }

    private void hidePlayers(Cage cage) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.equals(cage.attacker) || viewer.equals(cage.target)) {
                continue;
            }

            if (viewer.canSee(cage.attacker)) {
                viewer.hidePlayer(plugin, cage.attacker);
                cage.hiddenPairs.add(new VisibilityPair(viewer, cage.attacker));
            }

            if (viewer.canSee(cage.target)) {
                viewer.hidePlayer(plugin, cage.target);
                cage.hiddenPairs.add(new VisibilityPair(viewer, cage.target));
            }

            if (cage.attacker.canSee(viewer)) {
                cage.attacker.hidePlayer(plugin, viewer);
                cage.hiddenPairs.add(new VisibilityPair(cage.attacker, viewer));
            }

            if (cage.target.canSee(viewer)) {
                cage.target.hidePlayer(plugin, viewer);
                cage.hiddenPairs.add(new VisibilityPair(cage.target, viewer));
            }
        }
    }

    private void endFight(Cage cage, String message) {
        if (cage.ended) {
            return;
        }

        cage.ended = true;

        cages.remove(cage.attacker.getUniqueId());
        cages.remove(cage.target.getUniqueId());

        if (cage.attacker.isOnline()) {
            cage.attacker.setWorldBorder(cage.attackerOldBorder);
            cage.attacker.sendMessage(message);
        }

        if (cage.target.isOnline()) {
            cage.target.setWorldBorder(cage.targetOldBorder);
            cage.target.sendMessage(message);
        }

        for (VisibilityPair pair : cage.hiddenPairs) {
            if (pair.viewer.isOnline() && pair.hidden.isOnline()) {
                pair.viewer.showPlayer(plugin, pair.hidden);
            }
        }
    }

    @EventHandler
    public void onPearlTeleport(PlayerTeleportEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            return;
        }

        Cage cage = cages.get(event.getPlayer().getUniqueId());

        if (cage == null || event.getTo() == null) {
            return;
        }

        WorldBorder border = event.getPlayer().getWorldBorder();

        if (!border.isInside(event.getTo())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onGlide(EntityToggleGlideEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (event.isGliding() && cages.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (isBuda(event.getItemInHand())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Cage cage = cages.get(event.getEntity().getUniqueId());

        if (cage != null) {
            endFight(cage, "§cWalka zakończona przez śmierć gracza!");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Cage cage = cages.get(event.getPlayer().getUniqueId());

        if (cage != null) {
            endFight(cage, "§cWalka zakończona przez wyjście gracza!");
        }
    }

    private static class Cage {
        private final Player attacker;
        private final Player target;
        private final WorldBorder attackerOldBorder;
        private final WorldBorder targetOldBorder;
        private final WorldBorder attackerBorder;
        private final WorldBorder targetBorder;
        private final double floorY;
        private final double roofY;
        private final List<VisibilityPair> hiddenPairs = new ArrayList<>();
        private boolean ended = false;

        private Cage(
                Player attacker,
                Player target,
                WorldBorder attackerOldBorder,
                WorldBorder targetOldBorder,
                WorldBorder attackerBorder,
                WorldBorder targetBorder,
                double floorY,
                double roofY
        ) {
            this.attacker = attacker;
            this.target = target;
            this.attackerOldBorder = attackerOldBorder;
            this.targetOldBorder = targetOldBorder;
            this.attackerBorder = attackerBorder;
            this.targetBorder = targetBorder;
            this.floorY = floorY;
            this.roofY = roofY;
        }
    }

    private static class VisibilityPair {
        private final Player viewer;
        private final Player hidden;

        private VisibilityPair(Player viewer, Player hidden) {
            this.viewer = viewer;
            this.hidden = hidden;
        }
    }
}
