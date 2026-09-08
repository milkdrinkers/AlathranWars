package io.github.alathra.alathranwars.utility;


import io.github.alathra.alathranwars.AbstractAlathranWars;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;

/**
 * A class that provides shorthand access to {@link AbstractAlathranWars#getComponentLogger}.
 */
public final class Logger {
    /**
     * Get component logger. Shorthand for:
     *
     * @return the component logger {@link AbstractAlathranWars#getComponentLogger}.
     */
    @NotNull
    public static ComponentLogger get() {
        return AbstractAlathranWars.getInstance().getComponentLogger();
    }
}
