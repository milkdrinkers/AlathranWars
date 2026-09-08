package io.github.alathra.alathranwars.conflict.battle.siege;

import io.github.alathra.alathranwars.conflict.battle.BattleRunnable;
import io.github.alathra.alathranwars.conflict.battle.bossbar.BossBarManager;
import io.github.alathra.alathranwars.conflict.battle.bossbar.WrappedBossBar;
import io.github.alathra.alathranwars.conflict.war.side.SideImpl;
import io.github.alathra.alathranwars.data.ControlPoint;
import io.github.alathra.alathranwars.enums.CaptureProgressDirection;
import io.github.alathra.alathranwars.enums.battle.BattleSide;
import io.github.alathra.alathranwars.enums.battle.BattleVictoryReason;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.translation.Argument;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;

import static io.github.alathra.alathranwars.enums.CaptureProgressDirection.*;

public class SiegeRunnable extends BattleRunnable {
    // Settings
    private static final Duration ANNOUNCEMENT_COOLDOWN = Duration.ofMinutes(5);
    public final static int CAPTURE_RANGE = 10;
    public final static double CAPTURE_RANGE_SQUARED = Math.pow(CAPTURE_RANGE, 2);
    private final @NotNull SiegeImpl siege;

    // Variables
    private @NotNull CaptureProgressDirection oldProgressDirection = UNCONTESTED;
    private Instant nextAnnouncement;

    /**
     * Start a siege
     *
     * @param siege the siege
     */
    public SiegeRunnable(@NotNull SiegeImpl siege) {
        super(20L);
        this.siege = siege;

        siege.getProgressManager().set(0);

        siege.setTownSpawn(siege.getTown().getSpawnOrNull());
        siege.setControlPoint(ControlPoint.get(siege.getTown()));

        nextAnnouncement = Instant.now().plus(ANNOUNCEMENT_COOLDOWN);

        updateBossBar(CONTESTED);
    }

    /**
     * Resume a siege at tick
     *
     * @param siege         the siege
     * @param siegeProgress the siege ticks
     */
    public SiegeRunnable(@NotNull SiegeImpl siege, int siegeProgress) {
        super(20L);
        this.siege = siege;

        siege.getProgressManager().set(siegeProgress);

        siege.setTownSpawn(siege.getTown().getSpawnOrNull());
        siege.setControlPoint(ControlPoint.get(siege.getTown()));

        nextAnnouncement = Instant.now().plus(ANNOUNCEMENT_COOLDOWN);

        updateBossBar(CONTESTED);
    }

    @Override
    public void run() {
        final @Nullable Location controlPoint = siege.getControlPoint();
        final @Nullable Location townSpawn = siege.getTownSpawn();

        if (controlPoint == null)
            return;

        if (townSpawn == null)
            return;

        // Progress the siege
        final int attackersOnPoint = getPeopleOnPoint(controlPoint, BattleSide.ATTACKER);
        final int defendersOnPoint = getPeopleOnPoint(controlPoint, BattleSide.DEFENDER);
        final @NotNull CaptureProgressDirection progressDirection = getSiegeProgressDirection(attackersOnPoint, defendersOnPoint);

        // Siege is past max time or attackers haven't touched in time, defenders won
        if (
            (!progressDirection.equals(CONTESTED) && !progressDirection.equals(UP)) &&
                (Instant.now().isAfter(siege.getEndTime()) /*||
                    Instant.now().isAfter(siege.getLastTouched().plus(ATTACKERS_MUST_TOUCH_END))*/)
        ) {
            siege.defendersWin(BattleVictoryReason.OPPONENT_LOST);
            return;
        }

        // Attackers captured the town
        if (siege.getProgressManager().get() >= siege.MAX_SIEGE_PROGRESS) {
            siege.attackersWin(BattleVictoryReason.OPPONENT_LOST);
            return;
        }

        // Update progress on siege
        switch (progressDirection) {
            case UP -> {
                final int playerOnPointDiff = attackersOnPoint - defendersOnPoint;

                // If you have less than 5 people contesting you get (4 + excessPlayers) points per second
                if (playerOnPointDiff < 5) {
                    siege.getProgressManager().set(siege.getProgressManager().get() + (4 + playerOnPointDiff));
                } else {
                    siege.getProgressManager().set(siege.getProgressManager().get() + 10);
                }
            }
            case DOWN -> siege.getProgressManager().set(siege.getProgressManager().get() - 10);
        }

        if (oldProgressDirection != progressDirection) {
            switch (progressDirection) {
                case UP -> {
                    siege.getPlayersOnBattlefield().forEach(p -> p.sendMessage(
                        Component.translatable(
                            "alathranwars.battle.siege.progress.capturing",
                            Argument.component("prefix", Component.translatable("alathranwars.prefix")))
                    ));
                }
                case CONTESTED -> {
                    if (oldProgressDirection.equals(UNCONTESTED))
                        siege.getPlayersOnBattlefield().forEach(p -> p.sendMessage(
                            Component.translatable(
                                "alathranwars.battle.siege.progress.contested",
                                Argument.component("prefix", Component.translatable("alathranwars.prefix")))
                        ));
                }
                case UNCONTESTED -> {
                    if (oldProgressDirection.equals(UP) || oldProgressDirection.equals(CONTESTED) || oldProgressDirection.equals(DOWN))
                        siege.getPlayersOnBattlefield().forEach(p -> p.sendMessage(
                            Component.translatable(
                                "alathranwars.battle.siege.progress.uncontested",
                                Argument.component("prefix", Component.translatable("alathranwars.prefix")))
                        ));
                }
                case DOWN -> {
                    if (oldProgressDirection.equals(UP) || oldProgressDirection.equals(CONTESTED))
                        siege.getPlayersOnBattlefield().forEach(p -> p.sendMessage(
                            Component.translatable(
                                "alathranwars.battle.siege.progress.secured",
                                Argument.component("prefix", Component.translatable("alathranwars.prefix")))
                        ));
                }
            }
        }

        if (Instant.now().isAfter(nextAnnouncement)) {
            nextAnnouncement = Instant.now().plus(ANNOUNCEMENT_COOLDOWN);

            siege.getPlayersOnBattlefield().forEach(p -> p.sendMessage(
                Component.translatable(
                    "alathranwars.battle.siege.progress.time-remaining",
                    Argument.component("prefix", Component.translatable("alathranwars.prefix")),
                    Argument.numeric("time", Duration.between(Instant.now(), siege.getEndTime()).toMinutesPart()))
            ));
        }

        updateBossBar(progressDirection);
        oldProgressDirection = progressDirection;
    }

