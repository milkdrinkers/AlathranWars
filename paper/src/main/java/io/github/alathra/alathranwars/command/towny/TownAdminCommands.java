package io.github.alathra.alathranwars.command.towny;

import com.palmergames.bukkit.towny.TownyCommandAddonAPI;
import com.palmergames.bukkit.towny.command.BaseCommand;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.AddonCommand;
import com.palmergames.bukkit.towny.object.Town;
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

class TownAdminCommands extends BaseCommand implements TabExecutor {
    private static final String PERM_ADMIN = "alathranwars.command.admin.capturepoint";

    public TownAdminCommands() {
        AddonCommand adminCapturePointCommand = new AddonCommand(TownyCommandAddonAPI.CommandType.TOWNYADMIN_TOWN, "capturepoint", this);

        TownyCommandAddonAPI.addSubCommand(adminCapturePointCommand);
    }

    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        return Collections.emptyList();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 0 && sender instanceof Player p) {
            try {
                set(p, args);
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
        sender.sendMessage(Component.translatable("alathranwars.commands.town.capturepoint-admin-usage"));
    }

    public void set(Player p, String[] args) throws TownyException {
        checkPermOrThrow(p, PERM_ADMIN);

        Town town = getTownOrThrow(args[0]);

        final @Nullable Location oldLoc = ControlPoint.get(town);

        PreSetControlPointEvent preEvent = new PreSetControlPointEvent(town, oldLoc, p.getLocation(), p);
        if (!preEvent.callEvent())
            throw new TownyException("The capture point could not be set.");

        final Location loc = preEvent.getNewLocation();

        if (!town.getWorld().equals(loc.getWorld()))
            throw new TownyException("The capture point is not in the same world as the town.");

        ControlPoint.set(town, loc);

        new SetControlPointEvent(town, oldLoc, loc, p).callEvent();
    }
}
