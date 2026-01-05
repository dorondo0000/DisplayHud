package kr.dorondo.displayHud;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class DisplayHud extends JavaPlugin {

    private static DisplayHud INSTANCE;

    private float unitX;
    private float unitY;
    private float screenX;
    private float screenY;
    private Integer alignmentGap;


    @Override
    public void onEnable() {
        INSTANCE = this;
        getLogger().info("앙기무리");
        loadSettings();

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static DisplayHud getInstance() {
        return INSTANCE;
    }

    public void loadSettings() {
        this.unitX = 100;
        this.unitY = 100;
        this.screenX = 1920;
        this.screenY = 1080;
        this.alignmentGap = 1000000;
        //config.yml로 교체
    }

    public float getUnitX() { return unitX; }
    public float getUnitY() { return unitY; }
    public float getScreenX() { return screenX; }
    public float getScreenY() { return screenY; }
    public Integer getAlignmentGap() { return alignmentGap; }
}
