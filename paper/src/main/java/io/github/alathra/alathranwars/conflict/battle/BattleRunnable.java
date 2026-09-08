package io.github.alathra.alathranwars.conflict.battle;

/**
 * A wrapper for runnables so they can be easily associated with battles.
 *
 * @implNote This is used for creating runnables that are executed while a battle is active.
 */
public abstract class BattleRunnable implements Runnable {
    private final long tickRate;

    public BattleRunnable(long tickRate) {
        this.tickRate = tickRate;
    }

    public long getTickRate() {
        return tickRate;
    }

    public void stop() {
    }
}
