package kr.dorondo.displayHud.core;

import java.util.Objects;

/**
 * Player-agnostic HUD blueprint (template/definition).
 * Recommended to keep immutable.
 */
public final class HudDefinition {

    private final HudId hudId;

    public HudDefinition(HudId hudId) {
        this.hudId = Objects.requireNonNull(hudId, "hudId");
    }

    public HudId hudId() {
        return hudId;
    }

    /**
     * If you later want to store structure/element definitions, put them here
     * as final fields (immutable), or expose read-only views.
     */
}
