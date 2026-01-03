package kr.dorondo.displayHud.core;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds player-bound Hud instances.
 *
 * Suggested structure:
 * Map<viewerUuid, Map<hudId, Hud>>
 */
public final class HudRegistry {

    private final Map<UUID, Map<HudId, Hud>> byViewer = new ConcurrentHashMap<>();

    public Optional<Hud> get(Player viewer, HudId hudId) {
        Objects.requireNonNull(viewer, "viewer");
        Objects.requireNonNull(hudId, "hudId");
        Map<HudId, Hud> map = byViewer.get(viewer.getUniqueId());
        if (map == null) return Optional.empty();
        return Optional.ofNullable(map.get(hudId));
    }

    public Hud put(Hud hud) {
        Objects.requireNonNull(hud, "hud");
        UUID uuid = hud.viewer().getUniqueId();
        return byViewer
                .computeIfAbsent(uuid, k -> new ConcurrentHashMap<>())
                .put(hud.hudId(), hud);
    }

    public Optional<Hud> remove(Player viewer, HudId hudId) {
        Objects.requireNonNull(viewer, "viewer");
        Objects.requireNonNull(hudId, "hudId");
        Map<HudId, Hud> map = byViewer.get(viewer.getUniqueId());
        if (map == null) return Optional.empty();
        return Optional.ofNullable(map.remove(hudId));
    }

    public void removeAll(Player viewer) {
        Objects.requireNonNull(viewer, "viewer");
        Map<HudId, Hud> map = byViewer.remove(viewer.getUniqueId());
        if (map == null) return;

        // TODO: destroy all huds safely
        // map.values().forEach(Hud::destroy);
        map.clear();
    }

    public void clear() {
        // TODO: destroy all huds safely
        byViewer.clear();
    }
}
