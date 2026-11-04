package pl.pykmcitems.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

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

        meta.displayName(pogrubiony("Koniczynka", NamedTextColor.GREEN));

        List<Component> lore = new ArrayList<>();

        lore.add(Component.text()
                .append(tekst("Ta koniczynka po ", NamedTextColor.GRAY))
                .append(tekst("PPM", NamedTextColor.GREEN))
                .append(tekst(" nadaje:", NamedTextColor.GRAY))
                .build());

        lore.add(Component.empty());
        lore.add(tekst("Efekty zaklętego złotego jabłka", NamedTextColor.GOLD));
        lore.add(tekst("Strength II na 5 sekund", NamedTextColor.RED));
        lore.add(tekst("Haste II na 5 sekund", NamedTextColor.YELLOW));
        lore.add(Component.empty());
        lore.add(tekst("Przedmiot jest jednorazowy", NamedTextColor.RED));
        lore.add(tekst("Cooldown: 30 sekund", NamedTextColor.GRAY));

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

        meta.displayName(pogrubiony("Róg Niebios", NamedTextColor.AQUA));

        List<Component> lore = new ArrayList<>();

        lore.add(Component.text()
                .append(tekst("Ten róg po ", NamedTextColor.GRAY))
                .append(tekst("PPM", NamedTextColor.GREEN))
                .build());

        lore.add(Component.text()
                .append(tekst("wyrzuca graczy w obrębie ", NamedTextColor.GRAY))
                .append(tekst("5 kratek", NamedTextColor.AQUA))
                .build());

        lore.add(tekst("wysoko w niebo", NamedTextColor.AQUA));
        lore.add(Component.empty());
        lore.add(tekst("Cooldown: 120 sekund", NamedTextColor.GRAY));

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

        meta.displayName(pogrubiony("Głodomiot", NamedTextColor.GOLD));

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
        lore.add(tekst("Cooldown: 1 minuta 30 sekund", NamedTextColor.GRAY));

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

        meta.displayName(pogrubiony("Rozpadacz", NamedTextColor.DARK_RED));

        List<Component> lore = new ArrayList<>();

        lore.add(Component.text()
                .append(tekst("Po ", NamedTextColor.GRAY))
                .append(tekst("PPM", NamedTextColor.GREEN))
                .append(tekst(" zabiera każdemu w obrębie ", NamedTextColor.GRAY))
                .append(tekst("4 kratek", NamedTextColor.YELLOW))
                .build());

        lore.add(tekst("33% życia", NamedTextColor.YELLOW));
        lore.add(Component.empty());
        lore.add(tekst("Cooldown: 1 minuta 30 sekund", NamedTextColor.GRAY));

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

        meta.displayName(pogrubiony("Rózga", NamedTextColor.GRAY));

        List<Component> lore = new ArrayList<>();

        lore.add(Component.text()
                .append(tekst("Po ", NamedTextColor.GRAY))
                .append(tekst("uderzeniu przeciwnika ", NamedTextColor.RED))
                .append(tekst("usuwa mu hełm na ", NamedTextColor.GRAY))
                .append(tekst("3.5 sekundy", NamedTextColor.YELLOW))
                .build());

        lore.add(tekst("Cooldown: 2 minuty", NamedTextColor.GRAY));

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

        meta.displayName(pogrubiony("Świąteczna Katana", NamedTextColor.GREEN));

        List<Component> lore = new ArrayList<>();

        lore.add(Component.text()
                .append(tekst("Po ", NamedTextColor.GRAY))
                .append(tekst("PPM + SHIFT", NamedTextColor.GREEN))
                .append(tekst(" na gracza", NamedTextColor.GRAY))
                .build());

        lore.add(Component.text()
                .append(tekst("teleportuje cię ", NamedTextColor.GRAY))
                .append(tekst("2 bloki", NamedTextColor.AQUA))
                .append(tekst(" za cel", NamedTextColor.GRAY))
                .build());

        lore.add(Component.text()
                .append(tekst("i zamraża go, zadając ", NamedTextColor.GRAY))
                .append(tekst("4 serca", NamedTextColor.RED))
                .build());

        lore.add(Component.empty());

        lore.add(Component.text()
                .append(tekst("Cooldown: ", NamedTextColor.GRAY))
                .append(tekst("1 minuta 30 sekund", NamedTextColor.GREEN))
                .build());

        meta.lore(lore);
        meta.setCustomModelData(2);

        meta.addEnchant(Enchantment.SHARPNESS, 6, true);
        meta.addEnchant(Enchantment.FIRE_ASPECT, 3, true);
        meta.addEnchant(Enchantment.UNBREAKING, 4, true);

        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "katana"),
                PersistentDataType.BYTE,
                (byte) 1
        );

        item.setItemMeta(meta);
        return item;
    }

    // TOPÓR WYKLUCZENIA

    public static ItemStack toporWykluczenia(JavaPlugin plugin) {
        ItemStack item = new ItemStack(Material.IRON_AXE);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(pogrubiony("Topór Wykluczenia", NamedTextColor.GOLD));

        List<Component> lore = new ArrayList<>();

        lore.add(Component.text()
                .append(tekst("Po ", NamedTextColor.GRAY))
                .append(tekst("uderzeniu przeciwnika", NamedTextColor.RED))
                .append(tekst(" blokuje", NamedTextColor.GRAY))
                .build());

        lore.add(Component.text()
                .append(tekst("używanie mu ", NamedTextColor.GRAY))
                .append(tekst("ekwipunku", NamedTextColor.YELLOW))
                .append(tekst(" na ", NamedTextColor.GRAY))
                .append(tekst("8 sekund", NamedTextColor.GREEN))
                .build());

        lore.add(Component.empty());

        lore.add(Component.text()
                .append(tekst("Cooldown: ", NamedTextColor.GRAY))
                .append(tekst("2 minuty", NamedTextColor.YELLOW))
                .build());

        meta.lore(lore);
        meta.setCustomModelData(3);

        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "topor_wykluczenia"),
                PersistentDataType.BYTE,
                (byte) 1
        );

        item.setItemMeta(meta);
        return item;
    }

    // LODOŁAP

    public static ItemStack lodolap(JavaPlugin plugin) {
        ItemStack item = new ItemStack(Material.SNOWBALL);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(pogrubiony("Lodołap", NamedTextColor.AQUA));

        List<Component> lore = new ArrayList<>();

        lore.add(Component.text()
                .append(tekst("Po ", NamedTextColor.GRAY))
                .append(tekst("trafieniu", NamedTextColor.YELLOW))
                .append(tekst(" w gracza", NamedTextColor.GRAY))
                .build());

        lore.add(Component.text()
                .append(tekst("blokuje mu ", NamedTextColor.GRAY))
                .append(tekst("używanie elytry", NamedTextColor.RED))
                .build());

        lore.add(tekst("na 10 sekund", NamedTextColor.GRAY));
        lore.add(Component.empty());

        lore.add(Component.text()
                .append(tekst("Po rzucie: ", NamedTextColor.GRAY))
                .append(tekst("3 sekundy cooldown", NamedTextColor.YELLOW))
                .build());

        lore.add(Component.text()
                .append(tekst("Po trafieniu: ", NamedTextColor.GRAY))
                .append(tekst("60 sekund cooldown", NamedTextColor.YELLOW))
                .build());

        meta.lore(lore);
        meta.setCustomModelData(3);

        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "lodolap_item"),
                PersistentDataType.BYTE,
                (byte) 1
        );

        item.setItemMeta(meta);
        return item;
    }

    // HYDRA

    public static ItemStack hydra(JavaPlugin plugin) {
        ItemStack item = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(pogrubiony("Hydra", NamedTextColor.DARK_BLUE));

        List<Component> lore = new ArrayList<>();

        lore.add(Component.text()
                .append(tekst("Po ", NamedTextColor.GRAY))
                .append(tekst("⚔ uderzeniu przeciwnika ", NamedTextColor.RED))
                .append(tekst("ładuje", NamedTextColor.GRAY))
                .build());

        lore.add(Component.text()
                .append(tekst("się ", NamedTextColor.GRAY))
                .append(tekst("✦ Moc Hydry ", NamedTextColor.AQUA))
                .append(tekst("która nadaje bonusy", NamedTextColor.GRAY))
                .build());

        lore.add(Component.empty());

        lore.add(Component.text()
                .append(tekst("☠ ", NamedTextColor.DARK_GRAY))
                .append(tekst("Maks. poziom naładowania: ", NamedTextColor.GRAY))
                .append(tekst("14", NamedTextColor.YELLOW))
                .build());

        lore.add(Component.empty());

        lore.add(Component.text()
                .append(tekst("⚔ ", NamedTextColor.RED))
                .append(tekst("Damage: ", NamedTextColor.GRAY))
                .append(tekst("Maks. 5", NamedTextColor.YELLOW))
                .build());

        lore.add(Component.text()
                .append(tekst("⚡ ", NamedTextColor.AQUA))
                .append(tekst("Szybkość: ", NamedTextColor.GRAY))
                .append(tekst("Maks. 3", NamedTextColor.YELLOW))
                .build());

        lore.add(Component.text()
                .append(tekst("⛏ ", NamedTextColor.GOLD))
                .append(tekst("Haste: ", NamedTextColor.GRAY))
                .append(tekst("Maks. 3", NamedTextColor.YELLOW))
                .build());

        lore.add(Component.text()
                .append(tekst("🛡 ", NamedTextColor.GREEN))
                .append(tekst("Anty Knockback: ", NamedTextColor.GRAY))
                .append(tekst("Maks. -30%", NamedTextColor.YELLOW))
                .build());

        meta.lore(lore);
        meta.setCustomModelData(3);

        meta.addEnchant(Enchantment.SHARPNESS, 6, true);
        meta.addEnchant(Enchantment.UNBREAKING, 4, true);
        meta.addEnchant(Enchantment.FIRE_ASPECT, 3, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "hydra"),
                PersistentDataType.BYTE,
                (byte) 1
        );

        item.setItemMeta(meta);
        return item;
    }

    // BUDA

    public static ItemStack buda(JavaPlugin plugin) {
        ItemStack item = new ItemStack(Material.CHAIN);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(pogrubiony("Buda", NamedTextColor.YELLOW));

        List<Component> lore = new ArrayList<>();

        lore.add(Component.text()
                .append(tekst("Po ", NamedTextColor.GRAY))
                .append(tekst("PPM na gracza", NamedTextColor.GREEN))
                .build());

        lore.add(tekst("zamyka go w klatce 1v1", NamedTextColor.GRAY));
        lore.add(Component.empty());

        lore.add(Component.text()
                .append(tekst("Cooldown: ", NamedTextColor.GRAY))
                .append(tekst("1 minuta 30 sekund", NamedTextColor.YELLOW))
                .build());

        meta.lore(lore);
        meta.setCustomModelData(3);

        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "buda"),
                PersistentDataType.BYTE,
                (byte) 1
        );

        item.setItemMeta(meta);
        return item;
    }

    // ZŁAMANE SERCE

    public static ItemStack zlamaneSerce(JavaPlugin plugin) {
        ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(pogrubiony("Złamane Serce", NamedTextColor.RED));

        List<Component> lore = new ArrayList<>();

        lore.add(Component.text()
                .append(tekst("Po ", NamedTextColor.GRAY))
                .append(tekst("PPM", NamedTextColor.GREEN))
                .append(tekst(" blokuje przeciwnikowi perły", NamedTextColor.GRAY))
                .build());

        lore.add(Component.text()
                .append(tekst("na ", NamedTextColor.GRAY))
                .append(tekst("10 sekund", NamedTextColor.YELLOW))
                .build());

        lore.add(Component.text()
                .append(tekst("oraz przywołuje ", NamedTextColor.GRAY))
                .append(tekst("5 Kupidynów", NamedTextColor.RED))
                .build());

        lore.add(Component.empty());

        lore.add(Component.text()
                .append(tekst("Cooldown: ", NamedTextColor.GRAY))
                .append(tekst("1 minuta 30 sekund", NamedTextColor.YELLOW))
                .build());

        meta.lore(lore);
        meta.setCustomModelData(3);

        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "zlamane_serce"),
                PersistentDataType.BYTE,
                (byte) 1
        );

        item.setItemMeta(meta);
        return item;
    }

    // ZWOJE

    private static ItemStack stworzZwoj(
            JavaPlugin plugin,
            String klucz,
            String nazwa,
            NamedTextColor kolor,
            int customModelData,
            String opis
    ) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(pogrubiony(nazwa, kolor));

        List<Component> lore = new ArrayList<>();
        lore.add(tekst(opis, NamedTextColor.GRAY));
        lore.add(Component.empty());
        lore.add(tekst("Nakładaj przez kowadło", NamedTextColor.YELLOW));
        lore.add(tekst("Ulepszenie: I", NamedTextColor.GREEN));

        meta.lore(lore);
        meta.setCustomModelData(customModelData);

        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, klucz),
                PersistentDataType.BYTE,
                (byte) 1
        );

        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack zwojOslony(JavaPlugin plugin) {
        return stworzZwoj(
                plugin,
                "zwoj_oslony",
                "Zwój Osłony I",
                NamedTextColor.GREEN,
                1,
                "Ochrona zbroi: -25% obrażeń"
        );
    }

    public static ItemStack zwojRegeneracji(JavaPlugin plugin) {
        return stworzZwoj(
                plugin,
                "zwoj_regeneracji",
                "Zwój Regeneracji I",
                NamedTextColor.LIGHT_PURPLE,
                2,
                "Regeneruje 1 HP co 7 sekund"
        );
    }

    public static ItemStack zwojOgnia(JavaPlugin plugin) {
        return stworzZwoj(
                plugin,
                "zwoj_ognia",
                "Zwój Ognia I",
                NamedTextColor.GOLD,
                3,
                "Stała odporność na ogień"
        );
    }

    public static ItemStack zwojSzybkosci(JavaPlugin plugin) {
        return stworzZwoj(
                plugin,
                "zwoj_szybkosci",
                "Zwój Szybkości I",
                NamedTextColor.AQUA,
                4,
                "Stała Szybkość I"
        );
    }

    public static ItemStack zwojCiemnosci(JavaPlugin plugin) {
        return stworzZwoj(
                plugin,
                "zwoj_ciemosci",
                "Zwój Ciemności I",
                NamedTextColor.DARK_PURPLE,
                5,
                "3% szansy na Wither I na 5 sekund"
        );
    }

    public static ItemStack zwojJasnosci(JavaPlugin plugin) {
        return stworzZwoj(
                plugin,
                "zwoj_jasnosci",
                "Zwój Jasności I",
                NamedTextColor.YELLOW,
                6,
                "3% szansy na Glowing i +10% obrażeń"
        );
    }

    // KLATA MEDUZY

    public static ItemStack klataMeduzy(JavaPlugin plugin) {
        ItemStack item = new ItemStack(Material.NETHERITE_CHESTPLATE);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(pogrubiony("Klata Meduzy", NamedTextColor.AQUA));

        List<Component> lore = new ArrayList<>();

        lore.add(Component.empty());
        lore.add(pogrubiony("Właściwości:", NamedTextColor.AQUA));

        lore.add(Component.text()
                .append(tekst("- Szansa na ", NamedTextColor.GRAY))
                .append(tekst("Niewidzialność", NamedTextColor.WHITE))
                .append(tekst(" na 30 s przy otrzymaniu obrażeń ", NamedTextColor.GRAY))
                .append(tekst("(ON)", NamedTextColor.GREEN))
                .build());

        lore.add(Component.text()
                .append(tekst("- Szansa na uderzenie ", NamedTextColor.GRAY))
                .append(tekst("potężnego pioruna", NamedTextColor.AQUA))
                .append(tekst(" w przeciwnika", NamedTextColor.GRAY))
                .build());

        lore.add(Component.text()
                .append(tekst("- Stały efekt ", NamedTextColor.GRAY))
                .append(tekst("Siły I", NamedTextColor.RED))
                .build());

        meta.lore(lore);
        meta.setCustomModelData(3);

        meta.addEnchant(Enchantment.PROTECTION, 5, true);
        meta.addEnchant(Enchantment.UNBREAKING, 4, true);

        if (meta instanceof ArmorMeta armorMeta) {
            armorMeta.setTrim(
                    new ArmorTrim(TrimMaterial.DIAMOND, TrimPattern.FLOW)
            );
        }

        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "klata_meduzy"),
                PersistentDataType.BYTE,
                (byte) 1
        );

        item.setItemMeta(meta);
        return item;
    }
}
