package io.github.alathra.alathranwars.api;

import io.github.alathra.alathranwars.conflict.battle.siege.Siege;
import io.github.alathra.alathranwars.conflict.war.War;
import io.github.alathra.alathranwars.conflict.war.side.Side;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * The entry point of the AlathranWars API.
 *
 * <p>Obtain the singleton with {@link #getInstance()} once the plugin is enabled. The accessors
 * here are deliberately few, they are the starting points for reaching the model types.
 * Everything else is reached through {@link War} and {@link Side}.
 *
 * @see #getInstance()
 * @since 4.0.0
 */
public abstract class AlathranWarsAPI {
    private static AlathranWarsAPI INSTANCE = null;

    /**
     * Gets the instance of the AlathranWarsAPI.
     *
     * @return the instance of AlathranWarsAPI
     * @throws RuntimeException if accessed before the plugin has initialized it
     * @since 4.0.0
     */
    public static AlathranWarsAPI getInstance() {
        if (!isLoaded())
            throw new RuntimeException("AlathranWars API was accessed before being initialized!");
        return INSTANCE;
    }

    /**
     * Sets the instance of the AlathranWarsAPI.
     * This method is intended for internal use by the api provider only.
     *
     * @param api the instance of AlathranWarsAPI to set
     * @since 4.0.0
     */
    @ApiStatus.Internal
    protected static void setInstance(AlathranWarsAPI api) {
        INSTANCE = api;
    }

    /**
     * Checks if the AlathranWarsAPI is available
     *
     * @return true if API is loaded and available
     * @since 4.0.0
     */
    public static boolean isLoaded() {
        return INSTANCE != null;
    }

    /**
     * Gets every war currently loaded.
     *
     * @return an unmodifiable view of the wars
     * @since 4.0.0
     */
    public abstract @NotNull Collection<War> getWars();

    /**
     * Gets a war by its name, as returned by {@link War#getName()}.
     *
     * @param name the war name
     * @return the war, or null if no war has that name
     * @since 4.0.0
     */
    public abstract @Nullable War getWar(@NotNull String name);

    /**
     * Gets the side a player belongs to in the given war.
     *
     * <p>A surrendered player still has a side.
     *
     * @param player the player
     * @param war    the war
     * @return the side, or null if the player is not in that war
     * @since 4.0.0
     */
    public abstract @Nullable Side getPlayerSide(@NotNull OfflinePlayer player, @NotNull War war);

    /**
     * Checks whether a player is in any war, surrendered or not.
     *
     * @param player the player
     * @return true if the player is in at least one war
     * @since 4.0.0
     */
    public abstract boolean isInWar(@NotNull OfflinePlayer player);

    /**
     * Gets the radius of a siege battlefield, in blocks.
     *
     * <p>Exposed here rather than as a constant on {@link Siege} because the value is read from the
     * server's config, and this module cannot reach the config layer.
     *
     * @return the battlefield radius in blocks
     * @since 4.0.0
     */
    public abstract int getSiegeBattlefieldRange();

    /**
     * Gets the closest a player may be to a town to start a siege on it, in blocks.
     *
     * @return the minimum start range in blocks
     * @since 4.0.0
     */
    public abstract int getSiegeStartMinRange();

    /**
     * Gets the furthest a player may be from a town to start a siege on it, in blocks.
     *
     * @return the maximum start range in blocks
     * @since 4.0.0
     */
    public abstract int getSiegeStartMaxRange();
}
