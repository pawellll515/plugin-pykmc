
package pykmcitems;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class Main extends JavaPlugin implements Listener {

    private NamespacedKey cloverKey;

    private final Map<UUID, Long> cooldowns = new HashMap<>();

    private static final long COOLDOWN = 30_000L;
    private static final int COOLDOWN_TICKS = 600;

    @Override
    public void onEnable() {
        cloverKey = new NamespacedKey(this, "koniczynka");

        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info("PykMCItems zostal wlaczony!");
    }

    @Override
    public void onDisable() {
        cooldowns.clear();
    }

    // KOMENDA /KONICZYNA
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!command.getName().equalsIgnoreCase("koniczyna")) {
            return false;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Tej komendy moze uzyc tylko gracz!");
            return true;
        }

        if (!player.hasPermission("pykmcitems.koniczyna")) {
            player.sendMessage(Component.text("Nie masz uprawnien!", NamedTextColor.RED));
            return true;
        }

        player.getInventory().addItem(createClover());

        player.sendMessage(Component.text("Otrzymales Koniczynke!", NamedTextColor.GREEN));

        return true;
    }

    // TWORZENIE KONICZYNKI
    private ItemStack createClover() {

        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(
                Component.text("Koniczynka", NamedTextColor.GREEN)
                        .decorate(TextDecoration.BOLD)
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
        );

        meta.lore(List.of(
                noItalic("Ta koniczynka po PPM nadaje:", NamedTextColor.GRAY),
                Component.empty().decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                noItalic("Efekty zakletego zlotego jablka", NamedTextColor.GOLD),
                noItalic("Strength II na 5 sekund", NamedTextColor.RED),
                noItalic("Haste II na 5 sekund", NamedTextColor.YELLOW),
                Component.empty().decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                noItalic("Przedmiot jest jednorazowy", NamedTextColor.RED),
                noItalic("Cooldown: 30 sekund", NamedTextColor.GRAY)
        ));

        meta.setCustomModelData(2);

        meta.getPersistentDataContainer().set(
                cloverKey,
                PersistentDataType.BYTE,
                (byte) 1
        );

        item.setItemMeta(meta);

        return item;
    }

    // TEKST BEZ KURSYWY
    private Component noItalic(String text, NamedTextColor color) {
        return Component.text(text, color)
                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    // SPRAWDZANIE KONICZYNKI
    private boolean isClover(ItemStack item) {

        if (item == null || item.getType() != Material.PAPER || !item.hasItemMeta()) {
            return false;
        }

        Byte marker = item.getItemMeta()
                .getPersistentDataContainer()
                .get(cloverKey, PersistentDataType.BYTE);

        return marker != null && marker == (byte) 1;
    }

    // UZYWANIE KONICZYNKI
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {

        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Action action = event.getAction();

        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();

        if (!isClover(item)) {
            return;
        }

        event.setCancelled(true);

        Player player = event.getPlayer();

        long now = System.currentTimeMillis();

        Long cooldownEnd = cooldowns.get(player.getUniqueId());

        // SPRAWDZANIE COOLDOWNU
        if (cooldownEnd != null && cooldownEnd > now) {
            return;
        }

        // USTAWIANIE COOLDOWNU
        cooldowns.put(player.getUniqueId(), now + COOLDOWN);

        // VANILLA COOLDOWN - 30 SEKUND
        player.setCooldown(item, COOLDOWN_TICKS);

        // EFEKTY
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 100, 1, true, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 100, 1, true, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 6000, 0, true, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 6000, 0, true, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 400, 1, true, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 3, true, true, true));

        // USUWANIE JEDNEJ KONICZYNKI
        if (item.getAmount() <= 1) {
            player.getInventory().setItemInMainHand(null);
        } else {
            item.setAmount(item.getAmount() - 1);
        }
    }

    // WYJSCIE GRACZA
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cooldowns.remove(event.getPlayer().getUniqueId());
    }

    // SMIERC GRACZA
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        cooldowns.remove(event.getEntity().getUniqueId());
    }
}
