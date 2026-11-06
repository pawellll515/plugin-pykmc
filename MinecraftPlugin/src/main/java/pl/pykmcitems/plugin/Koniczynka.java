package pl.pykmcitems.plugin;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Koniczynka implements Listener {

    private final NamespacedKey cloverKey;

    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public Koniczynka(JavaPlugin plugin) {
        this.cloverKey = new NamespacedKey(plugin, "koniczynka");
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

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        if (cooldowns.containsKey(uuid)
                && cooldowns.get(uuid) > now) {
            return;
        }

        cooldowns.put(uuid, now + 30_000);
        ((Main) plugin).getCooldownScoreboard().startCooldown(player, "koniczynka", "&a&lKoniczynka", 30_000L);

        // Efekty Koniczynki

        player.addPotionEffect(new PotionEffect(
                PotionEffectType.STRENGTH,
                100,
                1,
                true,
                true,
                true
        ));

        player.addPotionEffect(new PotionEffect(
                PotionEffectType.HASTE,
                100,
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

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cooldowns.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        cooldowns.remove(event.getEntity().getUniqueId());
    }
}
