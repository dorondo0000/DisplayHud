package kr.dorondo.displayHud.core;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class DisplayHudManager {

    private DisplayHudManager() {}

    public static float unitX;
    public static float unitY;
    public static float screenX;
    public static float screenY;
    public static Integer alignmentGap;
    public static boolean BukkitEventListener;

    public static void load(JavaPlugin plugin) {
        plugin.saveDefaultConfig();
        FileConfiguration config = plugin.getConfig();

        unitX = (float) config.getDouble("settings.unit-x", 100);
        unitY = (float) config.getDouble("settings.unit-y", 100);
        screenX = (float) config.getDouble("settings.screen-x", 1920);
        screenY = (float) config.getDouble("settings.screen-y", 1080);
        alignmentGap = config.getInt("settings.alignment-gap", 1000000);

        BukkitEventListener = config.getBoolean("customize.bukkit-event-listener", true);

    }
}
