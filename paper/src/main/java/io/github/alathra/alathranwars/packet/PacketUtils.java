package io.github.alathra.alathranwars.packet;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * Utility methods when dealing with packets.
 */
public abstract class PacketUtils {
    public static void sendPackets(Collection<Player> players, Collection<? extends PacketWrapper<?>> packets) {
        for (Player player : players) {
            sendPackets(player, packets);
        }
    }

    public static void sendPackets(Player player, Collection<? extends PacketWrapper<?>> packets) {
        for (PacketWrapper<?> packet : packets) {
            sendPacket(player, packet);
        }
    }

    public static void sendPacket(Collection<Player> players, PacketWrapper<?> packet) {
        for (Player player : players) {
            sendPacket(player, packet);
        }
    }

    public static void sendPacket(Player player, PacketWrapper<?> packet) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }
}
