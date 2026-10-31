package pl.twojanazwa.plugin;

import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getLogger().info("MinecraftPlugin zostal wlaczony!");
    }

    @Override
    public void onDisable() {
        getLogger().info("MinecraftPlugin zostal wylaczony!");
    }
}
