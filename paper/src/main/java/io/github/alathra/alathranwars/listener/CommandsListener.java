package io.github.alathra.alathranwars.listener;

import io.github.alathra.alathranwars.conflict.war.WarController;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.translation.Argument;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.jetbrains.annotations.NotNull;

public class CommandsListener implements Listener {
    final static String[] PREFIXES = new String[]{
        "", "towny", "essentials", "wild", "minecraft"
    };
    final static String[] PREFIXES_TOWNY = new String[]{
        "", "towny"
    };
    final static String[] BLACKLISTED_X_LONG = new String[]{
    };
    final static String[] BLACKLISTED_SHORT = new String[]{
        "homes",
        "home",
        "wild", "rtp", "wilderness", "wildtp",
        "tpa", "tpahere", "tpaccept", "tpacancel",
        "etpa", "etpahere", "etpaccept", "etpacancel",
        "ehomes"

    };

    @EventHandler
    public void onCommandSend(final @NotNull PlayerCommandPreprocessEvent event) {

        @NotNull Player p = event.getPlayer();

        //if player is admin, ignore this behavior
//        if (p.hasPermission("!AlathranWars.admin")) return;

        String[] args = event.getMessage().split(" ");

        //Prevent players from teleporting during a siege

        if (!WarController.getInstance().isInActiveWar(p))
            return;

        //set spawn and properties
        if (args.length >= 3) {
            @NotNull String parse = (args[0].charAt(0) == '/' ? args[0].substring(1) : args[0]) + " " + args[1] + " " + args[2];
            for (@NotNull String prefix : PREFIXES_TOWNY) {
                //payment check
                for (String cmd : BLACKLISTED_X_LONG) {
                    if (parse.equalsIgnoreCase(prefix + (prefix.isEmpty() ? "" : ":") + cmd)) {
                        p.sendMessage(Component.translatable(
                            "alathranwars.commands.blocked.property-during-siege",
                            Argument.component("prefix", Component.translatable("alathranwars.prefix"))));
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }

        //random tp commands
        if (args.length >= 1) {
            //parse what we have, remove the starting slash
            @NotNull String parse = args[0].charAt(0) == '/' ? args[0].substring(1) : args[0];
            for (@NotNull String prefix : PREFIXES) {
                for (String cmd : BLACKLISTED_SHORT) {
                    //check for each prefix
                    if (parse.equalsIgnoreCase(prefix + (prefix.isEmpty() ? "" : ":") + cmd)) {
                        //spawn world check
                        if (p.getWorld().getName().equalsIgnoreCase("world")) {
                            p.sendMessage(Component.translatable(
                                "alathranwars.teleport.stuck-in-spawn",
                                Argument.component("prefix", Component.translatable("alathranwars.prefix"))));
                            p.sendMessage(Component.translatable(
                                "alathranwars.teleport.stuck-in-spawn-hint",
                                Argument.component("prefix", Component.translatable("alathranwars.prefix"))));
                            event.setCancelled(true);
                            return;
                        }

                        p.sendMessage(Component.translatable(
                            "alathranwars.teleport.siege-blocked",
                            Argument.component("prefix", Component.translatable("alathranwars.prefix"))));
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }
}
