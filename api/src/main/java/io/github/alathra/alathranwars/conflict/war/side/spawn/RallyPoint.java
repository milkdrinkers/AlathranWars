package io.github.alathra.alathranwars.conflict.war.side.spawn;

import org.bukkit.OfflinePlayer;

/**
 * A player placed spawn, created by placing a rally banner.
 */
public interface RallyPoint extends Spawn {
    /**
     * Gets the player who placed the rally point.
     *
     * @return the creator
     */
    OfflinePlayer getCreator();
}
