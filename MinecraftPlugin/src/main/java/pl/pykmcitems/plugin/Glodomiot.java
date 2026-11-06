
package pl.pykmcitems.plugin;

import net.kyori.adventure.text.Component;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Glodomiot implements Listener {

    private final NamespacedKey glodomiotKey;

    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public Glodomiot(JavaPlugin plugin) {
        this.glodomiotKey = new NamespacedKey(plugin, "glodomiot");
    }

    private boolean isGlodomiot(ItemStack item) {
        if (item == null || item.getType() != Material.IRON_HOE) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        return meta != null
                && meta.getPersistentDataContainer().has(
                        glodomiotKey,
                        PersistentDataType.BYTE
                );
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {

        if (!(event.getDamager() instanceof Player attacker)) {
            return;
        }

        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        ItemStack item = attacker.getInventory().getItemInMainHand();

        if (!isGlodomiot(item)) {
            return;
        }

        UUID uuid = attacker.getUniqueId();
        long now = System.currentTimeMillis();

        if (cooldowns.containsKey(uuid)
                && cooldowns.get(uuid) > now) {

            attacker.sendActionBar(
                    Component.text("§cGłodomiot na cooldownie!")
            );

            return;
        }

        cooldowns.put(uuid, now + 90_000);
        ((Main) plugin).getCooldownScoreboard().startCooldown(attacker, "glodomiot", "&6&lGłodomiot", 90_000L);

        // Informacje dla graczy
        attacker.sendActionBar(
                Component.text("§aUżyłeś Głodomiota!")
        );

        victim.sendActionBar(
                Component.text("§cPrzeciwnik użył na tobie Głodomiota!")
        );

        // Natychmiastowe wyzerowanie głodu
        victim.setFoodLevel(0);
        victim.setSaturation(0);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cooldowns.remove(event.getPlayer().getUniqueId());
    }
}
