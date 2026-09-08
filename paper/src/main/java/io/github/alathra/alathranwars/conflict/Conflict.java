package io.github.alathra.alathranwars.conflict;

import io.github.alathra.alathranwars.Reloadable;
import io.github.alathra.alathranwars.enums.ConflictType;

import java.util.UUID;

public abstract class Conflict implements IUnique<Conflict>, Reloadable {
    private ConflictType conflictType;
    private final UUID uuid;

    public Conflict(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * @return conflict uuid
     */
    @Override
    public UUID getUUID() {
        return uuid;
    }

    /**
     * Equals boolean.
     *
     * @param uuid the uuid
     * @return the boolean
     */
    @Override
    public boolean equals(UUID uuid) {
        return getUUID().equals(uuid);
    }

    /**
     * Equals boolean.
     *
     * @param conflict the conflict
     * @return the boolean
     */
    public boolean equals(Conflict conflict) {
        return conflict.getUUID().equals(uuid);
    }
}
