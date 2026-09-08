package io.github.alathra.alathranwars.command.towny;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyCommandAddonAPI;
import com.palmergames.bukkit.towny.command.BaseCommand;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.AddonCommand;
import com.palmergames.bukkit.towny.object.Town;
import io.github.alathra.alathranwars.conflict.war.WarController;
import io.github.alathra.alathranwars.data.ControlPoint;
import io.github.alathra.alathranwars.event.battle.PreSetControlPointEvent;
import io.github.alathra.alathranwars.event.battle.SetControlPointEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.translation.Argument;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

class TownCommands extends BaseCommand implements TabExecutor {
    private static final String PERM_TOWN = "alathranwars.command.capturepoint";

    public TownCommands() {
        AddonCommand capturePointCommand = new AddonCommand(TownyCommandAddonAPI.CommandType.TOWN_SET, "capturepoint", this);

        TownyCommandAddonAPI.addSubCommand(capturePointCommand);
    }

    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        return Collections.emptyList();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player p) {
            try {
                set(p);
            } catch (TownyException e) {
                p.sendMessage(Component.translatable(
                    "alathranwars.error",
                    Argument.string("error", e.getMessage())));
            }
        } else {
            help(sender);
        }
        return true;
    }

    public void help(@NotNull CommandSender sender) {
        sender.sendMessage(Component.translatable("alathranwars.commands.town.capturepoint-usage"));
    }

    public void set(Player p) throws TownyException {
        checkPermOrThrow(p, PERM_TOWN);

        Town town = TownyAPI.getInstance().getTown(p);
        if (town == null)
            throw new TownyException("The specified town could not be found.");

        if (WarController.getInstance().isInAnySieges(town)) // TODO Check for raids
            throw new TownyException("The specified town is currently in battle.");

        final @Nullable Location oldLoc = ControlPoint.get(town);

        PreSetControlPointEvent preEvent = new PreSetControlPointEvent(town, oldLoc, p.getLocation(), p);
        if (!preEvent.callEvent())
            throw new TownyException("The capture point could not be set.");

        final Location loc = preEvent.getNewLocation();

        if (!town.getWorld().equals(loc.getWorld()))
            throw new TownyException("The capture point is not in the same world as the town.");

        if (!town.isInsideTown(loc))
            throw new TownyException("The capture point is not within the town.");

        ControlPoint.set(town, loc);

        new SetControlPointEvent(town, oldLoc, loc, p).callEvent();
    }
}
