package pl.pykmcitems.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

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
import java.util.Locale;

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

        meta.displayName(Component.text("Koniczynka")
                .color(NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();

        lore.add(tekst("Po kliknieciu prawym, otrzymasz", NamedTextColor.GRAY));
        lore.add(Component.text()
                .append(tekst("efekty ", NamedTextColor.GRAY))
                .append(tekst("koksa ", NamedTextColor.GOLD))
                .append(tekst("oraz efekty: ", NamedTextColor.GRAY))
                .build());
        lore.add(Component.text()
                .append(tekst("» ", NamedTextColor.GRAY))
                .append(tekst("Siła 2 ", NamedTextColor.RED))
                .append(tekst("na ", NamedTextColor.GRAY))
                .append(tekst("20s ", NamedTextColor.RED))
                .build());
        lore.add(Component.text()
                .append(tekst("» ", NamedTextColor.GRAY))
                .append(tekst("Haste 1 ", NamedTextColor.GOLD))
                .append(tekst("na ", NamedTextColor.GRAY))
                .append(tekst("10s ", NamedTextColor.GOLD))
                .build());
        lore.add(Component.empty());
        lore.add(tekst("Przedmiot jest jednorazowy", NamedTextColor.GRAY));
        lore.add(Component.text()
                .append(tekst("Przedmiot pochodzi z eventu ", NamedTextColor.GRAY))
                .append(tekst("Świętego Patryka", NamedTextColor.GREEN))
                .build());

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

        lore.add(tekst("Po kliknięciu prawym, tworzy sie obszar", NamedTextColor.GRAY));
        lore.add(Component.text()
                .append(tekst("w którym gracze tracą ", NamedTextColor.GRAY))
                .append(tekst("serca", NamedTextColor.RED))
                .build());
        lore.add(tekst("", NamedTextColor.YELLOW));
        lore.add(Component.empty());
        lore.add(Component.text()
                .append(tekst("Cooldown: ", NamedTextColor.GRAY))
                .append(tekst("60s", NamedTextColor.YELLOW))
                .build());

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

        meta.displayName(Component.text("Rózga")
                .color(net.kyori.adventure.text.format.TextColor.fromHexString("#E6E0C2"))
                .decoration(TextDecoration.ITALIC, false));

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

        lore.add(Component.empty());
        
        lore.add(tekst("Każde Twoje uderzenie zadaje drugiego sweepa,", NamedTextColor.GRAY));
        lore.add(Component.text()
                .append(tekst("który zadaje ", NamedTextColor.GRAY))
                .append(tekst("8% ", NamedTextColor.RED))
                .append(tekst("uderzenia", NamedTextColor.GRAY))
                .build());
        
        lore.add(Component.empty());
        
        lore.add(Component.text()
                .append(tekst("Klikając ", NamedTextColor.GRAY))
                .append(tekst("SHIFT + PRAWY ", NamedTextColor.YELLOW))
                .append(tekst("robisz ", NamedTextColor.GRAY))
                .append(tekst("wyrafinowany krok ", NamedTextColor.YELLOW))
                .build());
        lore.add(tekst("który przecina przeciwników zadając", NamedTextColor.GRAY));
        lore.add(Component.text()
                .append(tekst("im obrażenia oraz efekt ", NamedTextColor.GRAY))
                .append(tekst("zamrożenia ", NamedTextColor.DARK_AQUA))
                .append(tekst("na ", NamedTextColor.GRAY))
                .append(tekst("2s ", NamedTextColor.YELLOW))
                .build());

        lore.add(Component.empty());

        lore.add(Component.text()
                .append(tekst("Cooldown: ", NamedTextColor.GRAY))
                .append(tekst("40s", NamedTextColor.YELLOW))
                .build());

        meta.lore(lore);
        meta.setCustomModelData(2);

        meta.addEnchant(Enchantment.SHARPNESS, 6, true);
        meta.addEnchant(Enchantment.FIRE_ASPECT, 3, true);
        meta.addEnchant(Enchantment.UNBREAKING, 4, true);


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

        meta.displayName(Component.text("Hydra")
                .color(net.kyori.adventure.text.format.TextColor.fromHexString("#3700FF"))
                .decoration(TextDecoration.ITALIC, false));

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

        meta.displayName(Component.text("Złamane Serce")
                .color(net.kyori.adventure.text.format.TextColor.fromHexString("#E547D0"))
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();

        lore.add(Component.empty());
        lore.add(Component.text()
                .append(tekst("Kliknij prawym myszki na wroga, ", NamedTextColor.GRAY))
                .build());

        lore.add(Component.text()
                .append(tekst("aby nasłać na niego ", NamedTextColor.GRAY))
                .append(tekst("5 potężnych kupidynów", NamedTextColor.YELLOW))
                .build());
        lore.add(Component.text()
                .append(tekst("oraz ", NamedTextColor.GRAY))
                .append(tekst("zablokować mu używanie pereł ", NamedTextColor.RED))
                .append(tekst("na ", NamedTextColor.GRAY))
                .append(tekst("10s ", NamedTextColor.YELLOW))
                .build());

        lore.add(Component.empty());

        lore.add(Component.text()
                .append(tekst("Cooldown: ", NamedTextColor.GRAY))
                .append(tekst("45s", NamedTextColor.YELLOW))
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

    // BERŁO DRAKULI
    public static ItemStack berloDrakuli(JavaPlugin plugin) {
        ItemStack item = new ItemStack(Material.STICK);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Berło Drakuli")
                .color(net.kyori.adventure.text.format.TextColor.fromHexString("#EA0000"))
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();

        lore.add(Component.empty());
        lore.add(tekst("Po użyciu zmniejszasz swoją postać", NamedTextColor.GRAY));

        lore.add(Component.text()
                .append(tekst("o ", NamedTextColor.GRAY))
                .append(tekst("50%", NamedTextColor.YELLOW))
                .append(tekst(" na ", NamedTextColor.GRAY))
                .append(tekst("15s", NamedTextColor.YELLOW))
                .build());

        lore.add(Component.empty());

        lore.add(Component.text()
                .append(tekst("Cooldown: ", NamedTextColor.GRAY))
                .append(tekst("2min", NamedTextColor.YELLOW))
                .build());

        meta.lore(lore);
        meta.setCustomModelData(1);

        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "berlo_drakuli"),
                PersistentDataType.BYTE,
                (byte) 1
        );

        item.setItemMeta(meta);
        return item;
    }

    // OBSIKANA ŚNIEŻKA
    public static ItemStack obsikanaSniezka(JavaPlugin plugin) {
        ItemStack item = new ItemStack(Material.SNOWBALL);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(
                Component.text("Obsikana Śnieżka", NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false)
        );

        List<Component> lore = new ArrayList<>();

        lore.add(Component.empty());
        lore.add(Component.text()
                .append(tekst("Po trafieniu w przeciwnika ", NamedTextColor.GRAY))
                .append(tekst("zamraża ", NamedTextColor.YELLOW))
                .append(tekst("go na ", NamedTextColor.GRAY))
                .append(tekst("2.5s", NamedTextColor.YELLOW))
                .build());

        lore.add(Component.text()
                .append(tekst("oraz uniemożliwia mu ", NamedTextColor.GRAY))
                .append(tekst("jedzenie ", NamedTextColor.RED))
                .append(tekst("przez czas efektu", NamedTextColor.GRAY))
                .build());

        lore.add(Component.empty());
        lore.add(tekst("Przedmiot jest jednorazowy", NamedTextColor.GRAY));

        meta.lore(lore);
        meta.setCustomModelData(2);

        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "obsikana_sniezka"),
                PersistentDataType.BYTE,
                (byte) 1
        );

        item.setItemMeta(meta);
        return item;
    }

    // ŁUK WIEDŹMY
    public static ItemStack lukWiedzmy(JavaPlugin plugin) {
        ItemStack item = new ItemStack(Material.BOW);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(
                Component.text("Łuk Wiedźmy", NamedTextColor.LIGHT_PURPLE)
                        .decoration(TextDecoration.ITALIC, false)
        );

        List<Component> lore = new ArrayList<>();

        lore.add(tekst("Po trafieniu przeciwnika:", NamedTextColor.GRAY));

        lore.add(Component.text()
                .append(tekst("» ", NamedTextColor.GRAY))
                .append(tekst("Oślepienie ", NamedTextColor.DARK_PURPLE))
                .append(tekst("na 2 sekundy", NamedTextColor.GRAY))
                .build());

        lore.add(Component.text()
                .append(tekst("» ", NamedTextColor.GRAY))
                .append(tekst("Zamrożenie ", NamedTextColor.AQUA))
                .append(tekst("na 2.5 sekundy", NamedTextColor.GRAY))
                .build());

        lore.add(Component.empty());
        lore.add(tekst("Cooldown: 60 sekund", NamedTextColor.YELLOW));
        lore.add(tekst("Przedmiot pochodzi z eventu Halloween 2024", NamedTextColor.GRAY));

        meta.lore(lore);
        meta.setCustomModelData(2);

        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "luk_wiedzmy"),
                PersistentDataType.BYTE,
                (byte) 1
        );

        item.setItemMeta(meta);
        return item;
    }

    // ==========================================
    // TALIZMANY
    // ==========================================

    private static final LegacyComponentSerializer TALIZMAN_SERIALIZER =
            LegacyComponentSerializer.legacyAmpersand();

    private static String procent(double wartosc) {
        return String.format(Locale.US, "%.1f", wartosc).replace(".", ",") + "%";
    }

    private static Component talizmanText(String tekst) {
        return TALIZMAN_SERIALIZER.deserialize(tekst)
                .decoration(TextDecoration.BOLD, false)
                .decoration(TextDecoration.ITALIC, false);
    }

    private static ItemStack stworzTalizman(
            JavaPlugin plugin,
            Material material,
            int customModelData,
            String id,
            String nazwa,
            double wartosc,
            String... opis
    ) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(talizmanText(nazwa));

        List<Component> lore = new ArrayList<>();

        for (String linia : opis) {
            lore.add(linia.isEmpty()
                    ? Component.empty()
                    : talizmanText(linia));
        }

        meta.lore(lore);
        meta.setCustomModelData(customModelData);

        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "talizman_id"),
                PersistentDataType.STRING,
                id
        );

        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "talizman_wartosc"),
                PersistentDataType.DOUBLE,
                wartosc
        );

        item.setItemMeta(meta);
        return item;
    }

    // ODPORNOŚĆ I
    public static ItemStack talizmanOdpornosciI(JavaPlugin plugin) {
        return stworzTalizman(plugin, Material.WHITE_DYE, 13,
                "odpornosc_1",
                "&b🛡 Talizman odporności I",
                5.0,
                "&b| +5% &7odporności na obrażenia."
        );
    }

    // ODPORNOŚĆ II
    public static ItemStack talizmanOdpornosciII(JavaPlugin plugin) {
        return stworzTalizman(plugin, Material.WHITE_DYE, 12,
                "odpornosc_2",
                "&b🛡 Talizman odporności II",
                8.0,
                "&b| +8% &7odporności na obrażenia."
        );
    }

    // ODPORNOŚĆ III
    public static ItemStack talizmanOdpornosciIII(JavaPlugin plugin, double wartosc) {
        return stworzTalizman(plugin, Material.WHITE_DYE, 11,
                "odpornosc_3",
                "&b🛡 Talizman odporności III",
                wartosc,
                "&b| +" + procent(wartosc) + " &7odporności na obrażenia.",
                "&b| &7Zakres losowania: &b+10% &7- &b15%",
                "",
                "&e☆ &bBonus maksymalnego poziomu:",
                "&7Kiedy przyjmujesz obrażenia,",
                "&7masz szansę otrzymać &ezłote serce &7absorpcji."
        );
    }

    // SIŁA I
    public static ItemStack talizmanSilyI(JavaPlugin plugin) {
        return stworzTalizman(plugin, Material.WHITE_DYE, 7,
                "sila_1",
                "&d⚔ Talizman siły I",
                4.0,
                "&d| +4% &7bonusowych obrażeń."
        );
    }

    // SIŁA II
    public static ItemStack talizmanSilyII(JavaPlugin plugin) {
        return stworzTalizman(plugin, Material.WHITE_DYE, 6,
                "sila_2",
                "&d⚔ Talizman siły II",
                6.0,
                "&d| +6% &7bonusowych obrażeń."
        );
    }

    // SIŁA III
    public static ItemStack talizmanSilyIII(JavaPlugin plugin, double wartosc) {
        return stworzTalizman(plugin, Material.WHITE_DYE, 5,
                "sila_3",
                "&d⚔ Talizman siły III",
                wartosc,
                "&d| +" + procent(wartosc) + " &7bonusowych obrażeń.",
                "&d| &7Zakres losowania: &d+8% &7- &d15%",
                "",
                "&e☆ &bBonus maksymalnego poziomu:",
                "&7Po zjedzeniu czegokolwiek istnieje szansa,",
                "&7na otrzymanie efektu &eszybkości &7na &e10 sekund."
        );
    }

    // SŁODKOŚCI III
    public static ItemStack talizmanSlodkosciIII(JavaPlugin plugin) {
        return stworzTalizman(plugin, Material.ORANGE_DYE, 1,
                "slodkosci_3",
                "&b🍬 Talizman słodkości III",
                0.0,
                "&e| +1s &7czasu trwania regeneracji ze złotych jabłek.",
                "",
                "&7» &fZwiększa obrażenia krytyczne."
        );
    }

    // TALIZMAN DROPU
    public static ItemStack talizmanDropu(JavaPlugin plugin) {
        return stworzTalizman(plugin, Material.LAPIS_LAZULI, 274,
                "drop",
                "&b✉ Talizman dropu",
                0.0,
                "&7» Po &czabiciu &7gracza jego przedmioty",
                "&7» &7trafiają do wolnego miejsca",
                "&7» &7w Twoim &denderchescie."
        );
    }

    // ŚWIĄTECZNY CZAS
    public static ItemStack talizmanSwiatecznegoCzasu(JavaPlugin plugin, double wartosc) {
        return stworzTalizman(plugin, Material.WHITE_DYE, 14,
                "swiateczny_czas",
                "&f❄ Talizman Świątecznego czasu",
                wartosc,
                "&f| -" + procent(wartosc) + " &7cooldownu na użycie przedmiotów eventowych.",
                "",
                "&7» &fTalizman eventowy - posiada tylko 1 poziom.",
                "&f| &7Zakres losowania: &e+5% &7- &e20%"
        );
    }
}
