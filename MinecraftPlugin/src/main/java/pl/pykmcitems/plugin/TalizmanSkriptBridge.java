
package pl.pykmcitems.plugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TalizmanSkriptBridge implements CommandExecutor {

    private final Main plugin;

    public TalizmanSkriptBridge(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {

        // Komendy mogą być wywoływane wyłącznie przez konsolę,
        // np. przez Skript: execute console command.
        if (!(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage("§cTa komenda jest przeznaczona wyłącznie dla konsoli.");
            return true;
        }

        String commandName = command.getName().toLowerCase();

        if (commandName.equals("pykmc-internal-clear")) {
            return obsluzCzyszczenie(sender, args);
        }

        if (commandName.equals("pykmc-internal-give-talisman")) {
            return obsluzNadawanie(sender, args);
        }

        return true;
    }

    private boolean obsluzCzyszczenie(
            CommandSender sender,
            String[] args
    ) {

        if (args.length != 1) {
            sender.sendMessage("Użycie: /pykmc-internal-clear <gracz>");
            return true;
        }

        Player player = plugin.getServer().getPlayerExact(args[0]);

        if (player == null) {
            sender.sendMessage("Gracz nie jest online: " + args[0]);
            return true;
        }

        // Czyści zwykły ekwipunek, zbroję i drugą rękę.
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
        player.getInventory().setItemInOffHand(null);

        // Czyści zapisane, założone talizmany bez wyrzucania ich.
        plugin.getTalizmanyManager().clear(player, false);

        // Czyści powiększony Ender Chest.
        plugin.getPowiekszonyEnderChest().clear(player);

        player.updateInventory();

        sender.sendMessage("Wyczyszczono ekwipunek, talizmany i Ender Chest gracza " + player.getName() + ".");
        player.sendMessage("§cTwój ekwipunek, talizmany i Ender Chest zostały wyczyszczone.");

        return true;
    }

    private boolean obsluzNadawanie(
            CommandSender sender,
            String[] args
    ) {

        if (args.length < 2 || args.length > 3) {
            sender.sendMessage("Użycie: /pykmc-internal-give-talisman <gracz> <id> [wartosc]");
            return true;
        }

        Player player = plugin.getServer().getPlayerExact(args[0]);

        if (player == null) {
            sender.sendMessage("Gracz nie jest online: " + args[0]);
            return true;
        }

        String id = args[1].toLowerCase();
        Double value = null;

        boolean requiresValue =
                id.equals("odpornosc_3")
                || id.equals("sila_3")
                || id.equals("swiateczny_czas");

        if (requiresValue) {

            if (args.length != 3) {
                sender.sendMessage("Ten talizman wymaga wartości.");
                return true;
            }

            try {
                value = Double.parseDouble(args[2].replace(",", "."));
            } catch (NumberFormatException exception) {
                sender.sendMessage("Niepoprawna wartość: " + args[2]);
                return true;
            }

            if (!czyPoprawnaWartosc(id, value)) {
                sender.sendMessage("Wartość poza dozwolonym zakresem dla: " + id);
                return true;
            }

        } else if (args.length != 2) {
            sender.sendMessage("Ten talizman nie przyjmuje dodatkowej wartości.");
            return true;
        }

        ItemStack talisman = stworzTalizman(id, value);

        if (talisman == null) {
            sender.sendMessage("Nieznany identyfikator talizmanu: " + id);
            return true;
        }

        var leftovers = player.getInventory().addItem(talisman);

        for (ItemStack item : leftovers.values()) {
            player.getWorld().dropItemNaturally(
                    player.getLocation(),
                    item
            );
        }

        sender.sendMessage("Nadano talizman " + id + " graczowi " + player.getName() + ".");
        player.sendMessage("§aOtrzymałeś talizman!");

        return true;
    }

    private ItemStack stworzTalizman(String id, Double value) {

        return switch (id) {
            case "odpornosc_1" -> Itemy.talizmanOdpornosciI(plugin);
            case "odpornosc_2" -> Itemy.talizmanOdpornosciII(plugin);
            case "odpornosc_3" -> value == null ? null
                    : Itemy.talizmanOdpornosciIII(plugin, value);

            case "sila_1" -> Itemy.talizmanSilyI(plugin);
            case "sila_2" -> Itemy.talizmanSilyII(plugin);
            case "sila_3" -> value == null ? null
                    : Itemy.talizmanSilyIII(plugin, value);

            case "slodkosci_3" -> Itemy.talizmanSlodkosciIII(plugin);
            case "drop" -> Itemy.talizmanDropu(plugin);

            case "swiateczny_czas" -> value == null ? null
                    : Itemy.talizmanSwiatecznegoCzasu(plugin, value);

            default -> null;
        };
    }

    private boolean czyPoprawnaWartosc(String id, double value) {

        return switch (id) {
            case "odpornosc_3" -> value >= 10.0 && value <= 15.0;
            case "sila_3" -> value >= 8.0 && value <= 15.0;
            case "swiateczny_czas" -> value >= 5.0 && value <= 20.0;
            default -> false;
        };
    }
}
