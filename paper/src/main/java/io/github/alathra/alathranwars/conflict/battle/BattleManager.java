package io.github.alathra.alathranwars.conflict.battle;

/**
 * Implemented in manager classes of Battles.
 */
public interface BattleManager {
    /**
     * Executed when the associated battle is started.
     */
    void start();

    /**
     * Executed when the associated battle is stopped.
     */
    void stop();
}
