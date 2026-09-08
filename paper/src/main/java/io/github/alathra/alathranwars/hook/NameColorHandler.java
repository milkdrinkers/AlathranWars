package io.github.alathra.alathranwars.hook;

import io.github.alathra.alathranwars.conflict.war.War;
import io.github.alathra.alathranwars.conflict.war.WarController;
import io.github.alathra.alathranwars.conflict.war.side.Side;
import io.github.alathra.alathranwars.utility.Logger;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class NameColorHandler {
    private static NameColorHandler instance;
    private final Map<Player, String> playerColor = new HashMap<>();
    private final Map<Player, String> playerInitial = new HashMap<>();
    private final Map<Player, String> playerPrefix = new HashMap<>();

    private NameColorHandler() {
        if (instance != null)
            Logger.get().warn("Tried to re-initialize singleton");
    }

    @NotNull
    public static NameColorHandler getInstance() {
        if (instance == null)
            instance = new NameColorHandler();

        return instance;
    }

    public void removePlayer(Player p) {
        playerColor.remove(p);
        playerInitial.remove(p);
        playerPrefix.remove(p);
    }

    public void calculatePlayerColors(Player p) {
        if (!WarController.getInstance().isInAnyWars(p)) {
            removePlayer(p);
            return;
        }

        Optional<War> war = WarController.getInstance().getWars(p).stream().findFirst();

        if (war.isEmpty())
            return;

        final boolean isPlayerSurrendered = war.get().getSide1().isSurrendered(p) || war.get().getSide2().isSurrendered(p);

        if (isPlayerSurrendered)
            return;

        final boolean isSideOne = war.get().getSide1().isOnSide(p);
        final Side side = war.get().getPlayerSide(p);

        if (side == null)
            return;

        if (isSideOne) {
            playerColor.put(p, "<red>");
        } else {
            playerColor.put(p, "<blue>");
        }

        final String sideName = side.getName().replace("_", " ");
        playerInitial.put(p, sideName.substring(0, 1).toUpperCase());

        final String prefix = isSideOne ? "<red><bold>⚔ %s ⚔ " : "<blue><bold>⚔ %s ⚔ ";
        playerPrefix.put(p, prefix.formatted(sideName));
    }

    public boolean isPlayerUsingModifiedName(Player p) {
        calculatePlayerColors(p);
        return WarController.getInstance().isInAnyWars(p);
    }

    public String getPlayerTabNameColor(Player p) {
        return "%s[%s] ".formatted(playerColor.get(p), playerInitial.get(p));
    }

    public String getPlayerNameColor(Player p) {
        return playerColor.get(p);
    }

    public String getPlayerPrefix(Player p) {
        return playerPrefix.get(p);
    }
}
