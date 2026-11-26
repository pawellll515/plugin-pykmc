
package pl.pykmcitems.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;

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

import java.time.Duration;

public class Rog implements Listener {

    private final JavaPlugin plugin;
    private final NamespacedKey hornKey;
    private final ItemCooldownManager cooldownManager;

    public Rog(JavaPlugin plugin) {
        this.plugin = plugin;
        this.hornKey = new NamespacedKey(plugin, "rog_niebios");
        this.cooldownManager = ((Main) plugin).getItemCooldownManager();
    }

    private boolean isHorn(ItemStack item) {
        if (item == null || item.getType() != Material.GOAT_HORN) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        return meta != null
                && meta.getPersistentDataContainer().has(
                        hornKey,
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

        if (!isHorn(item)) {
            return;
        }

        event.setCancelled(true);

        if (!cooldownManager.tryStartCooldown(
                player,
                "rog",
                "&b&lRóg Niebios",
                120_000L
        )) {
            return;
        }

        // Tytuł dla osoby używającej rogu
        player.showTitle(
                Title.title(
                        Component.empty(),
                        Component.text(
                                "Róg Niebios wyrzucił graczy",
                                NamedTextColor.AQUA
                        ).decorate(TextDecoration.BOLD),
                        Title.Times.times(
                                Duration.ofMillis(300),
                                Duration.ofSeconds(2),
                                Duration.ofMillis(500)
                        )
                )
        );

        // Wyrzucanie graczy w promieniu 5 kratek
        for (Player target : player.getWorld().getPlayers()) {

            if (target.equals(player)) {
                continue;
            }

            if (target.getLocation().distanceSquared(
                    player.getLocation()
            ) > 25) {
                continue;
            }

            // Levitation 50 przez 3 sekundy
            target.addPotionEffect(new PotionEffect(
                    PotionEffectType.LEVITATION,
                    60,
                    49,
                    true,
                    true,
                    true
            ));

            // Tytuł dla trafionego gracza
            target.showTitle(
                    Title.title(
                            Component.empty(),
                            Component.text(
                                    "Róg Niebios cię wyrzucił",
                                    NamedTextColor.AQUA
                            ).decorate(TextDecoration.BOLD),
                            Title.Times.times(
                                    Duration.ofMillis(300),
                                    Duration.ofSeconds(2),
                                    Duration.ofMillis(500)
                            )
                    )
            );
        }
    }
}
