
package pl.pykmcitems.plugin;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

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

        // Talizman Drop przenosi łupy do Ender Chesta zabójcy.
        if (zabojca != null && talizmanyManager.has(zabojca, "drop")) {
            List<ItemStack> przedmioty = new ArrayList<>();

            for (ItemStack item : event.getDrops()) {
                if (item != null && !item.getType().isAir()) {
                    przedmioty.add(item.clone());
                }
            }

            List<ItemStack> pozostale =
                    enderChest.addItems(zabojca, przedmioty);

            event.getDrops().clear();
            event.getDrops().addAll(pozostale);

            zabojca.sendMessage(
                    "§aTalizman Drop przeniósł łupy do twojego Ender Chesta!"
            );
        }

        // Talizmany założone przez ofiarę wypadają po śmierci.
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
