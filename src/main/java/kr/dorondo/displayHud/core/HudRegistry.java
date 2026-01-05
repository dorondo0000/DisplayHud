package kr.dorondo.displayHud.core;

import org.bukkit.entity.Player;

import java.util.*;

public final class HudRegistry {

    private static final HudRegistry INSTANCE = new HudRegistry();

    private final WeakHashMap<Player, Map<String, Hud>> HudMap = new WeakHashMap<>();

    private HudRegistry() {
    }

    public static HudRegistry getInstance() {
        return INSTANCE;
    }

    public Hud addHud(Player player, String id, HudDefinition definition) {
        return addHud(player, id, UUID.randomUUID(), definition, false);
    }

    public Hud addHud(Player player, String id, HudDefinition definition, boolean override) {
        return addHud(player, id, UUID.randomUUID(), definition, override);
    }

    public Hud addHud(Player player, String id, UUID uuid, HudDefinition definition) {
        return addHud(player, id, UUID.randomUUID(), definition, false);
    }

    public Hud addHud(Player player, String id, UUID uuid, HudDefinition definition, boolean override) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(uuid, "uuid");
        Objects.requireNonNull(definition, "definition");
        if (!override) {
            return HudMap.computeIfAbsent(player, v -> new HashMap<>()).computeIfAbsent(id, k -> new Hud(player, id, uuid, definition));
        }
        Hud old = HudMap.get(player).remove(id);
        if (old != null) {
            removeHud(old);
        }
        Hud hud = new Hud(player, id, uuid, definition);
        HudMap.computeIfAbsent(player, v -> new HashMap<>()).put(id, hud);
        return hud;
    }

    public Hud getHud(Player player, String id) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(id, "id");

        Map<String, Hud> huds = HudMap.get(player);
        if (huds == null) {
            return null;
        }
        return huds.get(id);
    }

    public Map<String, Hud> getHuds(Player player) {
        Objects.requireNonNull(player, "player");

        Map<String, Hud> huds = HudMap.get(player);
        if (huds == null || huds.isEmpty()) {
            return Collections.emptyMap();
        }
        return new HashMap<>(huds);
    }

    public void removeHud(Player player, String id) {
        removeHud(getHud(player,id));
    }

    public void removeHud(Hud hud) {
        Objects.requireNonNull(hud, "hud");
        Player player = hud.getPlayer();
        String id = hud.getId();
        Map<String, Hud> huds = HudMap.get(player);
        if (huds == null) {
            return;
        }
        huds.remove(id);
        hud.dispose();
        if (huds.isEmpty()) {
            HudMap.remove(player);
        }
    }

    public void clearHuds(Player player) {
        Objects.requireNonNull(player, "player");
        Map<String, Hud> huds = HudMap.remove(player);
        huds.values().forEach(Hud::dispose);
    }

}
