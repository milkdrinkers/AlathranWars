package io.github.alathra.alathranwars.conflict.war;

import io.github.alathra.alathranwars.conflict.war.side.SideCreationException;

public class WarCreationException extends SideCreationException {
    public WarCreationException(String errorMessage) {
        super(errorMessage);
    }
}
