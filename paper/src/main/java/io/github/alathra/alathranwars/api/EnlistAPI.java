package io.github.alathra.alathranwars.api;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import io.github.alathra.alathranwars.conflict.war.War;
import io.github.alathra.alathranwars.conflict.war.WarController;
import io.github.alathra.alathranwars.conflict.war.side.Side;
import io.github.alathra.alathranwars.hook.Hook;
import io.github.alathra.alathranwars.utility.Cfg;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;

import java.time.Duration;

public interface EnlistAPI {
    enum EnlistResult {
        SUCCESS,
        ALREADY_ENLISTED,
        OFFENSIVE_COOLDOWN,
        DEFENSIVE_COOLDOWN,
        NOT_ENOUGH_PLAYTIME,
    }

    default EnlistResult canEnlist(Player player, War war, Side side) {
        final boolean isOffensive = war.getAttacker().equals(side);
        if (isOffensive && AlathranWarsAPIImpl.getInstance().hasAttackCooldown(player))
            return EnlistResult.OFFENSIVE_COOLDOWN;

        if (!isOffensive && AlathranWarsAPIImpl.getInstance().hasDefenseCooldown(player))
            return EnlistResult.DEFENSIVE_COOLDOWN;

        // Check if player meets playtime requirements
        if (!checkPlaytime(player))
            return EnlistResult.NOT_ENOUGH_PLAYTIME;

        return EnlistResult.SUCCESS;
    }

    /**
     * Checks if a player has enough playtime to enlist in a war.
     *
     * @param player the player to check
     * @return true if the player has enough playtime, false otherwise
     */
    default boolean checkPlaytime(Player player) {
        if (Hook.getActiveUpkeepHook().isHookLoaded())
            return Hook.getActiveUpkeepHook().hasRequiredPlaytime(player);

        final Duration requiredPlaytime = Duration.ofMinutes(Cfg.get().war.requiredPlaytime);
        return getPlaytime(player).compareTo(requiredPlaytime) > 0;
    }

    /**
     * Gets the required playtime for enlisting in a war.
     *
     * @return the required playtime as a Duration
     */
    default Duration getPlaytimeRequired() {
        return Duration.ofMinutes(Cfg.get().war.requiredPlaytime);
    }

    /**
     * Gets the playtime of a player.
     *
     * @param player the player to get the playtime for
     * @return the playtime as a Duration
     */
    default Duration getPlaytime(Player player) {
        final int ticksPlayed = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
        return Duration.ofMillis(ticksPlayed * 50L);
    }

    default void applyEnlist(Player player, War war, Side side) {
        final boolean isOffensive = war.getAttacker().equals(side);
        if (isOffensive) {
            AlathranWarsAPIImpl.getInstance().setAttackCooldown(player);
        } else {
            AlathranWarsAPIImpl.getInstance().setDefenseCooldown(player);
        }
    }

    /**
     * Enlists a player in all wars they are eligible for.
     *
     * @param player the player to enlist
     * @return true if the player was enlisted in at least one war, false otherwise
     * @since 4.0.0
     */
    default boolean enlist(Player player) {
        final Resident res = TownyAPI.getInstance().getResident(player);
        if (res == null)
            return false;

        final Town town = res.getTownOrNull();

        boolean added = false;
        for (War war : WarController.getInstance().getWars(town)) {
            if (war.isInWar(player))
                continue;

            final Side side = war.getSide(town);
            if (side == null)
                continue;

            if (!canEnlist(player, war, side).equals(EnlistResult.SUCCESS))
                continue;

            side.add(player);
            applyEnlist(player, war, side);
            added = true;
        }

        return added;
    }
}
