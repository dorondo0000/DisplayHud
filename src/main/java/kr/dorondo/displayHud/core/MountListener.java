package kr.dorondo.displayHud.core;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.UUID;

public final class MountListener implements PacketListener {

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.SET_PASSENGERS) return;

        WrapperPlayServerSetPassengers wrapper = new WrapperPlayServerSetPassengers(event);

        Player viewer = toBukkitPlayer(event);
        if (viewer == null) return;

        if (viewer.getEntityId() != wrapper.getEntityId()) return;

        int[] passengers = wrapper.getPassengers();
        int[] extras = getHudIds(viewer);
        if (extras.length == 0) return;

        wrapper.setPassengers(appendPassengers(passengers, extras));

        //Bukkit.getLogger().info(Arrays.toString(extras));
    }

    private static int[] getHudIds(Player player) {
        return DisplayHud.getHuds(player).values().stream()
                .map(DisplayHud::getNMSid)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .toArray();
    }

    private static Player toBukkitPlayer(PacketSendEvent event) {
        Object p = event.getPlayer();
        if (p instanceof Player bp) return bp;

        try {
            UUID uuid = event.getUser().getUUID();
            return Bukkit.getPlayer(uuid);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static int[] appendPassengers(int[] original, int... extras) {
        LinkedHashSet<Integer> set = new LinkedHashSet<>();
        for (int v : original) set.add(v);
        for (int e : extras) set.add(e);

        int[] res = new int[set.size()];
        int i = 0;
        for (int v : set) res[i++] = v;
        return res;
    }
}
