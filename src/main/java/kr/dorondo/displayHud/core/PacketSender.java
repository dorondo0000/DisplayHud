package kr.dorondo.displayHud.core;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.player.PlayerManager;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.joml.Vector3f;


public final class PacketSender {

    private PacketSender() {
    }

    private static PlayerManager playerManager() {
        return PacketEvents.getAPI().getPlayerManager();
    }

    public static void spawn(Player player, int entityId, UUID uuid, EntityType entityType, Location location) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(entityType, "entityType");
        Objects.requireNonNull(location, "location");

        Vector3d position = new Vector3d(location.getX(), location.getY(), location.getZ());
        float pitch = location.getPitch();
        float yaw = location.getYaw();
        WrapperPlayServerSpawnEntity packet = new WrapperPlayServerSpawnEntity(
                entityId,
                Optional.of(uuid),
                entityType,
                position,
                pitch,
                yaw,
                yaw,
                0,
                Optional.empty()
        );
        playerManager().sendPacket(player, packet);
    }

    public static void remove(Player player, int entityId) {
        Objects.requireNonNull(player, "player");

        playerManager().sendPacket(player, new WrapperPlayServerDestroyEntities(entityId));
    }

    public static void mount(Player player, int vehicleEntityId, int... passengerEntityIds) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(passengerEntityIds, "passengerEntityIds");

        playerManager().sendPacket(player, new WrapperPlayServerSetPassengers(vehicleEntityId, passengerEntityIds));
    }

    public static void update(Player player, int entityId, List<EntityData<?>> metadata) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(metadata, "metadata");

        playerManager().sendPacket(player, new WrapperPlayServerEntityMetadata(entityId, metadata));
    }
}
