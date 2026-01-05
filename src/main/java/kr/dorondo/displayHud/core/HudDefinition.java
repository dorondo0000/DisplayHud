package kr.dorondo.displayHud.core;

import org.bukkit.entity.TextDisplay;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public record HudDefinition(HudElement element, Vector3f location, Vector3f scale, Vector3f leftRotation, Vector3f rightRotation,
                            HudAlignment hudAlignment, TextDisplay.TextAlignment textAlignment) {
}
