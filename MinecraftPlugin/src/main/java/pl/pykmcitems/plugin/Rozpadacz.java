
package pl.pykmcitems.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Rozpadacz implements Listener {

    private final JavaPlugin plugin;
    private final NamespacedKey rozpadaczKey;
    private final ItemCooldownManager cooldownManager;

    // Indywidualna ochrona trafionych graczy
    private final Map<UUID, Long> protectionCooldowns = new HashMap<>();

    public Rozpadacz(JavaPlugin plugin) {
        this.plugin = plugin;
        this.rozpadaczKey = new NamespacedKey(plugin, "rozpadacz");
        this.cooldownManager = ((Main) plugin).getItemCooldownManager();
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

        if (!cooldownManager.tryStartCooldown(
                player,
                "rozpadacz",
                "&4&lRozpadacz",
                60_000L
        )) {
            player.sendActionBar(
                    Component.text(
                            "Rozpadacz jest na cooldownie!",
                            NamedTextColor.RED
                    )
            );

            return;
        }

        // Komunikat dla używającego
        player.sendActionBar(
                Component.text("Rozpadacz ", NamedTextColor.DARK_RED)
                        .decorate(TextDecoration.BOLD)
                        .append(Component.text(
                                "został użyty!",
                                NamedTextColor.GRAY
                        ))
        );

        long now = System.currentTimeMillis();

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

            UUID targetUUID = target.getUniqueId();

            // Sprawdzenie indywidualnej ochrony gracza
            Long protectionUntil = protectionCooldowns.get(targetUUID);

            if (protectionUntil != null && protectionUntil > now) {
                continue;
            }

            // Ochrona przed kolejnym Rozpadaczem przez 45 sekund
            protectionCooldowns.put(targetUUID, now + 45_000L);

            // Zabranie dokładnie 8 serc (16 punktów zdrowia)
            double currentHealth = target.getHealth();
            double newHealth = Math.max(0.0, currentHealth - 16.0);

            target.setHealth(newHealth);

            // Efekt pioruna bez dodatkowych obrażeń
            target.getWorld().strikeLightningEffect(
                    target.getLocation()
            );

            // Komunikat dla trafionego gracza
            target.sendActionBar(
                    Component.text("Rozpadacz ", NamedTextColor.DARK_RED)
                            .decorate(TextDecoration.BOLD)
                            .append(Component.text(
                                    "zabrał ci ",
                                    NamedTextColor.GRAY
                            ))
                            .append(Component.text(
                                    "8 serc",
                                    NamedTextColor.YELLOW
                            ))
                            .append(Component.text(
                                    " życia!",
                                    NamedTextColor.GRAY
                            ))
            );
        }
    }
}
