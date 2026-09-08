package io.github.alathra.alathranwars.conflict.battle.siege;

import com.palmergames.bukkit.towny.object.Town;
import io.github.alathra.alathranwars.conflict.battle.Battle;
import io.github.alathra.alathranwars.conflict.war.side.Side;
import io.github.alathra.alathranwars.enums.battle.BattleSide;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Set;

/**
 * A siege of a town, fought between the two sides of a war.
 */
public interface Siege extends Battle {
    /**
     * Gets the name of the siege.
     *
     * @return the name
     */
    @NotNull
    String getName();

    /**
     * Gets the besieged town.
     *
     * @return the town
     */
    @NotNull
    Town getTown();

    /**
     * Gets the player who started the siege.
     *
     * @return the siege leader
     */
    @NotNull
    OfflinePlayer getSiegeLeader();

    /**
     * Gets the time at which the siege ends.
     *
     * @return the end time
     */
    @NotNull
    Instant getEndTime();

    /**
     * Gets how far the attackers have progressed towards capturing the control point.
     *
     * @return a value between 0.0 and 1.0
     */
    float getSiegeProgressPercentage();

    /**
     * Gets the control point being fought over.
     *
     * @return the control point, or null if the town has none
     */
    @Nullable
    Location getControlPoint();

    /**
     * Sets the control point being fought over.
     *
     * @param controlPoint the control point
     */
    void setControlPoint(@Nullable Location controlPoint);

    /**
     * Gets the attacking side.
     *
     * @return the attacker side
     */
    Side getAttackerSide();

    /**
     * Gets the defending side.
     *
     * @return the defender side
     */
    Side getDefenderSide();

    /**
     * Checks if the player is considered part of this battle
     *
     * @param p the player
     * @return yes if they are in the associated war
     * @apiNote This does not mean the player is inside the battle zone, only that they are in the associated war
     */
    boolean isInBattle(@Nullable Player p);

    /**
     * Check if a player is on the defending side
     *
     * @param p player
     * @return boolean
     */
    boolean isDefender(Player p);

    /**
     * Get which team the player belongs to in the battle
     *
     * @param p player
     * @return the team, or spectator
     */
    BattleSide getPlayerBattleSide(@Nullable Player p);

    /**
     * Get all online players inside the battle zone.
     *
     * @param side battle side
     * @return set of players
     */
    Set<Player> getPlayersInZone(BattleSide side);

    /**
     * Show the boss bar of a battle side to a player.
     *
     * @param side   battle side
     * @param player the player
     */
    void addBossBar(@NotNull BattleSide side, @NotNull Player player);

    /**
     * Hide the boss bar of a battle side from a player.
     *
     * @param side   battle side
     * @param player the player
     */
    void removeBossBar(@NotNull BattleSide side, @NotNull Player player);
}
