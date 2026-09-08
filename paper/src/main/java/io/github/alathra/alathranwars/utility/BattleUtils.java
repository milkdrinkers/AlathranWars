package io.github.alathra.alathranwars.utility;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import io.github.alathra.alathranwars.api.AlathranWarsAPIImpl;
import io.github.alathra.alathranwars.conflict.battle.Battle;
import io.github.alathra.alathranwars.conflict.battle.siege.Siege;
import io.github.alathra.alathranwars.conflict.battle.siege.SiegeImpl;
import io.github.alathra.alathranwars.conflict.war.War;
import io.github.alathra.alathranwars.conflict.war.WarController;
import io.github.alathra.alathranwars.conflict.war.side.Side;
import io.github.alathra.alathranwars.data.ControlPoint;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.translation.Argument;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class BattleUtils {
    public record SidedTown(War war, Side side, Town town) {
    }

    /**
     * Finds the nearest siegable town for a player, excluding allies and active sieges.
     *
     * @param player             The player to find the nearest siegable town for.
     * @param ignoreAllies       Whether to ignore allied towns.
     * @param ignoreActiveSieges Whether to ignore towns under active sieges.
     * @return An Optional containing the nearest SidedTown, or empty if none found.
     */
    public static Optional<SidedTown> getNearestSiegableTown(
        final Player player,
        final boolean ignoreAllies,
        final boolean ignoreActiveSieges
    ) {
        final Location playerLoc = player.getLocation();

        return WarController.getInstance().getWars(player).stream()
            .filter(War::isWarTime)
            .flatMap(war -> war.getSides().stream())
            .filter(side -> !side.isOnSide(player) && !side.isSiegeGraceActive()) // Excludes allies and sides with grace time
            .flatMap(side -> side.getTowns().stream().map(t -> new SidedTown(side.getWar(), side, t))) // Excludes surrendered towns
            .filter(t -> !WarController.getInstance().isInAnySieges(t.town())) // Excludes towns under siege
            .filter(t -> {
                // Excludes towns without a control point
                final Location l = ControlPoint.get(t.town);
                if (l == null)
                    return false;

                // Excludes towns in other worlds
                if (!l.getWorld().equals(playerLoc.getWorld()))
                    return false;

                final double maxDistance = Math.pow(Cfg.get().battles.sieges.trigger.range, 2);

                // Excludes towns that are too far away
                return l.distanceSquared(playerLoc) <= maxDistance;
            })
            .reduce((t1, t2) -> { // Returns the nearest town
                final Location loc1 = ControlPoint.get(t1.town);
                final double dist1 = loc1.distanceSquared(playerLoc);

                final Location loc2 = ControlPoint.get(t2.town);
                final double dist2 = loc2.distanceSquared(playerLoc);

                return dist1 < dist2 ? t1 : t2;
            });
    }

    /**
     * Checks if a player is a captain of their side in the current war.
     * A captain is a mayor, co-mayor, king, co-king or war leader in a war.
     *
     * @param player The player to check.
     * @return True if the player is a captain, false otherwise.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isCaptain(final Player player) {
        final Resident res = TownyAPI.getInstance().getResident(player);
        if (res == null)
            return false;

        return res.isMayor() || res.hasTownRank("co-mayor") || res.isKing() || res.hasNationRank("co-king");
    }

    public static boolean isSurrendered(
        final Player player,
        final Side side
    ) {
        return side.isSurrendered(player);
    }

    /**
     * Attempts to dynamically start a siege at the player's current location.
     *
     * @param player The player attempting to start the siege.
     */
    public static void attemptSiegeAtLocation(
        final Player player
    ) {
        if (!BattleUtils.isCaptain(player)) {
            player.sendMessage(Component.translatable("alathranwars.battle.siege.start-attempt.not-war-captain"));
            return;
        }

        if (!AlathranWarsAPIImpl.getInstance().hasActiveWarTime(player)) {
            player.sendMessage(Component.translatable("alathranwars.battle.siege.start-attempt.not-war-time"));
            return;
        }

        final Optional<SidedTown> nearestSiegableTown = getNearestSiegableTown(player, true, true);
        if (nearestSiegableTown.isEmpty()) {
            player.sendMessage(Component.translatable("alathranwars.battle.siege.start-attempt.no-town-near"));
            return;
        }

        final SidedTown sidedTown = nearestSiegableTown.get();

        if (!isCaptain(player)) {
            player.sendMessage(Component.translatable("alathranwars.battle.siege.start-attempt.not-captain"));
            return;
        }

        if (isSurrendered(player, sidedTown.side())) {
            player.sendMessage(Component.translatable("alathranwars.battle.siege.start-attempt.surrendered"));
            return;
        }

        startSiege(player, sidedTown.town(), sidedTown.war(), sidedTown.side());
    }

    public static void startSiege(
        Player siegeLeader,
        Town town,
        War war,
        Side defendingSide
    ) {
        defendingSide.setSiegeGrace();
        Siege siege = new SiegeImpl(war, town, siegeLeader);

        war.addSiege(siege);

        Bukkit.broadcast(
            Component.translatable(
                "alathranwars.battle.siege.event.started.global",
                Argument.component("prefix", Component.translatable("alathranwars.prefix")),
                Argument.string("town", town.getName()),
                Argument.string("side", SideUtils.getOpponent(defendingSide).getName()))
        );

        siege.start();
    }

    public static Stream<? extends Battle> getClosestBattles(Location location, double range) {
        final double rangeSquared = range * range;
        return WarController.getInstance().getWars().stream()
            .filter(war -> war.isWarTime() || !war.getSieges().isEmpty())
            .flatMap(war -> war.getSieges().stream())
            .filter(battle -> battle.getControlPoint().getWorld().equals(location.getWorld()))
            .filter(battle -> battle.getControlPoint().distanceSquared(location) <= rangeSquared);
    }

    public static Optional<? extends Battle> getClosestBattle(Location location, double range) {
        return getClosestBattle(location, range, null);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Battle> Optional<? extends Battle> getClosestBattle(Location location, double range, @Nullable Predicate<T> filter) {
        return getClosestBattles(location, range)
            .filter(battle -> filter == null || filter.test((T) battle))
            .reduce((battle1, battle2) -> {
                if (battle1 instanceof Siege siege1 && battle2 instanceof Siege siege2) {
                    final double dist1 = siege1.getControlPoint().distanceSquared(location);
                    final double dist2 = siege2.getControlPoint().distanceSquared(location);
                    return dist1 < dist2 ? battle1 : battle2;
                }
                return battle1;
            });
    }

    public static boolean isOnBattlefield(Player p, Battle battle) {
        if (!(battle instanceof Siege siege))
            return false;

        if (!siege.getControlPoint().getWorld().equals(p.getWorld()))
            return false;

        return siege.getControlPoint().distanceSquared(p.getLocation()) <= SiegeImpl.BATTLEFIELD_RANGE_SQUARED;
    }
}
