
package pl.pykmcitems.plugin;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.persistence.PersistentDataType;

public class ZlodziejZabawy implements Listener {

    private final Main plugin;
    private final NamespacedKey itemKey;

    private static final String COOLDOWN_ID = "zlodziej_zabawy";
    private static final long COOLDOWN = 120_000L;
    private static final double RANGE = 6.0;

    public ZlodziejZabawy(Main plugin) {
        this.plugin = plugin;
        this.itemKey = new NamespacedKey(plugin, "zlodziej_zabawy");
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {

        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || !item.hasItemMeta()) {
            return;
        }

        if (!item.getItemMeta().getPersistentDataContainer().has(
                itemKey,
                PersistentDataType.BYTE
        )) {
            return;
        }

        event.setCancelled(true);

        ItemCooldownManager cooldownManager =
                plugin.getItemCooldownManager();

        if (!cooldownManager.tryStartCooldown(
                player,
                COOLDOWN_ID,
                "&aZłodziej Zabawy",
                COOLDOWN
        )) {
            long remaining = cooldownManager.getRemainingCooldown(
                    player,
                    COOLDOWN_ID
            );

            long seconds = (remaining + 999) / 1000;

            player.sendMessage(
                    "§cZłodziej Zabawy jest na cooldownie! §7Pozostało: §e"
                            + seconds + "s"
            );
            return;
        }

        int removedEffects = 0;

        for (Entity entity : player.getNearbyEntities(
                RANGE,
                RANGE,
                RANGE
        )) {

            if (!(entity instanceof Player target)) {
                continue;
            }

            // Nie usuwamy efektów osobie używającej miecza.
            if (target.equals(player)) {
                continue;
            }

            // Dokładny zasięg 6 bloków, liczony od pozycji gracza.
            if (target.getLocation().distanceSquared(
                    player.getLocation()
            ) > RANGE * RANGE) {
                continue;
            }

            for (PotionEffect effect : target.getActivePotionEffects()) {

                if (effect.getType().getCategory()
                        == PotionEffectType.Category.BENEFICIAL) {

                    target.removePotionEffect(effect.getType());
                    removedEffects++;
                }
            }
        }

        player.sendMessage(
                "§aZłodziej Zabawy §7usunął pozytywne efekty graczom w pobliżu!"
        );
    }
}
