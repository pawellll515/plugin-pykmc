
package pl.pykmcitems.plugin;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectTypeCategory;

public class ZlodziejZabawy implements Listener {

    private static final String ITEM_ID = "zlodziej_zabawy";
    private static final long COOLDOWN = 120_000L;

    private final Main plugin;
    private final ItemCooldownManager cooldownManager;
    private final NamespacedKey itemKey;

    public ZlodziejZabawy(Main plugin) {
        this.plugin = plugin;
        this.cooldownManager = plugin.getItemCooldownManager();
        this.itemKey = new NamespacedKey(plugin, ITEM_ID);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {

        Action action = event.getAction();

        if (action != Action.RIGHT_CLICK_AIR
                && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() != Material.NETHERITE_SWORD) {
            return;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta == null || !meta.getPersistentDataContainer().has(
                itemKey,
                PersistentDataType.BYTE
        )) {
            return;
        }

        event.setCancelled(true);

        if (!cooldownManager.tryStartCooldown(
                player,
                ITEM_ID,
                "&aZłodziej Zabawy",
                COOLDOWN
        )) {
            return;
        }

        int affectedPlayers = 0;

        for (Entity entity : player.getNearbyEntities(6, 6, 6)) {

            if (!(entity instanceof Player target)) {
                continue;
            }

            if (target.equals(player)) {
                continue;
            }

            if (target.getLocation().distanceSquared(
                    player.getLocation()
            ) > 36) {
                continue;
            }

            boolean removed = false;

            for (PotionEffect effect : target.getActivePotionEffects()) {

                if (effect.getType().getCategory()
                        == PotionEffectTypeCategory.BENEFICIAL) {

                    target.removePotionEffect(effect.getType());
                    removed = true;
                }
            }

            if (removed) {
                affectedPlayers++;

                // Wyłącznie wizualny piorun – nie zadaje obrażeń.
                target.getWorld().strikeLightningEffect(
                        target.getLocation()
                );
            }
        }

        if (affectedPlayers > 0) {
            player.sendMessage(
                    "§aUsunięto pozytywne efekty graczom w pobliżu!"
            );
        } else {
            player.sendMessage(
                    "§7W pobliżu nie ma graczy z pozytywnymi efektami."
            );
        }
    }
}
