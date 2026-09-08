package io.github.alathra.alathranwars.conflict.war.side.spawn;

import io.github.alathra.alathranwars.AlathranWars;
import net.kyori.adventure.text.Component;

public class SpawnCreationException extends Exception {
    private final Component component;

    public SpawnCreationException(String errorMessage) {
        super(errorMessage);
        component = AlathranWars.plainTextComponentSerializer.deserialize(errorMessage);
    }

    public SpawnCreationException(Component errorComponent) {
        super(AlathranWars.plainTextComponentSerializer.serialize(errorComponent));
        component = errorComponent;
    }

    public Component getComponent() {
        return component;
    }
}
