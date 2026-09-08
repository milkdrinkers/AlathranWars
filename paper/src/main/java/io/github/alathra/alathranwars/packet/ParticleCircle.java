package io.github.alathra.alathranwars.packet;

import com.github.retrooper.packetevents.protocol.color.Color;
import com.github.retrooper.packetevents.protocol.particle.Particle;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleDustData;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleTypes;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerParticle;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * Class for sending a circle of particles to a collection of players using packets.
 *
 * @implNote Ensure PacketEvents hook is loaded before use.
 */
public abstract class ParticleCircle {
    /**
     * Spawn a circle of particles around a center point for a collection of players.
     *
     * @param player         player
     * @param center         the central point of the circle
     * @param radius         the radius
     * @param particlesCount the amount of particles to spawn around the circle
     */
    public static void sendCircle(Player player, org.bukkit.Location center, final int radius, final int particlesCount) {
        sendCircle(
            player,
            center,
            radius,
            particlesCount,
            new Color(255, 255, 255)
        );
    }

    /**
     * Spawn a circle of particles around a center point for a collection of players.
     *
     * @param player         player
     * @param center         the central point of the circle
     * @param radius         the radius
     * @param particlesCount the amount of particles to spawn around the circle
     * @param color          the color of the particles
     */
    public static void sendCircle(Player player, org.bukkit.Location center, final int radius, final int particlesCount, Color color) {
        // The particle object
        final Particle<ParticleDustData> particleObject = new Particle<>(
            ParticleTypes.DUST,
            new ParticleDustData(1.0F, color)
        );

        sendCircle(player, center, radius, particlesCount, particleObject);
    }

    /**
     * Spawn a circle of particles around a center point for a collection of players.
     *
     * @param player         player
     * @param center         the central point of the circle
     * @param radius         the radius
     * @param particlesCount the amount of particles to spawn around the circle
     * @param particleObject the particle to send
     */
    public static void sendCircle(Player player, final org.bukkit.Location center, final int radius, final int particlesCount, Particle<?> particleObject) {
        // Iterate over each particle
        for (int particleCount = 0; particleCount < particlesCount; particleCount++) {
            final org.bukkit.Location loc = center.clone();
            loc.setX(center.getX() + Math.cos(particleCount) * radius);
            loc.setZ(center.getZ() + Math.sin(particleCount) * radius);

            final Location location = SpigotConversionUtil.fromBukkitLocation(loc);
            final WrapperPlayServerParticle packet = new WrapperPlayServerParticle(
                particleObject,
                false,
                location.getPosition(),
                new Vector3f(0, 0, 0),
                0.25F,
                1
            );

            // Send the packets here since we are already iterating over all packets
            PacketUtils.sendPacket(player, packet);
        }
    }

    /**
     * Spawn a circle of particles around a center point for a collection of players.
     *
     * @param players        players
     * @param center         the central point of the circle
     * @param radius         the radius
     * @param particlesCount the amount of particles to spawn around the circle
     */
    public static void sendCircle(Collection<Player> players, org.bukkit.Location center, final int radius, final int particlesCount) {
        sendCircle(
            players,
            center,
            radius,
            particlesCount,
            new Color(255, 255, 255)
        );
    }

    /**
     * Spawn a circle of particles around a center point for a collection of players.
     *
     * @param players        players
     * @param center         the central point of the circle
     * @param radius         the radius
     * @param particlesCount the amount of particles to spawn around the circle
     * @param color          the color of the particles
     */
    public static void sendCircle(Collection<Player> players, org.bukkit.Location center, final int radius, final int particlesCount, Color color) {
        // The particle object
        final Particle<ParticleDustData> particleObject = new Particle<>(
            ParticleTypes.DUST,
            new ParticleDustData(1.0F, color)
        );

        sendCircle(players, center, radius, particlesCount, particleObject);
    }

    /**
     * Spawn a circle of particles around a center point for a collection of players.
     *
     * @param players        players
     * @param center         the central point of the circle
     * @param radius         the radius
     * @param particlesCount the amount of particles to spawn around the circle
     * @param particleObject the particle to send
     */
    public static void sendCircle(Collection<Player> players, final org.bukkit.Location center, final int radius, final int particlesCount, Particle<?> particleObject) {
        // Iterate over each particle
        for (int particleCount = 0; particleCount < particlesCount; particleCount++) {
            final org.bukkit.Location loc = center.clone();
            loc.setX(center.getX() + Math.cos(particleCount) * radius);
            loc.setZ(center.getZ() + Math.sin(particleCount) * radius);

            final Location location = SpigotConversionUtil.fromBukkitLocation(loc);
            final WrapperPlayServerParticle packet = new WrapperPlayServerParticle(
                particleObject,
                false,
                location.getPosition(),
                new Vector3f(0, 0, 0),
                0.25F,
                1
            );

            // Send the packets here since we are already iterating over all packets
            PacketUtils.sendPacket(players, packet);
        }
    }
}
