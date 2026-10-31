
package pl.pykmcitems.plugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {

        getServer().getPluginManager().registerEvents(
                new Koniczynka(this), this
        );

        getServer().getPluginManager().registerEvents(
                new Rog(this), this
        );

        getServer().getPluginManager().registerEvents(
                new Glodomiot(this), this
        );

        getLogger().info("PykMCItems zostal wlaczony!");
    }

    @Override
    public void onDisable() {
        getLogger().info("PykMCItems zostal wylaczony!");
    }

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

        return true;
    }
}