    private void updateBossBar(CaptureProgressDirection progressDirection) {
        final BossBarManager manager = siege.getBossBarManager();
        final boolean isAggressorAttackingTeam = ((SideImpl) siege.getAttackerSide()).getSide().equals(BattleSide.ATTACKER);

        final BossBar.Color barColor = switch (progressDirection) {
            case UP -> {
                if (isAggressorAttackingTeam) {
                    yield BossBar.Color.RED;
                } else {
                    yield BossBar.Color.BLUE;
                }
            }
            case CONTESTED -> BossBar.Color.YELLOW;
            case UNCONTESTED -> BossBar.Color.WHITE;
            case DOWN -> {
                if (isAggressorAttackingTeam) {
                    yield BossBar.Color.BLUE;
                } else {
                    yield BossBar.Color.RED;
                }
            }
        };
        final TextColor color = switch (progressDirection) {
            case UP -> {
                if (isAggressorAttackingTeam) {
                    yield NamedTextColor.RED;
                } else {
                    yield NamedTextColor.BLUE;
                }
            }
            case CONTESTED -> NamedTextColor.YELLOW;
            case UNCONTESTED -> NamedTextColor.WHITE;
            case DOWN -> {
                if (isAggressorAttackingTeam) {
                    yield NamedTextColor.BLUE;
                } else {
                    yield NamedTextColor.RED;
                }
            }
        };

        final float siegePrecentage = siege.getSiegeProgressPercentage();

        final Component name;
        if (Instant.now().isBefore(siege.getEndTime())) {
            name = Component.translatable(
                "alathranwars.battle.siege.bossbar.progress",
                Argument.tagResolver(Placeholder.styling("bar_color", color)),
                Argument.string("progress", "%.0f%%".formatted(siegePrecentage * 100)),
                Argument.numeric("time", Duration.between(Instant.now(), siege.getEndTime()).toMinutes()),
                Argument.string("town", siege.getName()));
        } else {
            name = Component.translatable(
                "alathranwars.battle.siege.bossbar.overtime",
                Argument.tagResolver(Placeholder.styling("bar_color", color)));
        }

        final WrappedBossBar bar1 = manager.getAttackerBar();
        final WrappedBossBar bar2 = manager.getDefenderBar();
        final WrappedBossBar bar3 = manager.getSpectatorBar();

        bar1.color(barColor);
        bar2.color(barColor);
        bar3.color(barColor);

        bar1.progress(siegePrecentage);
        bar2.progress(siegePrecentage);
        bar3.progress(siegePrecentage);

        bar1.name(name);
        bar2.name(name);
        bar3.name(name);

        bar1.update();
        bar2.update();
        bar3.update();
    }

    private int getPeopleOnPoint(Location controlPoint, BattleSide battleSide) {
        int onPoint = 0;

        for (final Player p : (battleSide.equals(BattleSide.ATTACKER) ? siege.getPlayersInZone(BattleSide.ATTACKER) : siege.getPlayersInZone(BattleSide.DEFENDER))) {
            if (p.isDead())
                continue;

            if (!controlPoint.getWorld().equals(p.getLocation().getWorld()))
                continue;

            if (controlPoint.distanceSquared(p.getLocation()) > CAPTURE_RANGE_SQUARED)
                continue;

            if (!p.hasLineOfSight(controlPoint))
                continue;

            onPoint += 1;
        }

        return onPoint;
    }

    @SuppressWarnings("ConstantConditions")
    private CaptureProgressDirection getSiegeProgressDirection(int attackersOnPoint, int defendersOnPoint) {
        final boolean attackersAreOnPoint = attackersOnPoint > 0;
        final boolean defendersAreOnPoint = defendersOnPoint > 0;

        if (attackersAreOnPoint)
            siege.setLastTouched(Instant.now());

        if (attackersAreOnPoint && defendersAreOnPoint) {

            if (attackersOnPoint > defendersOnPoint)
                return UP;

            return CONTESTED;
        } else if (attackersAreOnPoint && !defendersAreOnPoint) {
            if (siege.getProgressManager().get() == siege.MAX_SIEGE_PROGRESS)
                return CONTESTED;

            return UP;
        } else {
            if (siege.getProgressManager().get() == 0)
                return UNCONTESTED;

            // If the attackers haven't touched in a while, begin reverting progress
            if (Instant.now().isAfter(siege.getLastTouched().plus(siege.ATTACKERS_MUST_TOUCH_REVERT))) {
                return DOWN;
            } else {
                return UNCONTESTED;
            }
        }
    }
}