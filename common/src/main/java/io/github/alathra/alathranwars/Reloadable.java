package io.github.alathra.alathranwars;

/**
 * Implemented in classes that should support being reloaded IE executing the methods during runtime after startup.
 */
public interface Reloadable {
    /**
     * On plugin load.
     */
    default void onLoad(AbstractAlathranWars plugin) {
    }

    /**
     * On plugin enable.
     */
    default void onEnable(AbstractAlathranWars plugin) {
    }

    /**
     * On plugin disable.
     */
    default void onDisable(AbstractAlathranWars plugin) {
    }

}
