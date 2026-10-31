
package pl.pykmcitems.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Rozga implements Listener {

    private final JavaPlugin plugin;

    private final NamespacedKey rozgaKey;
    private final NamespacedKey temporaryHeadKey;

    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, ItemStack> savedHelmets = new HashMap<>();

    public Rozga(JavaPlugin plugin) {
        this.plugin = plugin;
        this.rozgaKey = new NamespacedKey(plugin, "rozga");
        this.temporaryHeadKey = new NamespacedKey(plugin, "rozga_glowka");
    }

    private boolean isRozga(ItemStack item) {
        if (item == null || item.getType() != Material.STICK) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        return meta != null
                && meta.getPersistentDataContainer().has(
                        rozgaKey,
                        PersistentDataType.BYTE
                );
    }

    private boolean isTemporaryHead(ItemStack item) {
        if (item == null || item.getType() != Material.PLAYER_HEAD) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        return meta != null
                && meta.getPersistentDataContainer().has(
                        temporaryHeadKey,
                        PersistentDataType.BYTE
                );
    }

    private ItemStack createSantaHead() {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        meta.setOwningPlayer(Bukkit.getOfflinePlayer("Santa"));

        meta.getPersistentDataContainer().set(
                temporaryHeadKey,
                PersistentDataType.BYTE,
                (byte) 1
        );

        head.setItemMeta(meta);

        return head;
    }

    private void restoreHelmet(Player player) {
        UUID uuid = player.getUniqueId();

        ItemStack originalHelmet = savedHelmets.remove(uuid);

        if (originalHelmet == null) {
            return;
        }

        ItemStack currentHelmet = player.getInventory().getHelmet();

        if (isTemporaryHead(currentHelmet)) {
            player.getInventory().setHelmet(originalHelmet);
        } else {
            Map<Integer, ItemStack> leftovers =
                    player.getInventory().addItem(originalHelmet);

            for (ItemStack leftover : leftovers.values()) {
                player.getWorld().dropItemNaturally(
                        player.getLocation(),
                        leftover
                );
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {

        if (!(event.getDamager() instanceof Player attacker)) {
            return;
        }

        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        ItemStack weapon = attacker.getInventory().getItemInMainHand();

        if (!isRozga(weapon)) {
            return;
        }

        UUID attackerUUID = attacker.getUniqueId();
        long now = System.currentTimeMillis();

        if (cooldowns.containsKey(attackerUUID)
                && cooldowns.get(attackerUUID) > now) {

            attacker.sendActionBar(
                    Component.text(
                            "Rózga jest na cooldownie!",
                            NamedTextColor.RED
                    )
            );

            return;
        }

        ItemStack victimHelmet = victim.getInventory().getHelmet();

        if (victimHelmet == null || victimHelmet.getType() == Material.AIR) {
            attacker.sendActionBar(
                    Component.text(
                            "Ten gracz nie ma hełmu!",
                            NamedTextColor.RED
                    )
            );

            return;
        }

        if (savedHelmets.containsKey(victim.getUniqueId())) {
            attacker.sendActionBar(
                    Component.text(
                            "Ten gracz ma już zdjęty hełm!",
                            NamedTextColor.RED
                    )
            );

            return;
        }

        // Cooldown 120 sekund
        cooldowns.put(attackerUUID, now + 120_000);
        attacker.setCooldown(Material.STICK, 2400);

        // Zapamiętanie oryginalnego hełmu
        ItemStack originalHelmet = victimHelmet.clone();
        savedHelmets.put(victim.getUniqueId(), originalHelmet);

        // Założenie zablokowanej główki Santa
        victim.getInventory().setHelmet(createSantaHead());

        // Komunikat dla atakującego
        attacker.sendActionBar(
                Component.text("Rózga ", NamedTextColor.GRAY)
                        .decorate(TextDecoration.BOLD)
                        .append(Component.text(
                                "zdjęła hełm!",
                                NamedTextColor.WHITE
                        ))
        );

        // Komunikat dla trafionego
        victim.sendActionBar(
                Component.text(
                        "Twój hełm został zdjęty na ",
                        NamedTextColor.RED
                ).append(Component.text(
                        "3.5 sekundy!",
                        NamedTextColor.YELLOW
                ))
        );

        // Przywrócenie hełmu po 3,5 sekundy
        Bukkit.getScheduler().runTaskLater(plugin, () -> {

            if (!victim.isOnline()) {
                return;
            }

            restoreHelmet(victim);

        }, 70L);
    }

    // Blokowanie zdejmowania główki w ekwipunku
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemStack helmet = player.getInventory().getHelmet();

        if (!isTemporaryHead(helmet)) {
            return;
        }

        if (event.getView().getType() == InventoryType.CRAFTING
                && event.getRawSlot() == 5) {

            event.setCancelled(true);
        }
    }

    // Blokowanie zdejmowania główki przez przeciąganie
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemStack helmet = player.getInventory().getHelmet();

        if (!isTemporaryHead(helmet)) {
            return;
        }

        if (event.getView().getType() == InventoryType.CRAFTING
                && event.getRawSlots().contains(5)) {

            event.setCancelled(true);
        }
    }

    // Śmierć podczas działania Rózgi
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {

        Player victim = event.getEntity();
        UUID uuid = victim.getUniqueId();

        if (!savedHelmets.containsKey(uuid)) {
            return;
        }

        ItemStack originalHelmet = savedHelmets.remove(uuid);

        // Usunięcie tymczasowej główki z dropu
        event.getDrops().removeIf(this::isTemporaryHead);

        // Wyrzucenie oryginalnego hełmu
        event.getDrops().add(originalHelmet);

        victim.getInventory().setHelmet(
                new ItemStack(Material.AIR)
        );
    }

    // Wyjście gracza z serwera
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        Player player = event.getPlayer();

        // Przywrócenie oryginalnego hełmu
        restoreHelmet(player);

        // Usunięcie cooldownu
        cooldowns.remove(player.getUniqueId());
    }
}
