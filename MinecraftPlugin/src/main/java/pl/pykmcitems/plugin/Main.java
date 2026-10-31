
package pl.pykmcitems.plugin;

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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Main extends JavaPlugin implements Listener {

    private NamespacedKey cloverKey;
    private NamespacedKey hornKey;

    private final Map<UUID, Long> cloverCooldowns = new HashMap<>();
    private final Map<UUID, Long> hornCooldowns = new HashMap<>();

    @Override
    public void onEnable() {
        cloverKey = new NamespacedKey(this, "koniczynka");
        hornKey = new NamespacedKey(this, "rog_niebios");

        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info("PykMCItems zostal wlaczony!");
    }

    @Override
    public void onDisable() {
        getLogger().info("PykMCItems zostal wylaczony!");
    }

    // KOMENDY

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Tej komendy moze uzyc tylko gracz!");
            return true;
        }

        if (command.getName().equalsIgnoreCase("koniczyna")) {
            player.getInventory().addItem(createClover());
            player.sendMessage("§aOtrzymales Koniczynke!");
            return true;
        }

        if (command.getName().equalsIgnoreCase("rog")) {
            player.getInventory().addItem(createHorn());
            player.sendMessage("§bOtrzymales Rog Niebios!");
            return true;
        }

        return true;
    }

    // TWORZENIE KONICZYNKI

    private ItemStack createClover() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(
                Component.text("Koniczynka", NamedTextColor.GREEN)
                        .decorate(TextDecoration.BOLD)
                        .decoration(TextDecoration.ITALIC, false)
        );

        List<Component> lore = new ArrayList<>();

        lore.add(noItalic(Component.text(
                "Ta koniczynka po PPM nadaje:",
                NamedTextColor.GRAY
        )));

        lore.add(Component.empty());

        lore.add(noItalic(Component.text(
                "Efekty zakletego zlotej jablka",
                NamedTextColor.GREEN
        )));

        lore.add(noItalic(Component.text(
                "Strength II na 5 sekund",
                NamedTextColor.GRAY
        )));

        lore.add(noItalic(Component.text(
                "Haste II na 5 sekund",
                NamedTextColor.GRAY
        )));

        lore.add(Component.empty());

        lore.add(noItalic(Component.text(
                "Przedmiot jest jednorazowy",
                NamedTextColor.RED
        )));

        lore.add(noItalic(Component.text(
                "Cooldown: 30 sekund",
                NamedTextColor.GRAY
        )));

        meta.lore(lore);
        meta.setCustomModelData(2);

        meta.getPersistentDataContainer().set(
                cloverKey,
                PersistentDataType.BYTE,
                (byte) 1
        );

        item.setItemMeta(meta);
        return item;
    }

    // TWORZENIE ROGU NIEBIOS

    private ItemStack createHorn() {
        ItemStack item = new ItemStack(Material.GOAT_HORN);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(
                Component.text("Rog Niebios", NamedTextColor.AQUA)
                        .decorate(TextDecoration.BOLD)
                        .decoration(TextDecoration.ITALIC, false)
        );

        List<Component> lore = new ArrayList<>();

        lore.add(noItalic(Component.text(
                "Ten rog po PPM wyrzuca graczy",
                NamedTextColor.GRAY
        )));

        lore.add(noItalic(Component.text(
                "w obrebie 5 kratek w niebo",
                NamedTextColor.AQUA
        )));

        lore.add(Component.empty());

        lore.add(noItalic(Component.text(
                "Cooldown: 120 sekund",
                NamedTextColor.GRAY
        )));

        meta.lore(lore);
        meta.setCustomModelData(3);

        meta.getPersistentDataContainer().set(
                hornKey,
                PersistentDataType.BYTE,
                (byte) 1
        );

        item.setItemMeta(meta);
        return item;
    }

    // USUWANIE KURSYWY Z TEKSTU

    private Component noItalic(Component component) {
        return component.decoration(TextDecoration.ITALIC, false);
    }

    // SPRAWDZANIE PRZEDMIOTOW

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

    // OBSLUGA PPM

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

        // KONICZYNKA

        if (isClover(item)) {
            event.setCancelled(true);

            UUID uuid = player.getUniqueId();
            long now = System.currentTimeMillis();

            if (cloverCooldowns.containsKey(uuid)
                    && cloverCooldowns.get(uuid) > now) {
                return;
            }

            cloverCooldowns.put(uuid, now + 30_000);

            // Vanilla cooldown: 30 sekund
            player.setCooldown(Material.PAPER, 600);

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

            // Usuniecie jednej Koniczynki
            removeOneItem(player);

            return;
        }

        // ROG NIEBIOS

        if (isHorn(item)) {
            event.setCancelled(true);

            UUID uuid = player.getUniqueId();
            long now = System.currentTimeMillis();

            if (hornCooldowns.containsKey(uuid)
                    && hornCooldowns.get(uuid) > now) {
                return;
            }

            hornCooldowns.put(uuid, now + 120_000);

            // Vanilla cooldown: 120 sekund
            player.setCooldown(Material.GOAT_HORN, 2400);

            // Tytul dla osoby uzywajacej rogu
            player.showTitle(
                    Title.title(
                            Component.empty(),
                            Component.text(
                                    "Rog Niebios wyrzucil graczy",
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

                // Tytul dla trafionego gracza
                target.showTitle(
                        Title.title(
                                Component.empty(),
                                Component.text(
                                        "Rog Niebios cie wyrzucil",
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

    // USUWANIE JEDNEGO PRZEDMIOTU

    private void removeOneItem(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getAmount() <= 1) {
            player.getInventory().setItemInMainHand(
                    new ItemStack(Material.AIR)
            );
        } else {
            item.setAmount(item.getAmount() - 1);
        }
    }

    // CZYSZCZENIE COOLDOWNOW

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        cloverCooldowns.remove(uuid);
        hornCooldowns.remove(uuid);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        UUID uuid = event.getEntity().getUniqueId();

        cloverCooldowns.remove(uuid);
        hornCooldowns.remove(uuid);
    }
}
