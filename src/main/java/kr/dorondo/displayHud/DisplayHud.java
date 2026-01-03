package kr.dorondo.displayHud;

import org.bukkit.plugin.java.JavaPlugin;

public final class DisplayHud extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("앙기무리");

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
