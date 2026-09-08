package io.github.alathra.alathranwars.conflict.war.side;

import com.palmergames.bukkit.towny.object.Government;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import io.github.alathra.alathranwars.conflict.IAssociatedWar;
import io.github.alathra.alathranwars.conflict.war.side.spawn.Spawn;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * One of the two sides participating in a {@link io.github.alathra.alathranwars.conflict.war.War}.
 */
public interface Side extends IAssociatedWar {
    /**
     * Gets the unique id of the side.
     *
     * <p>Stable for the lifetime of the side, unlike {@link #getName()}, which is a town or nation
     * name and can be renamed out from under you.
     *
     * @return the uuid
     */
    @NotNull
    UUID getUUID();

    /**
     * Gets the name of the side. (A town or nation name)
     *
     * @return the name
     */
    String getName();

    /**
     * Equals boolean.
     *
     * @param side the side
     * @return the boolean
     */
    boolean equals(Side side);

    // SECTION Governments management

    /**
     * Add a government to the side (Recursively adds subjects AKA towns, players)
     *
     * @param government a government
     */
    void add(Government government);

    /**
     * Remove a government from the side (Recursively removes subjects AKA towns, players)
     *
     * @param government a government
     */
    void remove(Government government);

    /**
     * Kick a government from the side (Recursively kicks subjects AKA towns, players)
     *
     * @param government a government
     */
    void kick(Government government);

    // SECTION Players management

    /**
     * Add a player to the side
     *
     * @param p a player
     */
    void add(Player p);

    /**
     * Remove a player from the side
     *
     * @param p a player
     */
    void remove(Player p);

    /**
     * Kick a player from the side
     *
     * @param uuid a player uuid
     */
    void kick(UUID uuid);

    /**
     * Add a player to the online set
     *
     * @param p a player
     * @apiNote this is only used internally to manage which players are online or not
     * @hidden
     */
    @ApiStatus.Internal
    void login(Player p);

    /**
     * Remove a player from the online set
     *
     * @param p a player
     * @apiNote this is only used internally to manage which players are online or not
     * @hidden
     */
    @ApiStatus.Internal
    void logout(Player p);

    // SECTION Governments Getters

    /**
     * Get nations on this side (excludes surrendered nations)
     *
     * @return the nations
     */
    Set<Nation> getNations();

    /**
     * Get all nations on this side (includes surrendered nations)
     *
     * @return the nations
     */
    List<Nation> getNationsAll();

    /**
     * Get towns on this side (excludes surrendered towns)
     *
     * @return the towns
     */
    Set<Town> getTowns();

    /**
     * Get towns on this side (excludes non-surrendered towns)
     *
     * @return the towns
     */
    Set<Town> getTownsSurrendered();

    /**
     * Get all towns on this side (includes surrendered towns)
     *
     * @return the towns
     */
    Set<Town> getTownsAll();

    // SECTION Players Getters

    /**
     * Get all players on this side (excluding surrendered)
     *
     * @return player list
     */
    Set<OfflinePlayer> getPlayers();

    /**
     * Get all players on this side (includes surrendered players)
     *
     * @return player list
     */
    Set<OfflinePlayer> getPlayersAll();

    /**
     * Get all online players on this side (excluding surrendered)
     *
     * @return player list
     */
    Set<Player> getPlayersOnline();

    // SECTION Checks

    /**
     * Check if a government is on this side (includes surrendered governments)
     *
     * @param government the government
     * @return true if on side
     */
    boolean isOnSide(Government government);

    /**
     * Check if a player is on this side (includes surrendered players)
     *
     * @param p the player
     * @return true if on side
     */
    boolean isOnSide(Player p);

    /**
     * Check if a government is surrendered on this side
     *
     * @param government the government
     * @return true if on side and surrendered
     */
    boolean isSurrendered(Government government);

    /**
     * Check if a player is surrendered on this side
     *
     * @param p the player
     * @return true if on side and surrendered
     */
    boolean isSurrendered(Player p);

    /**
     * Check if this side is the attacking side of its war
     *
     * @return the boolean
     */
    boolean isAttacker();

    /**
     * Check if this side is the defending side of its war
     *
     * @return the boolean
     */
    boolean isDefender();

    // SECTION Surrendering

    /**
     * Surrenders the war for this side if it is no longer able to continue fighting
     */
    void processSurrenders();

    // SECTION Graces

    /**
     * Whether this side is currently protected from being besieged
     *
     * @return the boolean
     */
    boolean isSiegeGraceActive();

    /**
     * The remaining time before this side can be besieged again
     *
     * @return the remaining cooldown
     */
    Duration getSiegeGraceCooldown();

    /**
     * Starts a new siege grace period for this side
     */
    void setSiegeGrace();

    // SECTION Score

    /**
     * Get the war score of this side
     *
     * @return the score
     */
    int getScore();

    /**
     * Add to the war score of this side
     *
     * @param add the amount to add
     */
    void addScore(int add);

    // SECTION Spawning

    /**
     * Get all spawns this side may currently respawn at. Includes spawns captured from the
     * opposing side and excludes this side's own surrendered towns.
     *
     * @return set of spawns
     */
    Set<Spawn> getSpawns();

    /**
     * Get every spawn tracked for this side, unfiltered. (Its towns, outposts and rally points)
     *
     * @return set of spawns
     * @implNote this retrieves a cached list
     */
    Set<Spawn> getAllSpawns();

    /**
     * Add a spawn to this side
     *
     * @param spawn the spawn
     */
    void addSpawn(@NotNull Spawn spawn);
}
