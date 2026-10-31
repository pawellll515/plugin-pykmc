
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
}
