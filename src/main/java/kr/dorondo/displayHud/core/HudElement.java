package kr.dorondo.displayHud.core;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

public final class HudElement {

    private final Component component;
    private final ItemStack itemStack;

    public HudElement(Component component) {
        this.component = component;
        this.itemStack = null;
    }

    public HudElement(ItemStack itemStack) {
        this.component = null;
        this.itemStack = itemStack;
    }

    public boolean isComponent() {
        return component != null;
    }

    public boolean isItemStack() {
        return itemStack != null;
    }

    public Component getComponent() {
        return component;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }


}
