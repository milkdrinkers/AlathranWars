package io.github.alathra.alathranwars.command;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandAPIPaper;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import io.github.alathra.alathranwars.conflict.battle.siege.Siege;
import io.github.alathra.alathranwars.conflict.battle.siege.SiegeImpl;
import io.github.alathra.alathranwars.conflict.war.War;
import io.github.alathra.alathranwars.conflict.war.WarController;
import io.github.alathra.alathranwars.conflict.war.side.Side;
import io.github.alathra.alathranwars.enums.battle.BattleVictoryReason;
import io.github.alathra.alathranwars.utility.BattleUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.translation.Argument;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

import static io.github.alathra.alathranwars.enums.CommandArgsSiege.ALL_SIEGES;
import static io.github.alathra.alathranwars.enums.CommandArgsSiege.IN_SIEGE;
import static io.github.alathra.alathranwars.enums.CommandArgsWar.ALL_WARS;
import static io.github.alathra.alathranwars.enums.CommandArgsWar.IN_WAR;

public class SiegeCommands {
    public SiegeCommands() {
        new CommandAPICommand("siege")
            .withSubcommands(
                commandStart(false),
                commandStop(false),
                commandAbandon(false),
                commandSurrender(false),
                commandList()
            )
            .executesPlayer((sender, args) -> {
                if (args.count() == 0)
                    throw CommandAPIPaper.failWithAdventureComponent(Component.translatable(
                        "alathranwars.commands.siege.invalid-args",
                        Argument.component("prefix", Component.translatable("alathranwars.prefix"))));
            })
            .register();
    }

    public static CommandAPICommand commandStart(boolean asAdmin) {
        return new CommandAPICommand("start")
//            .withPermission("AlathranWars.admin")
            .withArguments(
                CommandUtil.warWarArgument(
                    "war",
                    asAdmin,
                    asAdmin ? ALL_WARS : IN_WAR,
                    ""
                ),
                CommandUtil.customSiegeAttackableTownArgument(
                    "town",
                    "war",
                    false,
                    true
                ),
                new EntitySelectorArgument.OnePlayer("leader")
                    .setOptional(!asAdmin)
                    .withPermission("AlathranWars.admin")/*,
                new BooleanArgument("minutemen")
                    .setOptional(true)
                    .withPermission("AlathranWars.admin")*/
            )
            .executesPlayer((Player p, CommandArguments args) -> siegeStart(p, args, asAdmin));
    }

    public static CommandAPICommand commandStop(boolean asAdmin) {
        return new CommandAPICommand("stop")
            .withPermission("AlathranWars.admin")
            .withArguments(
                CommandUtil.siegeSiegeArgument("siege", asAdmin, asAdmin ? ALL_SIEGES : IN_SIEGE, "player")
            )
            .executesPlayer((Player p, CommandArguments args) -> siegeStop(p, args, asAdmin));
    }

    public static CommandAPICommand commandAbandon(boolean asAdmin) {
        return new CommandAPICommand("abandon")
            .withArguments(
                CommandUtil.siegeSiegeArgument("siege", asAdmin, asAdmin ? ALL_SIEGES : IN_SIEGE, "player")
            )
            .executesPlayer((Player p, CommandArguments args) -> siegeAbandon(p, args, asAdmin));
    }

    public static CommandAPICommand commandSurrender(boolean asAdmin) {
        return new CommandAPICommand("surrender")
            .withArguments(
                CommandUtil.siegeSiegeArgument("siege", asAdmin, asAdmin ? ALL_SIEGES : IN_SIEGE, "player")
            )
            .executesPlayer((Player p, CommandArguments args) -> siegeSurrender(p, args, asAdmin));
    }

    public static CommandAPICommand commandList() {
        return new CommandAPICommand("list")
            .executesPlayer(SiegeCommands::siegeList);
    }

