package io.github.alathra.alathranwars.conflict;

import io.github.alathra.alathranwars.conflict.war.War;
import org.jetbrains.annotations.Nullable;

public interface IAssociatedWar {
    /**
     * Gets the associated war with this object from the war uuid
     *
     * @return the associated war or null if it doesn't exist yet (Like during war creation)
     */
    @Nullable
    War getWar();

    /**
     * Check whether a war is associated with this class
     *
     * @param war the war to compare with
     * @return if the war is the one associated with this object
     */
    boolean equals(War war);
}
