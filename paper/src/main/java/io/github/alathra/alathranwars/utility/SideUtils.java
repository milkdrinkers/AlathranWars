package io.github.alathra.alathranwars.utility;

import io.github.alathra.alathranwars.conflict.war.War;
import io.github.alathra.alathranwars.conflict.war.WarController;
import io.github.alathra.alathranwars.conflict.war.side.Side;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

/**
 * Contains utility methods related to the side system
 */
public final class SideUtils {
    /**
     * Get a list of players at a coordinate that are associated with the specified side
     *
     * @param location location
     * @param dist     range
     * @param side     side
     * @return list of players near location
     */
    public static List<Player> getPlayersFromSideAtCoord(Location location, double dist, Side side) {
        return side.getPlayersOnline().stream()
            .filter(p -> location.getWorld().equals(p.getWorld()))
            .filter(p -> location.distanceSquared(p.getLocation()) < Math.pow(dist, 2))
            .toList();
    }

    /**
     * Checks if two sides are opposing each other in the same war
     *
     * @param side1 side
     * @param side2 side
     * @return true if in same war
     */
    public static boolean isOpponent(Side side1, Side side2) {
        return WarController.isAtWar(side1, side2);
    }

    /**
     * Get the opposing side
     *
     * @param side side
     * @return The opposing side in the war
     * @throws IllegalStateException If war or opposing side is null
     */
    public static Side getOpponent(Side side) throws IllegalStateException {
        War war = side.getWar();
        if (war == null)
            throw new IllegalStateException("War for side is null!");

        if (side.equals(war.getSide1()))
            return war.getSide2();

        if (side.equals(war.getSide2()))
            return war.getSide1();

        throw new IllegalStateException("Opposing side could not be found!");
    }

    /**
     * Get all sides this player is on
     *
     * @param p player
     * @return list of sides
     */
    public static List<Side> getPlayerSides(Player p) {
        return WarController.getInstance().getWars(p).stream()
            .map(war -> war.getPlayerSide(p))
            .filter(Objects::nonNull)
            .toList();
    }
}
