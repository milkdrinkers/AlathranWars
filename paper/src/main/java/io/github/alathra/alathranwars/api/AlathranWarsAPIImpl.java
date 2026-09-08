package io.github.alathra.alathranwars.api;

import io.github.alathra.alathranwars.conflict.battle.siege.SiegeImpl;
import io.github.alathra.alathranwars.conflict.war.War;
import io.github.alathra.alathranwars.conflict.war.WarController;
import io.github.alathra.alathranwars.conflict.war.side.Side;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * The server side implementation of {@link AlathranWarsAPI}.
 *
 * <p>Also aggregates the internal facades, which is why plugin internals call
 * {@link #getInstance()} here rather than on the published entry point. Third-party consumers
 * want {@link AlathranWarsAPI#getInstance()} instead.
 */
@ApiStatus.Internal
public class AlathranWarsAPIImpl extends AlathranWarsAPI implements WarAPI, MercenaryAPI, WarCooldownAPI, EnlistAPI, DeathAPI, ColorAPI {
    /**
     * Gets the instance of the API, typed as the implementation so the internal facades are
     * reachable. Hides {@link AlathranWarsAPI#getInstance()}.
     *
     * @return the instance
     */
    public static AlathranWarsAPIImpl getInstance() {
        return (AlathranWarsAPIImpl) AlathranWarsAPI.getInstance();
    }

    @Override
    public @NotNull Collection<War> getWars() {
        return WarController.getInstance().getWars();
    }

    @Override
    public @Nullable War getWar(@NotNull String name) {
        return WarController.getInstance().getWar(name);
    }

    @Override
    public @Nullable Side getPlayerSide(@NotNull OfflinePlayer player, @NotNull War war) {
        return war.getPlayerSide(player.getUniqueId());
    }

    @Override
    public boolean isInWar(@NotNull OfflinePlayer player) {
        return WarController.getInstance().isInAnyWars(player.getUniqueId());
    }

    @Override
    public int getSiegeBattlefieldRange() {
        return SiegeImpl.BATTLEFIELD_RANGE;
    }

    @Override
    public int getSiegeStartMinRange() {
        return SiegeImpl.BATTLEFIELD_START_MIN_RANGE;
    }

    @Override
    public int getSiegeStartMaxRange() {
        return SiegeImpl.BATTLEFIELD_START_MAX_RANGE;
    }
}
