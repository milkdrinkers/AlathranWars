package io.github.alathra.alathranwars.listener.battle.siege;

import com.palmergames.bukkit.towny.object.Town;
import io.github.alathra.alathranwars.conflict.battle.siege.Siege;
import io.github.alathra.alathranwars.conflict.war.War;
import io.github.alathra.alathranwars.conflict.war.WarController;
import io.github.alathra.alathranwars.conflict.war.side.Side;
import io.github.alathra.alathranwars.enums.battle.BattleSide;
import io.github.alathra.alathranwars.enums.battle.BattleType;
import io.github.alathra.alathranwars.enums.battle.BattleVictoryReason;
import io.github.alathra.alathranwars.event.battle.*;
import io.github.alathra.alathranwars.utility.Cfg;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public class SiegeListener implements Listener {
    private final static Title.Times TITLE_TIMES = Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(500));
    private final static Sound SOUND_VICTORY = Sound.sound(Key.key("item.goat_horn.sound.0"), Sound.Source.VOICE, 0.5f, 1.0F);
    private final static Sound SOUND_DEFEAT = Sound.sound(Key.key("entity.wither.death"), Sound.Source.VOICE, 0.5f, 1.0F);
    private final static List<Sound> SOUND_GOATHORNS = List.of(
        Sound.sound(Key.key("item.goat_horn.sound.0"), Sound.Source.VOICE, 0.5f, new Random().nextFloat(0.9F, 1.0F)),
        Sound.sound(Key.key("item.goat_horn.sound.2"), Sound.Source.VOICE, 0.5f, new Random().nextFloat(0.9F, 1.0F)),
        Sound.sound(Key.key("item.goat_horn.sound.3"), Sound.Source.VOICE, 0.5f, new Random().nextFloat(0.9F, 1.0F)),
        Sound.sound(Key.key("item.goat_horn.sound.7"), Sound.Source.VOICE, 0.5f, new Random().nextFloat(0.9F, 1.0F))
    );

    /**
     * On battle start UI handling.
     *
     * @param e event
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onBattleStart(BattleStartEvent e) {
        if (!e.getBattle().getBattleType().equals(BattleType.SIEGE)) return;

        if (!(e.getBattle() instanceof Siege siege)) return;

        final Title defTitle = Title.title(
            Component.translatable(
                "alathranwars.battle.siege.event.started.attacker.title",
                Argument.string("town", siege.getTown().getName()),
                Argument.string("attacker", siege.getAttackerSide().getName()),
                Argument.string("defender", siege.getDefenderSide().getName())
            ),
            Component.translatable(
                "alathranwars.battle.siege.event.started.attacker.subtitle",
                Argument.string("town", siege.getTown().getName()),
                Argument.string("attacker", siege.getAttackerSide().getName()),
                Argument.string("defender", siege.getDefenderSide().getName())
            ),
            TITLE_TIMES
        );
        final Title attTitle = Title.title(
            Component.translatable(
                "alathranwars.battle.siege.event.started.defender.title",
                Argument.string("town", siege.getTown().getName()),
                Argument.string("attacker", siege.getAttackerSide().getName()),
                Argument.string("defender", siege.getDefenderSide().getName())
            ),
            Component.translatable(
                "alathranwars.battle.siege.event.started.defender.subtitle",
                Argument.string("town", siege.getTown().getName()),
                Argument.string("attacker", siege.getAttackerSide().getName()),
                Argument.string("defender", siege.getDefenderSide().getName())
            ),
            TITLE_TIMES
        );


        siege.getPlayersInZone(BattleSide.DEFENDER).forEach(player -> {
            player.showTitle(defTitle);
            player.playSound(SOUND_GOATHORNS.get(new Random().nextInt(1, SOUND_GOATHORNS.size())));
        });

        siege.getPlayersInZone(BattleSide.ATTACKER).forEach(player -> {
            player.showTitle(attTitle);
            player.playSound(SOUND_GOATHORNS.get(new Random().nextInt(1, SOUND_GOATHORNS.size())));
        });
    }

    /**
     * On battle end UI handling.
     *
     * @param e event
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onBattleResult(BattleResultEvent e) {
        if (!e.getBattle().getBattleType().equals(BattleType.SIEGE)) return;

        if (!(e.getBattle() instanceof Siege siege)) return;

        switch (e.getBattleVictor()) {
            case ATTACKER -> {
                if (e.getBattleVictoryReason().equals(BattleVictoryReason.OPPONENT_RETREAT)) {
                    Bukkit.broadcast(
                        Component.translatable(
                            "alathranwars.battle.siege.event.ended.attacker-victory.global.surrender",
                            Argument.component("prefix", Component.translatable("alathranwars.prefix")),
                            Argument.string("town", siege.getTown().getName()),
                            Argument.string("attacker", siege.getAttackerSide().getName()),
                            Argument.string("defender", siege.getDefenderSide().getName())
                        )
                    );
                } else {
                    Bukkit.broadcast(Component.translatable(
                        "alathranwars.battle.siege.event.ended.attacker-victory.global.forced",
                        Argument.component("prefix", Component.translatable("alathranwars.prefix")),
                        Argument.string("town", siege.getTown().getName()),
                        Argument.string("attacker", siege.getAttackerSide().getName()),
                        Argument.string("defender", siege.getDefenderSide().getName())
                    ));
                }

                final Title vicAttackTitle = Title.title(
                    Component.translatable(
                        "alathranwars.battle.siege.event.ended.attacker-victory.attacker.title",
                        Argument.string("town", siege.getTown().getName()),
                        Argument.string("attacker", siege.getAttackerSide().getName()),
                        Argument.string("defender", siege.getDefenderSide().getName())
                    ),
                    Component.translatable(
                        "alathranwars.battle.siege.event.ended.attacker-victory.attacker.subtitle",
                        Argument.string("town", siege.getTown().getName()),
                        Argument.string("attacker", siege.getAttackerSide().getName()),
                        Argument.string("defender", siege.getDefenderSide().getName())
                    ),
                    TITLE_TIMES
                );
                final Title losAttackTitle = Title.title(
                    Component.translatable(
                        "alathranwars.battle.siege.event.ended.attacker-victory.defender.title",
                        Argument.string("town", siege.getTown().getName()),
                        Argument.string("attacker", siege.getAttackerSide().getName()),
                        Argument.string("defender", siege.getDefenderSide().getName())
                    ),
                    Component.translatable(
                        "alathranwars.battle.siege.event.ended.attacker-victory.defender.subtitle",
                        Argument.string("town", siege.getTown().getName()),
                        Argument.string("attacker", siege.getAttackerSide().getName()),
                        Argument.string("defender", siege.getDefenderSide().getName())
                    ),
                    TITLE_TIMES
                );
                siege.getPlayersInZone(BattleSide.ATTACKER).forEach(player -> {
                    player.showTitle(vicAttackTitle);
                    player.playSound(SOUND_VICTORY);
                });
                siege.getPlayersInZone(BattleSide.DEFENDER).forEach(player -> {
                    player.showTitle(losAttackTitle);
                    player.playSound(SOUND_DEFEAT);
                });
            }
            case DEFENDER -> {
                if (e.getBattleVictoryReason().equals(BattleVictoryReason.OPPONENT_RETREAT)) {
                    Bukkit.broadcast(
                        Component.translatable(
                            "alathranwars.battle.siege.event.ended.defender-victory.global.surrender",
                            Argument.component("prefix", Component.translatable("alathranwars.prefix")),
                            Argument.string("town", siege.getTown().getName()),
                            Argument.string("attacker", siege.getAttackerSide().getName()),
                            Argument.string("defender", siege.getDefenderSide().getName())
                        )
                    );
                } else {
                    Bukkit.broadcast(
                        Component.translatable(
                            "alathranwars.battle.siege.event.ended.defender-victory.global.forced",
                            Argument.component("prefix", Component.translatable("alathranwars.prefix")),
                            Argument.string("town", siege.getTown().getName()),
                            Argument.string("attacker", siege.getAttackerSide().getName()),
                            Argument.string("defender", siege.getDefenderSide().getName())
                        )
                    );
                }

                final Title vicDefendTitle = Title.title(
                    Component.translatable(
                        "alathranwars.battle.siege.event.ended.defender-victory.attacker.title",
                        Argument.string("town", siege.getTown().getName()),
                        Argument.string("attacker", siege.getAttackerSide().getName()),
                        Argument.string("defender", siege.getDefenderSide().getName())
                    ),
                    Component.translatable(
                        "alathranwars.battle.siege.event.ended.defender-victory.attacker.subtitle",
                        Argument.string("town", siege.getTown().getName()),
                        Argument.string("attacker", siege.getAttackerSide().getName()),
                        Argument.string("defender", siege.getDefenderSide().getName())
                    ),
                    TITLE_TIMES
                );
                final Title losDefendTitle = Title.title(
                    Component.translatable(
                        "alathranwars.battle.siege.event.ended.defender-victory.defender.title",
                        Argument.string("town", siege.getTown().getName()),
                        Argument.string("attacker", siege.getAttackerSide().getName()),
                        Argument.string("defender", siege.getDefenderSide().getName())
                    ),
                    Component.translatable(
                        "alathranwars.battle.siege.event.ended.defender-victory.defender.subtitle",
                        Argument.string("town", siege.getTown().getName()),
                        Argument.string("attacker", siege.getAttackerSide().getName()),
                        Argument.string("defender", siege.getDefenderSide().getName())
                    ),
                    TITLE_TIMES
                );
                siege.getPlayersInZone(BattleSide.ATTACKER).forEach(player -> {
                    player.showTitle(vicDefendTitle);
                    player.playSound(SOUND_DEFEAT);
                });
                siege.getPlayersInZone(BattleSide.DEFENDER).forEach(player -> {
                    player.showTitle(losDefendTitle);
                    player.playSound(SOUND_VICTORY);
                });
            }
            case DRAW -> {
                Bukkit.broadcast(
                    Component.translatable(
                        "alathranwars.battle.siege.event.ended.draw.global",
                        Argument.component("prefix", Component.translatable("alathranwars.prefix")),
                        Argument.string("town", siege.getTown().getName()),
                        Argument.string("attacker", siege.getAttackerSide().getName()),
                        Argument.string("defender", siege.getDefenderSide().getName())
                    )
                );

                final Title drawTitle = Title.title(
                    Component.translatable(
                        "alathranwars.battle.siege.event.ended.draw.title",
                        Argument.string("town", siege.getTown().getName()),
                        Argument.string("attacker", siege.getAttackerSide().getName()),
                        Argument.string("defender", siege.getDefenderSide().getName())
                    ),
                    Component.translatable(
                        "alathranwars.battle.siege.event.ended.draw.subtitle",
                        Argument.string("town", siege.getTown().getName()),
                        Argument.string("attacker", siege.getAttackerSide().getName()),
                        Argument.string("defender", siege.getDefenderSide().getName())
                    ),
                    TITLE_TIMES
                );
                siege.getPlayersInZone(BattleSide.ATTACKER).forEach(player -> {
                    player.showTitle(drawTitle);
                    player.playSound(SOUND_DEFEAT);
                });
                siege.getPlayersInZone(BattleSide.DEFENDER).forEach(player -> {
                    player.showTitle(drawTitle);
                    player.playSound(SOUND_DEFEAT);
                });
            }
        }
    }

    /**
     * On battle end handle occupation results.
     *
     * @param e event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBattleResultOccupy(BattleResultEvent e) {
        if (!e.getBattle().getBattleType().equals(BattleType.SIEGE)) return;

        if (!(e.getBattle() instanceof Siege siege)) return;

        switch (e.getBattleVictor()) {
            case ATTACKER -> {
                War war = e.getWar();

                if (war.isEventWar()) return;

                Town town = siege.getTown();
                Side townSide = war.getSide(town);

                Objects.requireNonNull(town, "Town should not be null here");

                final boolean isLiberation = siege.getAttackerSide().equals(townSide); // Is the town being liberated or occupied

                if (isLiberation) { // Liberation siege
                    if (townSide.isSurrendered(town)) {
                        war.unsurrender(town);
                    }
                } else { // Occupation siege
                    if (!townSide.isSurrendered(town)) {
                        war.surrender(town);
                    }
                }
            }
            case DEFENDER, DRAW -> {
            }
        }
    }

    /**
     * On battle end handle war score distribution.
     *
     * @param e event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBattleResultWarScore(BattleResultEvent e) {
        if (!e.getBattle().getBattleType().equals(BattleType.SIEGE)) return;

        if (!(e.getBattle() instanceof Siege siege)) return;

        switch (e.getBattleVictor()) {
            case ATTACKER -> {
                siege.getAttackerSide().addScore(Cfg.get().battles.sieges.points.attackerVictory);
                siege.getDefenderSide().addScore(Cfg.get().battles.sieges.points.defenderLoss);
            }
            case DEFENDER -> {
                siege.getAttackerSide().addScore(Cfg.get().battles.sieges.points.attackerLoss);
                siege.getDefenderSide().addScore(Cfg.get().battles.sieges.points.defenderVictory);
            }
            case DRAW -> {
            }
        }
    }

    /**
     * On player entering a battlefield.
     *
     * @param e event
     */
    @EventHandler
    public void onBattleEnter(PlayerEnteredBattlefieldEvent e) {
        if (!e.getBattle().getBattleType().equals(BattleType.SIEGE)) return;

        if (!(e.getBattle() instanceof Siege siege)) return;

        final Player p = e.getPlayer();

        final Title defTitle = Title.title(
            Component.translatable(
                "alathranwars.battle.battlefield.entered.title",
                Argument.string("town", siege.getTown().getName()),
                Argument.string("attacker", siege.getAttackerSide().getName()),
                Argument.string("defender", siege.getDefenderSide().getName())
            ),
            Component.translatable(
                "alathranwars.battle.battlefield.entered.subtitle",
                Argument.string("town", siege.getTown().getName()),
                Argument.string("attacker", siege.getAttackerSide().getName()),
                Argument.string("defender", siege.getDefenderSide().getName())
            ),
            TITLE_TIMES
        );

        p.showTitle(defTitle);

        // Boss bar
        final BattleSide battleSide = siege.getPlayerBattleSide(p);
        siege.addBossBar(battleSide, p);
    }

    /**
     * On player leaving a battlefield.
     *
     * @param e event
     */
    @EventHandler
    public void onBattleLeave(PlayerLeftBattlefieldEvent e) {
        if (!e.getBattle().getBattleType().equals(BattleType.SIEGE)) return;

        if (!(e.getBattle() instanceof Siege siege)) return;

        final Player p = e.getPlayer();

        final Title defTitle = Title.title(
            Component.translatable(
                "alathranwars.battle.battlefield.left.title",
                Argument.string("town", siege.getTown().getName()),
                Argument.string("attacker", siege.getAttackerSide().getName()),
                Argument.string("defender", siege.getDefenderSide().getName())
            ),
            Component.translatable(
                "alathranwars.battle.battlefield.left.subtitle",
                Argument.string("town", siege.getTown().getName()),
                Argument.string("attacker", siege.getAttackerSide().getName()),
                Argument.string("defender", siege.getDefenderSide().getName())
            ),
            TITLE_TIMES
        );

        p.showTitle(defTitle);

        // Boss bar remove player from audience
        final BattleSide battleSide = siege.getPlayerBattleSide(p);
        siege.removeBossBar(battleSide, p);
    }

    @EventHandler
    public void onPlayerServerLeave(PlayerQuitEvent e) {
        final Player p = e.getPlayer();

        for (Siege siege : WarController.getInstance().getSieges()) {
            // Boss bar remove player from audience
            final BattleSide battleSide = siege.getPlayerBattleSide(p);
            siege.removeBossBar(battleSide, p);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMoveControlPoint(SetControlPointEvent e) {
        if (!WarController.getInstance().isInAnySieges(e.getTown()))
            return;

        Set<Siege> sieges = WarController.getInstance().getSieges();

        for (Siege siege : sieges) {
            if (!siege.getTown().equals(e.getTown()))
                continue;

            siege.setControlPoint(e.getNewLocation());
        }
    }
}
