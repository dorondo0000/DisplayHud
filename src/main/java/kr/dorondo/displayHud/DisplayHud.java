package kr.dorondo.displayHud;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class DisplayHud extends JavaPlugin {

    private static DisplayHud INSTANCE;


    @Override
    public void onEnable() {
        INSTANCE = this;
        getLogger().info("앙기무리");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static DisplayHud getInstance() {
        return INSTANCE;
    }
}
