package io.github.alathra.alathranwars.api;

import io.github.alathra.alathranwars.conflict.war.War;
import io.github.alathra.alathranwars.conflict.war.WarController;
import org.bukkit.entity.Player;

public interface WarAPI {
    /**
     * Checks if the player is participating in any wars with active war times
     *
     * @param player player
     * @return true if there are any active war times the player is in, otherwise false
     */
    default boolean hasActiveWarTime(Player player) {
        return WarController.getInstance().getWars(player).stream()
            .anyMatch(War::isWarTime);
    }
}
