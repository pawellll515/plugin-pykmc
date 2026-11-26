
package pl.pykmcitems.plugin;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Koniczynka implements Listener {

    private final JavaPlugin plugin;
    private final NamespacedKey cloverKey;
    private final ItemCooldownManager cooldownManager;

    public Koniczynka(JavaPlugin plugin) {
        this.plugin = plugin;
        this.cloverKey = new NamespacedKey(plugin, "koniczynka");
        this.cooldownManager = ((Main) plugin).getItemCooldownManager();
    }

    private boolean isClover(ItemStack item) {
        if (item == null || item.getType() != Material.PAPER) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        return meta != null
                && meta.getPersistentDataContainer().has(
                        cloverKey,
                        PersistentDataType.BYTE
                );
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {

        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Action action = event.getAction();

        if (action != Action.RIGHT_CLICK_AIR
                && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();

        ItemStack item = player.getInventory().getItemInMainHand();

        if (!isClover(item)) {
            return;
        }

        event.setCancelled(true);

        // Rozpoczęcie cooldownu we wspólnym systemie
        if (!cooldownManager.tryStartCooldown(
                player,
                "koniczynka",
                "&a&lKoniczynka",
                30_000L
        )) {
            return;
        }

        // Efekty Koniczynki

        player.addPotionEffect(new PotionEffect(
                PotionEffectType.STRENGTH,
                400,
                1,
                true,
                true,
                true
        ));

        player.addPotionEffect(new PotionEffect(
                PotionEffectType.HASTE,
                200,
                1,
                true,
                true,
                true
        ));

        player.addPotionEffect(new PotionEffect(
                PotionEffectType.FIRE_RESISTANCE,
                6000,
                0,
                true,
                true,
                true
        ));

        player.addPotionEffect(new PotionEffect(
                PotionEffectType.RESISTANCE,
                6000,
                0,
                true,
                true,
                true
        ));

        player.addPotionEffect(new PotionEffect(
                PotionEffectType.REGENERATION,
                400,
                1,
                true,
                true,
                true
        ));

        player.addPotionEffect(new PotionEffect(
                PotionEffectType.ABSORPTION,
                2400,
                3,
                true,
                true,
                true
        ));

        // Zużycie jednej Koniczynki
        if (item.getAmount() <= 1) {
            player.getInventory().setItemInMainHand(
                    new ItemStack(Material.AIR)
            );
        } else {
            item.setAmount(item.getAmount() - 1);
        }
    }
}
