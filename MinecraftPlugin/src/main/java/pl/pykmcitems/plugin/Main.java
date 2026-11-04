package pl.pykmcitems.plugin;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private ZlamaneSerce zlamaneSerce;
    private KlataMeduzy klataMeduzy;
    private Zwoje zwoje;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new Koniczynka(this), this);
        getServer().getPluginManager().registerEvents(new Rog(this), this);
        getServer().getPluginManager().registerEvents(new Glodomiot(this), this);
        getServer().getPluginManager().registerEvents(new Rozpadacz(this), this);
        getServer().getPluginManager().registerEvents(new Rozga(this), this);
        getServer().getPluginManager().registerEvents(new SwiatecznaKatana(this), this);
        getServer().getPluginManager().registerEvents(new ToporWykluczenia(this), this);
        getServer().getPluginManager().registerEvents(new Hydra(this), this);
        getServer().getPluginManager().registerEvents(new Lodolap(this), this);
        getServer().getPluginManager().registerEvents(new Buda(this), this);

        zlamaneSerce = new ZlamaneSerce(this);

        klataMeduzy = new KlataMeduzy(this);
        getServer().getPluginManager().registerEvents(klataMeduzy, this);

        zwoje = new Zwoje(this);
        getServer().getPluginManager().registerEvents(zwoje, this);

        getLogger().info("PykMCItems zostal wlaczony!");
    }

    @Override
    public void onDisable() {
        if (zlamaneSerce != null) {
            zlamaneSerce.cleanup();
        }

        if (klataMeduzy != null) {
            klataMeduzy.cleanup();
        }

        getLogger().info("PykMCItems zostal wylaczony!");
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) 

        if (command.getName().equalsIgnoreCase("ustawglowke")) {
            if (!player.hasPermission("pykmcitems.ustawglowke")) {
                player.sendMessage("§cNie masz uprawnien do tej komendy!");
                return true;
            }

            ItemStack heldItem = player.getInventory().getItemInMainHand();

            if (heldItem.getType() != Material.PLAYER_HEAD) {
                player.sendMessage("§cMusisz trzymac glowke gracza w glównej rece!");
                return true;
            }

            getConfig().set("rozga.head", heldItem.clone());
            saveConfig();

            player.sendMessage("§aPomyslnie zapisano glowke Rózgi!");
            player.sendMessage("§7Od teraz Rózga bedzie uzywac tej glowki.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("koniczyna")) {
            player.getInventory().addItem(Itemy.koniczynka(this));
            player.sendMessage("§aOtrzymales Koniczynke!");
            return true;
        }

        if (command.getName().equalsIgnoreCase("rog")) {
            player.getInventory().addItem(Itemy.rogNiebios(this));
            player.sendMessage("§bOtrzymales Rog Niebios!");
            return true;
        }

        if (command.getName().equalsIgnoreCase("glodomiot")) {
            player.getInventory().addItem(Itemy.glodomiot(this));
            player.sendMessage("§6Otrzymales Glodomiot!");
            return true;
        }

        if (command.getName().equalsIgnoreCase("rozpadacz")) {
            player.getInventory().addItem(Itemy.rozpadacz(this));
            player.sendMessage("§4Otrzymales Rozpadacz!");
            return true;
        }

        if (command.getName().equalsIgnoreCase("rozga")) {
            player.getInventory().addItem(Itemy.rozga(this));
            player.sendMessage("§7Otrzymales Rozge!");
            return true;
        }

        if (command.getName().equalsIgnoreCase("katana")) {
            player.getInventory().addItem(Itemy.swiatecznaKatana(this));
            player.sendMessage("§aOtrzymales Świąteczną Katanę!");
            return true;
        }

        if (command.getName().equalsIgnoreCase("topor")) {
            if (!player.hasPermission("pykmcitems.topor")) {
                player.sendMessage("§cNie masz uprawnien do tej komendy!");
                return true;
            }

            player.getInventory().addItem(Itemy.toporWykluczenia(this));
            player.sendMessage("§aOtrzymales §6§lTopór Wykluczenia§a!");
            return true;
        }

        if (command.getName().equalsIgnoreCase("hydra")) {
            if (!player.hasPermission("pykmcitems.hydra")) {
                player.sendMessage("§cNie masz uprawnien do tej komendy!");
                return true;
            }

            player.getInventory().addItem(Itemy.hydra(this));
            player.sendMessage("§aOtrzymales §1§lHydre§a!");
            return true;
        }

        if (command.getName().equalsIgnoreCase("lodolap")) {
            if (!player.hasPermission("pykmcitems.lodolap")) {
                player.sendMessage("§cNie masz uprawnien do tej komendy!");
                return true;
            }

            player.getInventory().addItem(Itemy.lodolap(this));
            player.sendMessage("§aOtrzymales §b§lLodołap§a!");
            return true;
        }

        if (command.getName().equalsIgnoreCase("buda")) {
            if (!player.hasPermission("pykmcitems.buda")) {
                player.sendMessage("§cNie masz uprawnien do tej komendy!");
                return true;
            }

            player.getInventory().addItem(Itemy.buda(this));
            player.sendMessage("§aOtrzymales §eBudę§a!");
            return true;
        }

        if (command.getName().equalsIgnoreCase("zlamane")) {
            if (!player.hasPermission("pykmcitems.zlamane")) {
                player.sendMessage("§cNie masz uprawnien do tej komendy!");
                return true;
            }

            player.getInventory().addItem(ZlamaneSerce.createItem(this));
            player.sendMessage("§aOtrzymales §c§lZlamane Serce§a!");
            return true;
        }

        if (command.getName().equalsIgnoreCase("meduza")) {
            if (!player.hasPermission("pykmcitems.meduza")) {
                player.sendMessage("§cNie masz uprawnien do tej komendy!");
                return true;
            }

            player.getInventory().addItem(Itemy.klataMeduzy(this));
            player.sendMessage("§aOtrzymales §b§lKlata Meduzy§a!");
            return true;
        }

        return true;
    }
}