    protected static void siegeStart(@NotNull Player sender, @NotNull CommandArguments args, boolean asAdmin) throws WrapperCommandSyntaxException {
        if (!(args.get("war") instanceof final @NotNull War war))
            throw CommandAPIPaper.failWithAdventureComponent(Component.translatable("alathranwars.commands.siege.start.no-war"));

        if (!(args.get("town") instanceof final @NotNull Town town))
            throw CommandAPIPaper.failWithAdventureComponent(Component.translatable("alathranwars.commands.siege.start.no-town"));

        final @NotNull Player siegeLeader = (Player) args.getOptional("leader").orElse(sender);

        // Player participance check
        @Nullable Side side = war.getPlayerSide(siegeLeader);
        if (side == null)
            throw CommandAPIPaper.failWithAdventureComponent(Component.translatable("alathranwars.commands.siege.start.not-in-war"));

        if (!war.isWarTime() && !asAdmin)
            throw CommandAPIPaper.failWithAdventureComponent(Component.translatable("alathranwars.commands.siege.start.not-war-time"));

        if (side.isSurrendered(siegeLeader))
            throw CommandAPIPaper.failWithAdventureComponent(Component.translatable("alathranwars.commands.siege.start.surrendered"));

        if (side.isSiegeGraceActive() && !asAdmin)
            throw CommandAPIPaper.failWithAdventureComponent(Component.translatable(
                "alathranwars.commands.siege.start.grace",
                Argument.numeric("time", side.getSiegeGraceCooldown().toMinutes())));

        // Attacking own side
        if (side.isOnSide(town) && !side.isSurrendered(town)) {
            if (asAdmin)
                throw CommandAPIPaper.failWithAdventureComponent(Component.translatable("alathranwars.commands.siege.start.own-side"));
            else
                siegeLeader.sendMessage(Component.translatable("alathranwars.commands.siege.start.own-town"));
            return;
        }

        final Location location = siegeLeader.getLocation();
        final Location townLocation = town.getSpawnOrNull();
        if (townLocation == null)
            throw CommandAPIPaper.failWithAdventureComponent(Component.translatable("alathranwars.commands.siege.start.no-town-spawn"));

        if (!location.getWorld().equals(townLocation.getWorld()))
            throw CommandAPIPaper.failWithAdventureComponent(Component.translatable("alathranwars.commands.siege.start.wrong-world"));

        if (location.distance(townLocation) >= SiegeImpl.BATTLEFIELD_START_MAX_RANGE && !asAdmin)
            throw CommandAPIPaper.failWithAdventureComponent(Component.translatable(
                "alathranwars.commands.siege.start.too-far",
                Argument.numeric("range", SiegeImpl.BATTLEFIELD_START_MAX_RANGE)));

        if (location.distance(townLocation) <= SiegeImpl.BATTLEFIELD_START_MIN_RANGE && !asAdmin)
            throw CommandAPIPaper.failWithAdventureComponent(Component.translatable(
                "alathranwars.commands.siege.start.too-close",
                Argument.numeric("range", SiegeImpl.BATTLEFIELD_START_MIN_RANGE)));

//        if ((Hook.getVaultHook().isEconomyLoaded() && Hook.getVaultHook().getEconomy().getBalance(siegeLeader) < Siege.SIEGE_VICTORY_MONEY) && !asAdmin)
//            throw CommandAPIPaper.failWithAdventureComponent(ColorParser.of("<red>You need to have $<amount> to start a siege!").with("amount", String.valueOf(Siege.SIEGE_VICTORY_MONEY)).build());

        BattleUtils.startSiege(siegeLeader, town, war, side);
    }

    private static void siegeStop(@NotNull Player sender, @NotNull CommandArguments args, boolean asAdmin) throws WrapperCommandSyntaxException {
        if (!(args.get("siege") instanceof final @NotNull Siege siege))
            throw CommandAPIPaper.failWithAdventureComponent(Component.translatable("alathranwars.commands.siege.invalid-siege"));

        siege.equalWin(BattleVictoryReason.ADMIN_COMMAND);
    }

    private static void siegeAbandon(Player p, @NotNull CommandArguments args, boolean asAdmin) throws WrapperCommandSyntaxException {
        if (!(args.get("siege") instanceof final @NotNull Siege siege))
            throw CommandAPIPaper.failWithAdventureComponent(Component.translatable("alathranwars.commands.siege.invalid-siege"));

        @NotNull OfflinePlayer siegeLeader = siege.getSiegeLeader();
        if (siegeLeader != p && !asAdmin)
            throw CommandAPIPaper.failWithAdventureComponent(Component.translatable("alathranwars.commands.siege.abandon.not-leader"));

        siege.defendersWin(BattleVictoryReason.OPPONENT_RETREAT);
    }

    private static void siegeSurrender(Player p, @NotNull CommandArguments args, boolean asAdmin) throws WrapperCommandSyntaxException {
        if (!(args.get("siege") instanceof final @NotNull Siege siege))
            throw CommandAPIPaper.failWithAdventureComponent(Component.translatable("alathranwars.commands.siege.invalid-siege"));

        Town town = siege.getTown();

        @Nullable Resident res = TownyAPI.getInstance().getResident(p);
        if (res == null)
            throw CommandAPIPaper.failWithAdventureComponent(Component.translatable("alathranwars.commands.siege.resident-invalid"));

        final boolean canKingSurrender = (res.hasNation() && town.hasNation() && res.getNationOrNull().equals(town.getNationOrNull()) && res.isKing());
        final boolean canMayorSurrender = (res.hasTown() && res.getTownOrNull().equals(town) && res.isMayor());

        if (!asAdmin && !canKingSurrender && !canMayorSurrender)
            throw CommandAPIPaper.failWithAdventureComponent(Component.translatable("alathranwars.commands.siege.surrender.no-permission"));

        siege.attackersWin(BattleVictoryReason.OPPONENT_RETREAT);
    }

    private static void siegeList(@NotNull Player sender, CommandArguments args) throws WrapperCommandSyntaxException {
        Set<Siege> sieges = WarController.getInstance().getSieges();

        if (sieges.isEmpty()) {
            sender.sendMessage(Component.translatable("alathranwars.commands.siege.list.empty"));
            return;
        }

        final TextComponent.Builder msg = Component.text();

        for (Siege siege : sieges) {
            msg.append(Component.translatable(
                "alathranwars.commands.siege.list.entry",
                Argument.string("name", siege.getName()),
                Argument.numeric("progress", Math.round(siege.getSiegeProgressPercentage() * 100)),
                Argument.numeric("remaining", Duration.between(Instant.now(), siege.getEndTime()).toMinutesPart())
            ));
        }

        sender.sendMessage(msg.build());
    }
}
