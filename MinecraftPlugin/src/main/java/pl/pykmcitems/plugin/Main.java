
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
    private BerloDrakuli berloDrakuli;
    private ObsikanaSniezka obsikanaSniezka;
    private LukWiedzmy lukWiedzmy;

    private CooldownScoreboard cooldownScoreboard;
    private ItemCooldownManager itemCooldownManager;
    private TalizmanyGUI talizmanyGUI;
    private TalizmanyManager talizmanyManager;
    private PowiekszonyEnderChest powiekszonyEnderChest;

    @Override
    public void onEnable() {

        cooldownScoreboard = new CooldownScoreboard(this);

        itemCooldownManager = new ItemCooldownManager(
                this,
                cooldownScoreboard
        );

        getServer().getPluginManager().registerEvents(
                itemCooldownManager,
                this
        );

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

        berloDrakuli = new BerloDrakuli(this);
        getServer().getPluginManager().registerEvents(berloDrakuli, this);

        obsikanaSniezka = new ObsikanaSniezka(this);
        getServer().getPluginManager().registerEvents(obsikanaSniezka, this);

        lukWiedzmy = new LukWiedzmy(this);
        getServer().getPluginManager().registerEvents(lukWiedzmy, this);

        zlamaneSerce = new ZlamaneSerce(this);

        klataMeduzy = new KlataMeduzy(this);
        getServer().getPluginManager().registerEvents(klataMeduzy, this);

        // SYSTEM TALIZMANÓW
        talizmanyManager = new TalizmanyManager(this);

        getServer().getPluginManager().registerEvents(
                talizmanyManager,
                this
        );

        talizmanyGUI = new TalizmanyGUI(this, talizmanyManager);

        getServer().getPluginManager().registerEvents(
                talizmanyGUI,
                this
        );

        talizmanyManager.updateAllIcons();

        // BONUSY TALIZMANÓW
        TalizmanBonusy talizmanBonusy =
                new TalizmanBonusy(this, talizmanyManager);

        getServer().getPluginManager().registerEvents(
                talizmanBonusy,
                this
        );

        // POWIĘKSZONY ENDER CHEST
        powiekszonyEnderChest = new PowiekszonyEnderChest(this);

        getServer().getPluginManager().registerEvents(
                powiekszonyEnderChest,
                this
        );
        TalizmanySmierc talizmanySmierc =
        new TalizmanySmierc(talizmanyManager, powiekszonyEnderChest);

        getServer().getPluginManager().registerEvents(
        talizmanySmierc,
        this
        );

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

        if (berloDrakuli != null) {
            berloDrakuli.cleanup();
        }

        if (obsikanaSniezka != null) {
            obsikanaSniezka.cleanup();
        }

        if (lukWiedzmy != null) {
            lukWiedzmy.cleanup();
        }

        if (itemCooldownManager != null) {
            itemCooldownManager.save();
        }

        if (talizmanyManager != null) {
            talizmanyManager.save();
        }

        if (powiekszonyEnderChest != null) {
            powiekszonyEnderChest.saveAll();
        }

        if (cooldownScoreboard != null) {
            cooldownScoreboard.shutdown();
        }

        getLogger().info("PykMCItems zostal wylaczony!");
    }

    public CooldownScoreboard getCooldownScoreboard() {
        return cooldownScoreboard;
    }

    public ItemCooldownManager getItemCooldownManager() {
        return itemCooldownManager;
    }

    public TalizmanyManager getTalizmanyManager() {
        return talizmanyManager;
    }

    public PowiekszonyEnderChest getPowiekszonyEnderChest() {
        return powiekszonyEnderChest;
    }

    // ==========================================
    // TWORZENIE TALIZMANÓW Z KOMENDY
    // ==========================================

    private ItemStack stworzTalizmanZKomendy(String id, Double wartosc) {
        return switch (id.toLowerCase()) {
            case "odpornosc_1" -> Itemy.talizmanOdpornosciI(this);
            case "odpornosc_2" -> Itemy.talizmanOdpornosciII(this);
            case "odpornosc_3" -> wartosc == null ? null
                    : Itemy.talizmanOdpornosciIII(this, wartosc);

            case "sila_1" -> Itemy.talizmanSilyI(this);
            case "sila_2" -> Itemy.talizmanSilyII(this);
            case "sila_3" -> wartosc == null ? null
                    : Itemy.talizmanSilyIII(this, wartosc);

            case "slodkosci_3" -> Itemy.talizmanSlodkosciIII(this);
            case "drop" -> Itemy.talizmanDropu(this);

            case "swiateczny_czas" -> wartosc == null ? null
                    : Itemy.talizmanSwiatecznegoCzasu(this, wartosc);

            default -> null;
        };
    }

    private boolean czyPoprawnaWartosc(String id, double wartosc) {
        return switch (id.toLowerCase()) {
            case "odpornosc_3" -> wartosc >= 10.0 && wartosc <= 15.0;
            case "sila_3" -> wartosc >= 8.0 && wartosc <= 15.0;
            case "swiateczny_czas" -> wartosc >= 5.0 && wartosc <= 20.0;
            default -> false;
        };
    }

    private boolean wymagaWartosci(String id) {
        return id.equalsIgnoreCase("odpornosc_3")
                || id.equalsIgnoreCase("sila_3")
                || id.equalsIgnoreCase("swiateczny_czas");
    }

    private boolean obsluzDajTalizman(CommandSender sender, String[] args) {

        if (args.length < 2 || args.length > 3) {
            sender.sendMessage("§cUżycie: /dajtalizman <gracz> <id> [wartosc]");
            return true;
        }

        if (sender instanceof Player player
                && !player.hasPermission("pykmcitems.dajtalizman")) {
            player.sendMessage("§cNie masz uprawnień do tej komendy!");
            return true;
        }

        Player odbiorca = getServer().getPlayerExact(args[0]);

        if (odbiorca == null) {
            sender.sendMessage("§cTen gracz nie jest online!");
            return true;
        }

        String id = args[1].toLowerCase();
        Double wartosc = null;

        if (wymagaWartosci(id)) {

            if (args.length != 3) {
                sender.sendMessage("§cTen talizman wymaga podania wartości procentowej!");
                return true;
            }

            try {
                wartosc = Double.parseDouble(args[2].replace(",", "."));
            } catch (NumberFormatException exception) {
                sender.sendMessage("§cPodana wartość nie jest poprawną liczbą!");
                return true;
            }

            if (!czyPoprawnaWartosc(id, wartosc)) {
                sender.sendMessage("§cNieprawidłowa wartość dla tego talizmanu!");
                return true;
            }

        } else if (args.length != 2) {
            sender.sendMessage("§cTen talizman nie przyjmuje wartości procentowej!");
            return true;
        }

        ItemStack talizman = stworzTalizmanZKomendy(id, wartosc);

        if (talizman == null) {
            sender.sendMessage("§cNieznany identyfikator talizmanu!");
            sender.sendMessage("§7Dostępne: odpornosc_1, odpornosc_2, odpornosc_3, sila_1, sila_2, sila_3, slodkosci_3, drop, swiateczny_czas");
            return true;
        }

        var pozostale = odbiorca.getInventory().addItem(talizman);

        for (ItemStack item : pozostale.values()) {
            odbiorca.getWorld().dropItemNaturally(
                    odbiorca.getLocation(),
                    item
            );
        }

        sender.sendMessage("§aNadano talizman graczowi §e" + odbiorca.getName() + "§a.");
        odbiorca.sendMessage("§aOtrzymałeś talizman!");

        return true;
    }

    // ==========================================
    // KOMENDY
    // ==========================================

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {

        if (command.getName().equalsIgnoreCase("dajtalizman")) {
            return obsluzDajTalizman(sender, args);
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Ta komenda jest dostepna tylko dla graczy!");
            return true;
        }

        if (command.getName().equalsIgnoreCase("ec")
                || command.getName().equalsIgnoreCase("enderchest")) {

            powiekszonyEnderChest.open(player);
            return true;
        }

        if (command.getName().equalsIgnoreCase("sb")) {
            cooldownScoreboard.toggle(player);
            return true;
        }

        if (command.getName().equalsIgnoreCase("talizmany")) {
            talizmanyGUI.openMain(player);
            return true;
        }

        if (command.getName().equalsIgnoreCase("ustawglowke")) {

            if (!player.hasPermission("pykmcitems.ustawglowke")) {
                player.sendMessage("§cNie masz uprawnien do tej komendy!");
                return true;
            }

            ItemStack heldItem = player.getInventory().getItemInMainHand();

            if (heldItem.getType() != Material.PLAYER_HEAD) {
                player.sendMessage("§cMusisz trzymac glowke gracza w glownej rece!");
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

        if (command.getName().equalsIgnoreCase("obsikanasniezka")) {

            if (!player.hasPermission("pykmcitems.obsikanasniezka")) {
                player.sendMessage("§cNie masz uprawnien do tej komendy!");
                return true;
            }

            player.getInventory().addItem(Itemy.obsikanaSniezka(this));
            player.sendMessage("§aOtrzymales §eObsikaną Śnieżkę§a!");
            return true;
        }

        if (command.getName().equalsIgnoreCase("lukwiedzmy")) {

            if (!player.hasPermission("pykmcitems.lukwiedzmy")) {
                player.sendMessage("§cNie masz uprawnien do tej komendy!");
                return true;
            }

            player.getInventory().addItem(Itemy.lukWiedzmy(this));
            player.sendMessage("§aOtrzymales §dŁuk Wiedźmy§a!");
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

        if (command.getName().equalsIgnoreCase("berlo")) {

            if (!player.hasPermission("pykmcitems.berlo")) {
                player.sendMessage("§cNie masz uprawnien do tej komendy!");
                return true;
            }

            player.getInventory().addItem(BerloDrakuli.createItem());
            player.sendMessage("§aOtrzymales §x§E§A§0§0§0§0Berło Drakuli§a!");
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
