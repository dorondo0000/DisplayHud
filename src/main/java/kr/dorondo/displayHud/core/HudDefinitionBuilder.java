package kr.dorondo.displayHud.core;

import org.bukkit.Material;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class HudDefinitionBuilder {

    private HudElement element = new HudElement(new ItemStack(Material.WHITE_CONCRETE));;
    private Vector3f location = new Vector3f(0f,0f,0f);
    private Vector3f scale = new Vector3f(1f, 1f, 1f);
    private Vector3f leftRotation = new Vector3f();
    private Vector3f rightRotation = new Vector3f();
    private HudAlignment hudAlignment = HudAlignment.CENTER;
    private TextDisplay.TextAlignment textAlignment = TextDisplay.TextAlignment.CENTER;

    public HudDefinitionBuilder element(HudElement element) {
        this.element = element;
        return this;
    }

    public HudDefinitionBuilder location(Vector3f location) {
        this.location = location;
        return this;
    }

    public HudDefinitionBuilder scale(Vector3f scale) {
        this.scale = scale;
        return this;
    }

    public HudDefinitionBuilder leftRotation(Vector3f leftRotation) {
        this.leftRotation = leftRotation;
        return this;
    }

    public HudDefinitionBuilder rightRotation(Vector3f rightRotation) {
        this.rightRotation = rightRotation;
        return this;
    }

    public HudDefinitionBuilder hudAlignment(HudAlignment hudAlignment) {
        this.hudAlignment = hudAlignment;
        return this;
    }

    public HudDefinitionBuilder textAlignment(TextDisplay.TextAlignment textAlignment) {
        this.textAlignment = textAlignment;
        return this;
    }

    public HudDefinition build() {
        return new HudDefinition(
                element,
                location,
                scale,
                leftRotation,
                rightRotation,
                hudAlignment,
                textAlignment
        );
    }
}
