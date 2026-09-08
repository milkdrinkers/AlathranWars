package io.github.alathra.alathranwars.conflict.war.side.spawn;

import io.github.alathra.alathranwars.conflict.war.side.Side;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

/**
 * Represents a spawn location.
 */
public interface Spawn {
    /**
     * Gets the name of the spawn.
     *
     * @return the name
     */
    String getName();

    /**
     * Gets the location players respawn at.
     *
     * @return the location
     */
    Location getLocation();

    /**
     * Gets what kind of spawn this is.
     *
     * @return the type
     */
    SpawnType getType();

    /**
     * Gets the side this spawn belongs to.
     *
     * @return the side, or null if the spawn is not yet attached to one
     */
    @Nullable
    Side getSide();

    /**
     * Check if this spawn is proxied, also checks if some time has passed since it was last proxied
     *
     * @return boolean
     */
    boolean isProxied();

    /**
     * Returns the last time this spawn was proxied
     *
     * @return instant
     */
    Instant getLastProxied();

    /**
     * Check whether a location falls inside this spawn's bounding box
     *
     * @param location the location
     * @return boolean
     */
    boolean withinBoundingBox(Location location);
}
