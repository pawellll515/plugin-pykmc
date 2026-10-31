
package pl.pykmcitems.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class Itemy {

    private static Component tekst(String tekst, NamedTextColor kolor) {
        return Component.text(tekst, kolor)
                .decoration(TextDecoration.ITALIC, false);
    }

    private static Component pogrubiony(String tekst, NamedTextColor kolor) {
        return Component.text(tekst, kolor)
                .decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false);
    }

    // KONICZYNKA

    public static ItemStack koniczynka(JavaPlugin plugin) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(pogrubiony(
                "Koniczynka",
                NamedTextColor.GREEN
        ));

        List<Component> lore = new ArrayList<>();

        lore.add(Component.text()
                .append(tekst("Ta koniczynka po ", NamedTextColor.GRAY))
                .append(tekst("PPM", NamedTextColor.GREEN))
                .append(tekst(" nadaje:", NamedTextColor.GRAY))
                .build());

        lore.add(Component.empty());

        lore.add(tekst(
                "Efekty zaklętego złotego jabłka",
                NamedTextColor.GOLD
        ));

        lore.add(tekst(
                "Strength II na 5 sekund",
                NamedTextColor.RED
        ));

        lore.add(tekst(
                "Haste II na 5 sekund",
                NamedTextColor.YELLOW
        ));

        lore.add(Component.empty());

        lore.add(tekst(
                "Przedmiot jest jednorazowy",
                NamedTextColor.RED
        ));

        lore.add(tekst(
                "Cooldown: 30 sekund",
                NamedTextColor.GRAY
        ));

        meta.lore(lore);
        meta.setCustomModelData(2);

        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "koniczynka"),
                PersistentDataType.BYTE,
                (byte) 1
        );

        item.setItemMeta(meta);
        return item;
    }

    // RÓG NIEBIOS

    public static ItemStack rogNiebios(JavaPlugin plugin) {
        ItemStack item = new ItemStack(Material.GOAT_HORN);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(pogrubiony(
                "Róg Niebios",
                NamedTextColor.AQUA
        ));

        List<Component> lore = new ArrayList<>();

        lore.add(Component.text()
                .append(tekst("Ten róg po ", NamedTextColor.GRAY))
                .append(tekst("PPM", NamedTextColor.GREEN))
                .build());

        lore.add(Component.text()
                .append(tekst("wyrzuca graczy w obrębie ", NamedTextColor.GRAY))
                .append(tekst("5 kratek", NamedTextColor.AQUA))
                .build());

        lore.add(tekst(
                "wysoko w niebo",
                NamedTextColor.AQUA
        ));

        lore.add(Component.empty());

        lore.add(tekst(
                "Cooldown: 120 sekund",
                NamedTextColor.GRAY
        ));

        meta.lore(lore);
        meta.setCustomModelData(3);

        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "rog_niebios"),
                PersistentDataType.BYTE,
                (byte) 1
        );

        item.setItemMeta(meta);
        return item;
    }

    // GŁODOMIOT

    public static ItemStack glodomiot(JavaPlugin plugin) {
        ItemStack item = new ItemStack(Material.IRON_HOE);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(pogrubiony(
                "Głodomiot",
                NamedTextColor.GOLD
        ));

        List<Component> lore = new ArrayList<>();

        lore.add(Component.text()
                .append(tekst("Po ", NamedTextColor.GRAY))
                .append(tekst("uderzeniu przeciwnika", NamedTextColor.RED))
                .append(tekst(" głodzisz go,", NamedTextColor.GRAY))
                .build());

        lore.add(Component.text()
                .append(tekst("przez co jego hungerbar spada ", NamedTextColor.GRAY))
                .append(tekst("natychmiastowo", NamedTextColor.YELLOW))
                .build());

        lore.add(Component.empty());

        lore.add(tekst(
                "Cooldown: 1 minuta 30 sekund",
                NamedTextColor.GRAY
        ));

        meta.lore(lore);
        meta.setCustomModelData(3);

        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "glodomiot"),
                PersistentDataType.BYTE,
                (byte) 1
        );

        item.setItemMeta(meta);
        return item;
    }

    // ROZPADACZ

    public static ItemStack rozpadacz(JavaPlugin plugin) {
        ItemStack item = new ItemStack(Material.HEART_OF_THE_SEA);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(pogrubiony(
                "Rozpadacz",
                NamedTextColor.DARK_RED
        ));

        List<Component> lore = new ArrayList<>();

        lore.add(Component.text()
                .append(tekst("Po ", NamedTextColor.GRAY))
                .append(tekst("PPM", NamedTextColor.GREEN))
                .append(tekst(" zabiera każdemu w obrębie ", NamedTextColor.GRAY))
                .append(tekst("4 kratek", NamedTextColor.YELLOW))
                .build());

        lore.add(tekst(
                "33% życia",
                NamedTextColor.YELLOW
        ));

        lore.add(Component.empty());

        lore.add(tekst(
                "Cooldown: 1 minuta 30 sekund",
                NamedTextColor.GRAY
        ));

        meta.lore(lore);
        meta.setCustomModelData(3);

        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "rozpadacz"),
                PersistentDataType.BYTE,
                (byte) 1
        );

        item.setItemMeta(meta);
        return item;
    }


    // RÓZGA

    public static ItemStack rozga(JavaPlugin plugin) {
        ItemStack item = new ItemStack(Material.STICK);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(pogrubiony(
                "Rózga",
                NamedTextColor.GRAY
        ));

        List<Component> lore = new ArrayList<>();

        lore.add(Component.text()
                .append(tekst("Po ", NamedTextColor.GRAY))
                .append(tekst("uderzeniu przeciwnika ", NamedTextColor.RED))
                .append(tekst("usuwa mu hełm na ", NamedTextColor.GRAY))
                .append(tekst("3.5 sekundy", NamedTextColor.YELLOW))
                .build());

        lore.add(tekst(
                "Cooldown: 2 minuty",
                NamedTextColor.GRAY
        ));

        meta.lore(lore);
        meta.setCustomModelData(2);

        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "rozga"),
                PersistentDataType.BYTE,
                (byte) 1
        );

        item.setItemMeta(meta);
        return item;
    }
        // ŚWIĄTECZNA KATANA

    public static ItemStack swiatecznaKatana(JavaPlugin plugin) {
        ItemStack item = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(pogrubiony(
                "Świąteczna Katana",
                NamedTextColor.GREEN
        ));

        List<Component> lore = new ArrayList<>();

        lore.add(tekst(
                "Po PPM + SHIFT na gracza teleportuje",
                NamedTextColor.GRAY
        ));

        lore.add(tekst(
                "cię 2 bloki za cel i zamraża go",
                NamedTextColor.GRAY
        ));

        lore.add(tekst(
                "zadając mu 4 serca",
                NamedTextColor.GRAY
        ));

        lore.add(Component.empty());

        lore.add(tekst(
                "Cooldown: 1 minuta 30 sekund",
                NamedTextColor.GRAY
        ));

        meta.lore(lore);
        meta.setCustomModelData(2);

        meta.addEnchant(
                org.bukkit.enchantments.Enchantment.SHARPNESS,
                6,
                true
        );

        meta.addEnchant(
                org.bukkit.enchantments.Enchantment.FIRE_ASPECT,
                3,
                true
        );

        meta.addEnchant(
                org.bukkit.enchantments.Enchantment.UNBREAKING,
                4,
                true
        );

        meta.addItemFlags(
                org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS
        );

        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "katana"),
                PersistentDataType.BYTE,
                (byte) 1
        );

        item.setItemMeta(meta);
        return item;
    }
}
