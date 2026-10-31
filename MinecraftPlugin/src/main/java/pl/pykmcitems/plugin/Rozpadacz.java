
package pl.pykmcitems.plugin;

import net.kyori.adventure.text.Component;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Rozpadacz implements Listener {

    private final NamespacedKey rozpadaczKey;

    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public Rozpadacz(JavaPlugin plugin) {
        this.rozpadaczKey = new NamespacedKey(plugin, "rozpadacz");
    }

    private boolean isRozpadacz(ItemStack item) {
        if (item == null || item.getType() != Material.HEART_OF_THE_SEA) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        return meta != null
                && meta.getPersistentDataContainer().has(
                        rozpadaczKey,
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

        if (!isRozpadacz(item)) {
            return;
        }

        event.setCancelled(true);

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        if (cooldowns.containsKey(uuid)
                && cooldowns.get(uuid) > now) {

            player.sendActionBar(
                    Component.text("§cRozpadacz jest na cooldownie!")
            );

            return;
        }

        cooldowns.put(uuid, now + 90_000);

        // Cooldown Minecrafta: 90 sekund
        player.setCooldown(Material.HEART_OF_THE_SEA, 1800);

        player.sendActionBar(
                Component.text("§4§lRozpadacz §7został użyty!")
        );

        // Trafianie graczy w promieniu 4 kratek
        for (Player target : player.getWorld().getPlayers()) {

            if (target.equals(player)) {
                continue;
            }

            if (target.getLocation().distanceSquared(
                    player.getLocation()
            ) > 16) {
                continue;
            }

            // Zabranie 33% aktualnego zdrowia
            double currentHealth = target.getHealth();
            double newHealth = currentHealth * 0.67;

            target.setHealth(Math.max(0.0, newHealth));

            // Efekt pioruna bez dodatkowych obrażeń
            target.getWorld().strikeLightningEffect(
                    target.getLocation()
            );

            target.sendActionBar(
                    Component.text(
                            "§4§lRozpadacz §7zabrał ci §e33% §7życia!"
                    )
            );
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cooldowns.remove(event.getPlayer().getUniqueId());
    }
}
