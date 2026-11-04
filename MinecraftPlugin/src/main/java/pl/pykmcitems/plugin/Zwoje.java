
package pl.pykmcitems.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class Zwoje implements Listener {

    private final JavaPlugin plugin;

    public Zwoje(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public enum TypZwoju {
        OSLONY("zwoj_oslony", "Zwój Osłony I", NamedTextColor.GREEN),
        REGENERACJI("zwoj_regeneracji", "Zwój Regeneracji I", NamedTextColor.LIGHT_PURPLE),
        OGNIA("zwoj_ognia", "Zwój Ognia I", NamedTextColor.GOLD),
        SZYBKOSCI("zwoj_szybkosci", "Zwój Szybkości I", NamedTextColor.AQUA),
        CIEMNOSCI("zwoj_ciemosci", "Zwój Ciemności I", NamedTextColor.DARK_PURPLE),
        JASNOSCI("zwoj_jasnosci", "Zwój Jasności I", NamedTextColor.YELLOW);

        public final String klucz;
        public final String nazwa;
        public final NamedTextColor kolor;

        TypZwoju(String klucz, String nazwa, NamedTextColor kolor) {
            this.klucz = klucz;
            this.nazwa = nazwa;
            this.kolor = kolor;
        }
    }

    private NamespacedKey key(String nazwa) {
        return new NamespacedKey(plugin, nazwa);
    }

    private Component tekst(String tekst, NamedTextColor kolor) {
        return Component.text(tekst, kolor)
                .decoration(TextDecoration.ITALIC, false);
    }

    private String plain(Component component) {
        return net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
                .plainText()
                .serialize(component);
    }

    private boolean maLinieZwoju(ItemStack item, TypZwoju typ) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        List<Component> lore = item.getItemMeta().lore();

        if (lore == null) {
            return false;
        }

        for (Component linia : lore) {
            if (plain(linia).equals(typ.nazwa)) {
                return true;
            }
        }

        return false;
    }

    public boolean maZwój(ItemStack item, TypZwoju typ) {
        return maLinieZwoju(item, typ);
    }

    private TypZwoju rozpoznajZwój(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();

        for (TypZwoju typ : TypZwoju.values()) {
            if (meta.getPersistentDataContainer().has(
                    key(typ.klucz),
                    PersistentDataType.BYTE
            )) {
                return typ;
            }
        }

        return null;
    }

    private boolean jestKlataMeduzy(ItemStack item) {
        if (item == null || item.getType() != Material.NETHERITE_CHESTPLATE
                || !item.hasItemMeta()) {
            return false;
        }

        return item.getItemMeta().getPersistentDataContainer().has(
                key("klata_meduzy"),
                PersistentDataType.BYTE
        );
    }

    private boolean pasuje(ItemStack item, TypZwoju typ) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }

        Material material = item.getType();

        return switch (typ) {
            case OSLONY ->
                    material == Material.LEATHER_HELMET
                    || material == Material.CHAINMAIL_HELMET
                    || material == Material.IRON_HELMET
                    || material == Material.GOLDEN_HELMET
                    || material == Material.DIAMOND_HELMET
                    || material == Material.NETHERITE_HELMET
                    || material == Material.TURTLE_HELMET;

            case REGENERACJI ->
                    material == Material.LEATHER_CHESTPLATE
                    || material == Material.CHAINMAIL_CHESTPLATE
                    || material == Material.IRON_CHESTPLATE
                    || material == Material.GOLDEN_CHESTPLATE
                    || material == Material.DIAMOND_CHESTPLATE
                    || material == Material.NETHERITE_CHESTPLATE;

            case OGNIA ->
                    material == Material.LEATHER_LEGGINGS
                    || material == Material.CHAINMAIL_LEGGINGS
                    || material == Material.IRON_LEGGINGS
                    || material == Material.GOLDEN_LEGGINGS
                    || material == Material.DIAMOND_LEGGINGS
                    || material == Material.NETHERITE_LEGGINGS;

            case SZYBKOSCI ->
                    material == Material.LEATHER_BOOTS
                    || material == Material.CHAINMAIL_BOOTS
                    || material == Material.IRON_BOOTS
                    || material == Material.GOLDEN_BOOTS
                    || material == Material.DIAMOND_BOOTS
                    || material == Material.NETHERITE_BOOTS;

            case CIEMNOSCI, JASNOSCI ->
                    material == Material.WOODEN_SWORD
                    || material == Material.STONE_SWORD
                    || material == Material.IRON_SWORD
                    || material == Material.GOLDEN_SWORD
                    || material == Material.DIAMOND_SWORD
                    || material == Material.NETHERITE_SWORD;
        };
    }

    private boolean czyLiniaJestUlepszeniem(Component component) {
        String tekst = plain(component);

        for (TypZwoju typ : TypZwoju.values()) {
            if (tekst.equals(typ.nazwa)) {
                return true;
            }
        }

        return false;
    }

    private ItemStack przygotujWynik(ItemStack item, TypZwoju typ) {
        ItemStack wynik = item.clone();
        ItemMeta meta = wynik.getItemMeta();

        if (meta == null) {
            return null;
        }

        List<Component> lore = meta.lore();

        if (lore == null) {
            lore = new ArrayList<>();
        } else {
            lore = new ArrayList<>(lore);
        }

        if (maLinieZwoju(item, typ)) {
            return null;
        }

        // Zachowujemy stare lore i oddzielamy sekcję ulepszeń pustą linią.
        boolean maJuzInnyZwój = false;

        for (Component linia : lore) {
            if (czyLiniaJestUlepszeniem(linia)) {
                maJuzInnyZwój = true;
                break;
            }
        }

        if (!maJuzInnyZwój && !lore.isEmpty()
                && !plain(lore.get(lore.size() - 1)).isEmpty()) {
            lore.add(Component.empty());
        }

        // Kolejne ulepszenia są dopisywane bez dodatkowych pustych linii.
        lore.add(tekst(typ.nazwa, typ.kolor));
        meta.lore(lore);

        // Trim jest tylko wizualny. Nie służy do wykrywania działania zwoju.
        if (meta instanceof ArmorMeta armorMeta) {
            if (!jestKlataMeduzy(item)) {
                armorMeta.setTrim(
                        new ArmorTrim(TrimMaterial.GOLD, TrimPattern.FLOW)
                );
            }
        }

        wynik.setItemMeta(meta);
        return wynik;
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilInventory inventory = event.getInventory();

        ItemStack item = inventory.getItem(0);
        ItemStack zwój = inventory.getItem(1);

        TypZwoju typ = rozpoznajZwój(zwój);

        if (typ == null) {
            return;
        }

        if (item == null || item.getType() == Material.AIR
                || !pasuje(item, typ)
                || maLinieZwoju(item, typ)) {
            event.setResult(null);
            return;
        }

        ItemStack wynik = przygotujWynik(item, typ);
        event.setResult(wynik);

        if (wynik != null) {
            inventory.setRepairCost(1);
        }
    }
}
