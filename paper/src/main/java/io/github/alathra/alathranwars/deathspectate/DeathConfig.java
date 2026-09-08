package io.github.alathra.alathranwars.deathspectate;

import io.github.alathra.alathranwars.utility.Cfg;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;

public abstract class DeathConfig {
    private static final String DEATH_COMMAND_DENIED = "";
    private static final String DEATH_MESSAGE = "";

    public static String getCommandDeniedMessage() {
        return DEATH_COMMAND_DENIED;
    }

    public static String getYouDiedMessage() {
        return DEATH_MESSAGE;
    }

    public static boolean canSpectate(Player player, EntityDamageEvent.DamageCause damageCause) {
        return damageCause != null &&
            isWhitelistedWorld(player.getWorld()) &&
            !Cfg.get().respawns.blacklistedDamageCauses
                .stream()
                .map(name -> EntityDamageEvent.DamageCause.valueOf(name.toUpperCase()))
                .toList()
                .contains(damageCause) &&
            hasPermissionToSpectate(player);
    }

    public static Component formatter(String stringToFormat, Object... formats) {
        return formatter(MessageFormat.format(stringToFormat, formats));
    }

    public static Component formatter(String stringToFormat) {
        return ColorParser.of(stringToFormat).legacy().build();
    }

    private static boolean isWhitelistedWorld(World world) {
        final List<World> whitelistedWorlds = Cfg.get().respawns.whitelistedWorlds.stream()
            .map(name -> Bukkit.getServer().getWorld(name))
            .filter(Objects::nonNull)
            .toList();
        return whitelistedWorlds.isEmpty() || whitelistedWorlds.contains(world);
    }

    private static boolean hasPermissionToSpectate(Player player) {
        return !Cfg.get().respawns.permission || player.hasPermission("alathranwars.death.spectate");
    }
}
