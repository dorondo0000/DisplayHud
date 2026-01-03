package kr.dorondo.displayHud.core;

/**
 * Simple HUD identifier.
 * record auto-generates equals/hashCode -> perfect for Map keys.
 */
public record HudId(String value) {
    public HudId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("HudId must not be blank");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
