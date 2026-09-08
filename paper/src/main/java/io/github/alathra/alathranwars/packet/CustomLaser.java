package io.github.alathra.alathranwars.packet;

import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import me.tofaa.entitylib.meta.other.EndCrystalMeta;
import me.tofaa.entitylib.wrapper.WrapperEntity;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

/**
 * Renders an end crystal and its laser between two points using packets.
 * Players need to be added and removed viewers to start receiving packets regarding the laser.
 */
public class CustomLaser {
    private org.bukkit.Location from;
    private org.bukkit.Location to;
    private @Nullable WrapperEntity entity;

    /**
     * Instantiates a new CustomLaser object.
     *
     * @param from The location of the end crystal and the origin of the laser
     * @param to   The end location of the laser
     * @throws IllegalStateException Thrown if from and to locations are not in the same world
     * @implNote Use {@link #spawn()} to create the crystal on clients and {@link #despawn()} to remove it
     */
    public static CustomLaser of(org.bukkit.Location from, org.bukkit.Location to) throws IllegalStateException {
        return new CustomLaser(from, to);
    }

    /**
     * Instantiates a new CustomLaser object.
     *
     * @param from The location of the end crystal and the origin of the laser
     * @param to   The end location of the laser
     * @throws IllegalStateException Thrown if from and to locations are not in the same world
     * @implNote Use {@link #spawn()} to create the crystal on clients and {@link #despawn()} to remove it
     */
    private CustomLaser(org.bukkit.Location from, org.bukkit.Location to) throws IllegalStateException {
        this.from = from;
        this.to = to;

        if (!from.getWorld().equals(to.getWorld()))
            throw new IllegalStateException("From and to locations need to be in the same world!");
    }

    /**
     * Spawns the entity for all viewers
     */
    public void spawn() {
        final UUID uuid = UUID.randomUUID();
        final int entityId = SpigotReflectionUtil.generateEntityId();

        entity = new WrapperEntity(
            entityId,
            uuid,
            EntityTypes.END_CRYSTAL
        );

        entity.spawn(SpigotConversionUtil.fromBukkitLocation(from));

        entity.consumeEntityMeta(EndCrystalMeta.class, meta -> {
            meta.setHasNoGravity(true);
            meta.setInvisible(true);
            meta.setShowingBottom(false);
            meta.setBeamTarget(SpigotConversionUtil.fromBukkitLocation(to).getPosition().toVector3i());
        });
    }

    /**
     * De-spawns the entity for all viewers
     */
    public void despawn() {
        if (entity == null)
            return;

        entity.despawn();
        entity = null;
    }

    /**
     * Updates the entity location for all viewers
     *
     * @param from The location of the end crystal and the origin of the laser
     * @param to   The end location of the laser
     * @throws IllegalStateException Thrown if from and to locations are not in the same world
     */
    public void move(org.bukkit.Location from, org.bukkit.Location to) throws IllegalStateException {
        this.from = from;
        this.to = to;

        if (!from.getWorld().equals(to.getWorld()))
            throw new IllegalStateException("From and to locations need to be in the same world!");

        if (entity == null)
            return;

        Set<UUID> previousViewers = entity.getViewers();
        despawn();
        spawn();
        previousViewers.forEach(viewer -> entity.addViewer(viewer));
    }

    /**
     * Add a viewer
     *
     * @param p player
     * @implNote The entity is spawned with meta applied for new viewers
     */
    public void addViewer(Player p) {
        if (entity == null)
            return;

        entity.addViewer(p.getUniqueId());
    }

    /**
     * Remove a viewer
     *
     * @param p player
     */
    public void removeViewer(Player p) {
        if (entity == null)
            return;

        entity.removeViewer(p.getUniqueId());
    }

    public static double LASER_LENGHT = 350D;
    public static double LASER_HEIGHT_ABOVE_GROUND = 2.5D;

    public static Location getLaserFromLocation(Location location) {
        return location.clone().add(new Vector(0, LASER_HEIGHT_ABOVE_GROUND, 0));
    }

    public static Location getLaserToLocation(Location location) {
        return location.clone().add(new Vector(0, LASER_LENGHT, 0));
    }
}
