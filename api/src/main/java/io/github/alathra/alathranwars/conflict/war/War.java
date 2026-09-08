package io.github.alathra.alathranwars.conflict.war;

import com.palmergames.bukkit.towny.object.Government;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import io.github.alathra.alathranwars.conflict.battle.siege.Siege;
import io.github.alathra.alathranwars.conflict.war.side.Side;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * A war between two sides.
 */
public interface War {
    /**
     * Gets the unique id of the war.
     *
     * <p>Stable for the lifetime of the war, unlike {@link #getName()}. Hold this rather than a
     * name if you need a war to stay identifiable.
     *
     * <p>Declared here rather than by extending {@code IUnique<War>}: the implementation already
     * inherits {@code IUnique<Conflict>}, and a class cannot implement one generic interface at two
     * different type arguments.
     *
     * @return the uuid
     */
    @NotNull
    UUID getUUID();

    /**
     * Gets name of the war.
     *
     * @return the name
     */
    @NotNull
    String getName();

    /**
     * Gets label of the war.
     *
     * @return the label
     */
    @NotNull
    String getLabel();

    /**
     * Gets side 1.
     *
     * @return the side 1
     */
    @NotNull
    Side getSide1();

    /**
     * Gets side 2.
     *
     * @return the side 2
     */
    @NotNull
    Side getSide2();

    /**
     * Gets sides in a list.
     *
     * @return the sides
     */
    @NotNull
    Set<Side> getSides();

    /**
     * Gets attacker side.
     *
     * @return the attacker
     */
    @NotNull
    Side getAttacker();

    /**
     * Gets side by name.
     *
     * @param name the name
     * @return the side
     */
    @Nullable
    Side getSide(String name);

    /**
     * Gets nation or town side, or null if not in war.
     *
     * @param government the nation or town
     * @return the town side
     */
    @Nullable
    Side getSide(Government government);

    /**
     * Gets sieges in a list.
     *
     * @return the sieges
     */
    @NotNull
    Set<Siege> getSieges();

    /**
     * Add siege to war.
     *
     * @param siege the siege
     */
    @ApiStatus.Internal
    void addSiege(Siege siege);

    /**
     * Cancel all active sieges in the war. Ends the sieges in a draw.
     *
     * @param town the town
     */
    @ApiStatus.Internal
    void cancelSieges(Town town);

    /**
     * Gets nations in the war excluding surrendered ones.
     *
     * @return the nations
     */
    Set<Nation> getNations();

    /**
     * Gets all surrendered nations in the war.
     *
     * @return the surrendered nations
     */
    Set<Nation> getNationsSurrendered();

    /**
     * Gets all nations in the war including surrendered.
     *
     * @return the all nations
     */
    Set<Nation> getNationsAll();

    /**
     * Gets towns in the war excluding surrendered ones.
     *
     * @return the towns
     */
    Set<Town> getTowns();

    /**
     * Gets all surrendered towns in the war.
     *
     * @return the surrendered towns
     */
    Set<Town> getTownsSurrendered();

    /**
     * Gets all towns in the war including surrendered towns.
     *
     * @return the all towns
     */
    Set<Town> getTownsAll();

    /**
     * Get all players in the war (includes surrendered players)
     *
     * @return player list
     */
    List<OfflinePlayer> getPlayersAll();

    /**
     * Get all online players in the war (excluding surrendered)
     *
     * @return player list
     */
    List<Player> getPlayersOnline();

    /**
     * Get all online players in the war (excludes non-surrendered players)
     *
     * @return player list
     */
    List<Player> getPlayersSurrenderedOnline();

    /**
     * Get all online players in the war (includes surrendered players)
     *
     * @return player list
     */
    List<Player> getPlayersOnlineAll();

    /**
     * Check if player is in war.
     *
     * @param p the p
     * @return the boolean
     */
    boolean isInWar(Player p);

    /**
     * Is nation or town in war.
     *
     * @param government the nation or town
     * @return the boolean
     */
    boolean isInWar(Government government);

    /**
     * Gets the side of the player or null if they are not in the war.
     *
     * @param p the p
     * @return the player side
     */
    @Nullable
    Side getPlayerSide(Player p);

    /**
     * Gets player side by player UUID or null if they are not in the war.
     *
     * @param uuid the uuid
     * @return the player side
     */
    @Nullable
    Side getPlayerSide(UUID uuid);

    /**
     * Check if a side exists with the specified name.
     *
     * @param sideName the side name
     * @return the boolean
     */
    boolean isSideValid(String sideName);

    /**
     * Makes a government surrender (Recursively surrenders subjects AKA nations, towns, players)
     *
     * @param government the government
     */
    void surrender(Government government);

    /**
     * Makes a government un-surrender (Recursively un-surrenders subjects AKA nations, towns, players)
     *
     * @param government the government
     */
    void unsurrender(Government government);

    /**
     * End the war in a white peace/draw.
     */
    void draw();

    /**
     * Gets if this is an event war. Event wars skip or execute certain logic as they are for player events.
     *
     * @return the boolean
     */
    boolean isEventWar();

    /**
     * Whether the war is currently within its scheduled war time.
     *
     * @return the boolean
     */
    boolean isWarTime();

    /**
     * Sets the time at which war time begins.
     *
     * @param scheduledWarTime the scheduled war time, or null to clear it
     */
    void setScheduledWarTime(@Nullable Instant scheduledWarTime);
}
