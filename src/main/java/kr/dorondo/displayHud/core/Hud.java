package kr.dorondo.displayHud.core;

import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * Player-bound HUD instance (runtime state).
 * Created from HudDefinition and independent from it after creation.
 */
public final class Hud {

    private final Player viewer;
    private final HudId hudId;
    private final HudDefinition definition;

    // visibility/runtime state (placeholder)
    private boolean visible;

    public Hud(Player viewer, HudId hudId, HudDefinition definition) {
        this.viewer = Objects.requireNonNull(viewer, "viewer");
        this.hudId = Objects.requireNonNull(hudId, "hudId");
        this.definition = Objects.requireNonNull(definition, "definition");
    }

    public Player viewer() {
        return viewer;
    }

    public HudId hudId() {
        return hudId;
    }

    /**
     * The definition used at creation time.
     * Hud must stay independent from this after instantiation.
     * (Keep reference only for debugging/inspection, or remove if you prefer.)
     */
    public HudDefinition definition() {
        return definition;
    }

    public boolean isVisible() {
        return visible;
    }

    public void show() {
        // TODO: spawn virtual entities + mount + send packets
        this.visible = true;
    }

    public void hide() {
        // TODO: despawn packets for this viewer
        this.visible = false;
    }

    public void resync() {
        // TODO: full despawn + respawn + remount + metadata
    }

    public void destroy() {
        // TODO: cleanup, ensure hide(), release ids, etc.
        hide();
    }
}
