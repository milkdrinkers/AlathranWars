package io.github.alathra.alathranwars.conflict.battle;

import io.github.alathra.alathranwars.conflict.IUnique;
import io.github.alathra.alathranwars.conflict.war.War;
import io.github.alathra.alathranwars.enums.battle.BattleType;
import io.github.alathra.alathranwars.enums.battle.BattleVictoryReason;
import org.jetbrains.annotations.ApiStatus;

public interface Battle extends IUnique<Battle> {
    War getWar();

    /**
     * Starts the battle
     */
    @ApiStatus.Internal
    void start();

    /**
     * Resumes the battle (after a server restart e.t.c.)
     */
    @ApiStatus.Internal
    void resume();

    /**
     * Stops the battle
     * </p>
     * Internal stop method for battles which triggers cleanup methods
     */
    @ApiStatus.Internal
    void stop(); // Ends battle

    /**
     * Stop a battle in favor of the attackers
     *
     * @param reason what triggered the end
     */
    void attackersWin(BattleVictoryReason reason);

    /**
     * Stop a battle in favor of the defenders
     *
     * @param reason what triggered the end
     */
    void defendersWin(BattleVictoryReason reason);

    /**
     * End a battle in favor of no one
     *
     * @param reason what triggered the end
     */
    void equalWin(BattleVictoryReason reason);

    /**
     * Equals boolean.
     *
     * @param battle the battle
     * @return the boolean
     */
    default boolean equals(Battle battle) {
        return getUUID().equals(battle.getUUID());
    }

    BattleType battleType = null;

    default BattleType getBattleType() {
        return battleType;
    }
}
