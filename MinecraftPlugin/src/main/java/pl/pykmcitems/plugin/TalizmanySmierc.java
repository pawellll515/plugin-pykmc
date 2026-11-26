
package pl.pykmcitems.plugin;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class TalizmanySmierc implements Listener {

    private final TalizmanyManager talizmanyManager;
    private final PowiekszonyEnderChest enderChest;

    public TalizmanySmierc(
            TalizmanyManager talizmanyManager,
            PowiekszonyEnderChest enderChest
    ) {
        this.talizmanyManager = talizmanyManager;
        this.enderChest = enderChest;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {

        Player ofiara = event.getEntity();
        Player zabojca = ofiara.getKiller();

        // ==========================================
        // TALIZMAN DROP ZABÓJCY
        // ==========================================

        if (zabojca != null
                && talizmanyManager.has(zabojca, "drop")) {

            Collection<ItemStack> przedmioty = new ArrayList<>();

            for (ItemStack item : event.getDrops()) {
                if (item != null && !item.getType().isAir()) {
                    przedmioty.add(item.clone());
                }
            }

            Map<Integer, ItemStack> pozostale =
                    enderChest.addItems(zabojca, przedmioty);

            // Usuwamy z listy śmierci tylko przedmioty,
            // które faktycznie zmieściły się w Ender Cheście.
            List<ItemStack> doUsuniecia = new ArrayList<>();

            for (ItemStack oryginal : event.getDrops()) {
                if (oryginal == null || oryginal.getType().isAir()) {
                    continue;
                }

                int pozostalaIlosc = 0;

                for (ItemStack item : pozostale.values()) {
                    if (item.isSimilar(oryginal)) {
                        pozostalaIlosc += item.getAmount();
                    }
                }

                int przeniesionaIlosc =
                        oryginal.getAmount() - pozostalaIlosc;

                if (przeniesionaIlosc >= oryginal.getAmount()) {
                    doUsuniecia.add(oryginal);
                } else if (przeniesionaIlosc > 0) {
                    oryginal.setAmount(
                            oryginal.getAmount() - przeniesionaIlosc
                    );
                }
            }

            event.getDrops().removeAll(doUsuniecia);

            zabojca.sendMessage(
                    "§a✦ §7Łupy gracza §e" + ofiara.getName()
                            + " §7zostały przeniesione do twojego Ender Chesta!"
            );
        }

        // ==========================================
        // WYPADANIE TALIZMANÓW OFIARY
        // ==========================================

        List<ItemStack> zalozone =
                talizmanyManager.getEquipped(ofiara);

        talizmanyManager.clear(ofiara, true);

        if (!zalozone.isEmpty()) {
            ofiara.sendMessage(
                    "§cTwoje założone talizmany wypadły po śmierci!"
            );
        }
    }
}
