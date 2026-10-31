
package pykmcitems;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;

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
    private NamespacedKey hornKey;

    private final Map<UUID, Long> cloverCooldowns = new HashMap<>();
    private final Map<UUID, Long> hornCooldowns = new HashMap<>();

    private static final long CLOVER_COOLDOWN = 30_000L;
    private static final long HORN_COOLDOWN = 120_000L;

    @Override
    public void onEnable() {

        cloverKey = new NamespacedKey(this, "koniczynka");
        hornKey = new NamespacedKey(this, "rog_niebios");

        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info("PykMCItems zostal wlaczony!");
    }

    @Override
    public void onDisable() {
        cloverCooldowns.clear();
        hornCooldowns.clear();
    }

    // KOMENDY
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Tej komendy moze uzyc tylko gracz!");
            return true;
        }

        if (command.getName().equalsIgnoreCase("koniczyna")) {

            if (!player.hasPermission("pykmcitems.koniczyna")) {
                player.sendMessage(Component.text("Nie masz uprawnien!", NamedTextColor.RED));
                return true;
            }

            player.getInventory().addItem(createClover());
            player.sendMessage(Component.text("Otrzymales Koniczynke!", NamedTextColor.GREEN));

            return true;
        }

        if (command.getName().equalsIgnoreCase("rog")) {

            if (!player.hasPermission("pykmcitems.rog")) {
                player.sendMessage(Component.text("Nie masz uprawnien!", NamedTextColor.RED));
                return true;
            }

            player.getInventory().addItem(createHorn());
            player.sendMessage(Component.text("Otrzymales Rog Niebios!", NamedTextColor.AQUA));

            return true;
        }

        return false;
    }

    // KONICZYNKA
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
                Component.empty(),
                noItalic("Efekty zakletego zlotego jablka", NamedTextColor.GOLD),
                noItalic("Strength II na 5 sekund", NamedTextColor.RED),
                noItalic("Haste II na 5 sekund", NamedTextColor.YELLOW),
                Component.empty(),
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

    // ROG NIEBIOS
    private ItemStack createHorn() {

        ItemStack item = new ItemStack(Material.GOAT_HORN);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(
                Component.text("Róg Niebios", NamedTextColor.AQUA)
                        .decorate(TextDecoration.BOLD)
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
        );

        meta.lore(List.of(
                noItalic("Ten rog po PPM wyrzuca graczy", NamedTextColor.GRAY),
                noItalic("w obrebie 5 kratek w niebo", NamedTextColor.GRAY)
        ));

        meta.setCustomModelData(3);

        meta.getPersistentDataContainer().set(
                hornKey,
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

    // SPRAWDZANIE ROGU
    private boolean isHorn(ItemStack item) {

        if (item == null || item.getType() != Material.GOAT_HORN || !item.hasItemMeta()) {
            return false;
        }

        Byte marker = item.getItemMeta()
                .getPersistentDataContainer()
                .get(hornKey, PersistentDataType.BYTE);

        return marker != null && marker == (byte) 1;
    }

    // OBSLUGA PPM
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

        // KONICZYNKA
        if (isClover(item)) {

            event.setCancelled(true);

            Player player = event.getPlayer();

            long now = System.currentTimeMillis();

            Long cooldownEnd = cloverCooldowns.get(player.getUniqueId());

            if (cooldownEnd != null && cooldownEnd > now) {
                return;
            }

            cloverCooldowns.put(player.getUniqueId(), now + CLOVER_COOLDOWN);

            player.setCooldown(Material.PAPER, 600);

            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 100, 1, true, true, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 100, 1, true, true, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 6000, 0, true, true, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 6000, 0, true, true, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 400, 1, true, true, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 3, true, true, true));

            if (item.getAmount() <= 1) {
                player.getInventory().setItemInMainHand(null);
            } else {
                item.setAmount(item.getAmount() - 1);
            }

            return;
        }

        // ROG NIEBIOS
        if (isHorn(item)) {

            event.setCancelled(true);

            Player player = event.getPlayer();

            long now = System.currentTimeMillis();

            Long cooldownEnd = hornCooldowns.get(player.getUniqueId());

            if (cooldownEnd != null && cooldownEnd > now) {
                return;
            }

            hornCooldowns.put(player.getUniqueId(), now + HORN_COOLDOWN);

            // VANILLA COOLDOWN - 120 SEKUND
            player.setCooldown(Material.GOAT_HORN, 2400);

            // TITLE DLA UZYWAJACEGO
            player.showTitle(
                    Title.title(
                            Component.empty(),
                            Component.text("Róg Niebios wyrzucił graczy", NamedTextColor.GRAY)
                                    .decorate(TextDecoration.BOLD)
                                    .color(NamedTextColor.AQUA),
                    Title.Times.times(
                            java.time.Duration.ofMillis(250),
                            java.time.Duration.ofSeconds(3),
                            java.time.Duration.ofMillis(500)
                    )
            );

            // GRACZE W PROMIENIU 5 KRATEK
            for (Player target : player.getWorld().getPlayers()) {

                if (target.equals(player)) {
                    continue;
                }

                if (target.getLocation().distanceSquared(player.getLocation()) > 25) {
                    continue;
                }

                // LEWITACJA III (AMPLIFIER 49 = POZIOM 50)
                target.addPotionEffect(
                        new PotionEffect(PotionEffectType.LEVITATION, 60, 49, true, true, true)
                );

                // TITLE DLA TRAFIONEGO GRACZA
                target.showTitle(
                        Title.title(
                                Component.empty(),
                                Component.text("Róg Niebios cię wyrzucił", NamedTextColor.GRAY)
                                        .decorate(TextDecoration.BOLD)
                                        .color(NamedTextColor.AQUA),
                                Title.Times.times(
                                        java.time.Duration.ofMillis(250),
                                        java.time.Duration.ofSeconds(3),
                                        java.time.Duration.ofMillis(500)
                                )
                        )
                );
            }
        }
    }

    // WYJSCIE GRACZA
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cloverCooldowns.remove(event.getPlayer().getUniqueId());
        hornCooldowns.remove(event.getPlayer().getUniqueId());
    }

    // SMIERC GRACZA
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        cloverCooldowns.remove(event.getEntity().getUniqueId());
        hornCooldowns.remove(event.getEntity().getUniqueId());
    }
}
