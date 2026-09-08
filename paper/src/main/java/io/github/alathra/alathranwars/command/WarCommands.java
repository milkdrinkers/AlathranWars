package io.github.alathra.alathranwars.command;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandAPIPaper;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import io.github.alathra.alathranwars.api.AlathranWarsAPIImpl;
import io.github.alathra.alathranwars.conflict.war.War;
import io.github.alathra.alathranwars.conflict.war.WarController;
import io.github.alathra.alathranwars.conflict.war.WarImpl;
import io.github.alathra.alathranwars.conflict.war.side.Side;
import io.github.alathra.alathranwars.conflict.war.side.SideCreationException;
import io.github.alathra.alathranwars.hook.NameColorHandler;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static io.github.alathra.alathranwars.enums.CommandArgsWar.*;

public class WarCommands {
    public WarCommands() {
        new CommandAPICommand("war")
            .withSubcommands(
                commandCreate(false),
                commandDelete(false),
                commandJoin(false),
                commandSurrender(false),
                commandList(),
                commandInfo()
            )
            .executesPlayer((sender, args) -> {
                if (args.count() == 0)
                    throw CommandAPIPaper.failWithAdventureComponent(Component.translatable(
                        "alathranwars.commands.war.invalid-args",
                        Argument.component("prefix", Component.translatable("alathranwars.prefix"))));
            })
            .register();
    }

    public static CommandAPICommand commandCreate(boolean asAdmin) {
        return new CommandAPICommand("create")
            .withPermission("AlathranWars.admin")
            .withArguments(
                CommandUtil.customTownAndNationArgument("side1", "war", false),
                CommandUtil.customTownAndNationArgument("side2", "war", false),
                new BooleanArgument("event"),
                new GreedyStringArgument("warlabel")
                    .replaceSuggestions(
                        ArgumentSuggestions.stringCollection(info -> List.of(
                            "Nations War of Conquest",
                            "Nations War for Survival",
                            "Nations War of Liberation"
                        ))
                    )
            )
            .executesPlayer(WarCommands::warCreate);
    }

    public static CommandAPICommand commandDelete(boolean asAdmin) {
        return new CommandAPICommand("delete")
            .withPermission("AlathranWars.admin")
            .withArguments(
                CommandUtil.warWarArgument("war", asAdmin, ALL_WARS, "")
            )
            .executesPlayer(WarCommands::warDelete);
    }

    public static CommandAPICommand commandJoin(boolean asAdmin) {
        return new CommandAPICommand("join")
            .withPermission("AlathranWars.war.join")
            .withArguments(
                CommandUtil.warWarArgument("war", asAdmin, asAdmin ? ALL_WARS : OUT_WAR, ""),
                CommandUtil.warSideCreateArgument("side", "war", asAdmin, !asAdmin, ""),
                CommandUtil.warTargetCreateArgument("target", "war", asAdmin).setOptional(!asAdmin)
            )
            .executesPlayer((Player p, CommandArguments args) -> warJoin(p, args, asAdmin));
    }

    public static CommandAPICommand commandJoinNear() {
        return new CommandAPICommand("joinnear")
            .withPermission("AlathranWars.admin")
            .withArguments(
                CommandUtil.warWarArgument("war", true, ALL_WARS, ""),
                CommandUtil.warSideCreateArgument("side", "war", true, false, "")
            )
            .executesPlayer((Player p, CommandArguments args) -> warJoinNear(p, args, true));
    }

    public static CommandAPICommand commandSurrender(boolean asAdmin) {
        return new CommandAPICommand("surrender")
            .withArguments(
                CommandUtil.warWarArgument("war", asAdmin, asAdmin ? ALL_WARS : IN_WAR, "player"),
                new EntitySelectorArgument.OnePlayer("player")
                    .setOptional(true)
                    .withPermission("AlathranWars.admin")
            )
            .executesPlayer((Player p, CommandArguments args) -> warSurrender(p, args, false));
    }

    public static CommandAPICommand commandList() {
        return new CommandAPICommand("list")
            .executesPlayer(WarCommands::warList);
    }

    public static CommandAPICommand commandInfo() {
        return new CommandAPICommand("info")
            .withArguments(
                CommandUtil.warWarArgument("war", false, ALL_WARS, "player")
            )
            .executesPlayer(WarCommands::warInfo);
    }

    public static CommandAPICommand commandInfoSide() {
        return new CommandAPICommand("infoside")
            .withArguments(
                CommandUtil.warWarArgument("war", false, ALL_WARS, "player"),
                CommandUtil.warSideArgument("side", "war", true, false, "")
            )
            .executesPlayer(WarCommands::warInfoSide);
    }

    public static CommandAPICommand commandKick() {
        return new CommandAPICommand("kick")
            .withPermission("AlathranWars.admin")
            .withArguments(
                CommandUtil.warWarArgument("war", true, ALL_WARS, "player"),
                CommandUtil.playerOrTownOrNationInWar("target", "war")
            )
            .executesPlayer(WarCommands::warKick);
    }

    protected static void warCreate(CommandSender p, @NotNull CommandArguments args) throws WrapperCommandSyntaxException {
        if (!(args.get("warlabel") instanceof final String argLabel))
            throw CommandAPIPaper.failWithAdventureComponent(Component.translatable(
                "alathranwars.commands.war.create.no-label",
                Argument.component("prefix", Component.translatable("alathranwars.prefix"))));

        if (Objects.equals(args.get("side1"), args.get("side2")))
            throw CommandAPIPaper.failWithAdventureComponent(Component.translatable(
                "alathranwars.commands.war.create.self-declare",
                Argument.component("prefix", Component.translatable("alathranwars.prefix"))));

        final boolean event = (boolean) args.getOrDefault("event", false);

        final UUID uuid1 = (UUID) args.get("side1");
        final UUID uuid2 = (UUID) args.get("side2");

        // Create new war depending on what argument was specified
        @Nullable Town town1 = TownyAPI.getInstance().getTown(uuid1);
        @Nullable Town town2 = TownyAPI.getInstance().getTown(uuid2);

        // Automatically escalate to nation level if possible
        @Nullable Nation nation1 = town1 == null ? TownyAPI.getInstance().getNation(uuid1) : town1.getNationOrNull();
        @Nullable Nation nation2 = town2 == null ? TownyAPI.getInstance().getNation(uuid2) : town2.getNationOrNull();

        // Implement permission checking here if this is ever going to be exposed to players

        // Create war depending on what args have been passed, attempt to create nation wars first
        WarImpl.Builder builder = WarImpl.builder()
            .setUuid(UUID.randomUUID())
            .setLabel(argLabel)
            .setEvent(event);

        if (nation1 != null) {
            if (nation2 != null) { // Nation vs Nation
                // Allow civil wars
                if (nation2.getUUID() != nation1.getUUID()) {
                    builder
                        .setAggressor(nation1)
                        .setVictim(nation2);
                } else {
                    builder
                        .setAggressor(town1)
                        .setVictim(town2);
                }
            } else if (town2 != null) { // Nation vs Town
                builder
                    .setAggressor(nation1)
                    .setVictim(town2);
            }
        } else if (town1 != null) {
            if (town2 != null) { // Town vs Town
                builder
                    .setAggressor(town1)
                    .setVictim(town2);
            } else if (nation2 != null) { // Nation vs Town
                builder
                    .setAggressor(town1)
                    .setVictim(nation2);
            }
        }

        try {
            builder.create();
        } catch (SideCreationException e) {
            throw CommandAPIPaper.failWithAdventureComponent(Component.translatable(
                "alathranwars.error",
                Argument.string("error", e.getMessage())));
        }
    }

    private static void warDelete(Player p, @NotNull CommandArguments args) throws WrapperCommandSyntaxException {
        War war = (War) args.get("war");
        if (war == null) return;

        war.draw();
    }

    protected static void warJoin(Player p, @NotNull CommandArguments args, boolean asAdmin) throws WrapperCommandSyntaxException {
        War war = (War) args.get("war");
        if (war == null) return;

        if (!(args.get("side") instanceof final Side side))
            throw CommandAPIPaper.failWithAdventureComponent(Component.translatable(
                "alathranwars.commands.war.no-side",
                Argument.component("prefix", Component.translatable("alathranwars.prefix"))));

        if (!(args.getOptional("target").orElse(new CommandUtil.TownyIdentifierArgument(CommandUtil.TownyIdentifierArgument.TownyIdentifierArgumentType.PLAYER, p.getUniqueId())) instanceof CommandUtil.TownyIdentifierArgument target))
            throw CommandAPIPaper.failWithAdventureComponent(Component.translatable("alathranwars.commands.war.invalid-target"));

        CommandUtil.TownyIdentifierArgument.TownyIdentifierArgumentType targetType = target.getType();
        UUID targetUuid = target.getUUID();

        @Nullable Resident res = TownyAPI.getInstance().getResident(p);
        if (res == null)
            throw CommandAPIPaper.failWithAdventureComponent(Component.translatable("alathranwars.commands.war.resident-invalid"));

        switch (targetType) {
            case NATION -> {
                final Nation nation = TownyAPI.getInstance().getNation(targetUuid);

                if (nation == null)
                    throw CommandAPIPaper.failWithAdventureComponent(Component.translatable("alathranwars.commands.war.join.no-nation"));

                if (war.isInWar(nation))
                    throw CommandAPIPaper.failWithAdventureComponent(Component.translatable("alathranwars.commands.war.join.nation-already-in"));

                final boolean canKingJoin = (res.hasNation() && res.getNationOrNull().equals(nation) && res.isKing() && nation.isKing(res));

                if (!asAdmin && !canKingJoin)
                    throw CommandAPIPaper.failWithAdventureComponent(Component.translatable("alathranwars.commands.war.join.nation-rank"));

                if (!war.isEventWar()) {
                    if (side.isAttacker() && AlathranWarsAPIImpl.getInstance().hasAttackCooldown(nation)) {
                        throw CommandAPIPaper.failWithAdventureComponent(Component.translatable("alathranwars.commands.war.join.nation-cooldown-attack"));
                    } else if (side.isDefender() && AlathranWarsAPIImpl.getInstance().hasDefenseCooldown(nation)) {
                        throw CommandAPIPaper.failWithAdventureComponent(Component.translatable("alathranwars.commands.war.join.nation-cooldown-defense"));
                    }
                }

                if (!war.isEventWar()) {
                    if (side.isAttacker()) {
                        AlathranWarsAPIImpl.getInstance().setAttackCooldown(nation);
                    } else {
                        AlathranWarsAPIImpl.getInstance().setDefenseCooldown(nation);
                    }
                }
                side.add(nation);
                nation.getResidents().stream().filter(Resident::isOnline).map(Resident::getPlayer).toList().forEach(player -> NameColorHandler.getInstance().calculatePlayerColors(player));
                Bukkit.broadcast(Component.translatable(
                    "alathranwars.commands.war.join.broadcast-nation",
                    Argument.component("prefix", Component.translatable("alathranwars.prefix")),
                    Argument.string("nation", nation.getName()),
                    Argument.string("war", war.getLabel()),
                    Argument.string("side", side.getName())
                ));
                final Title warTitle = Title.title(
                    Component.translatable("alathranwars.war.banner"),
                    Component.translatable(
                        "alathranwars.commands.war.join.subtitle",
                        Argument.string("war", war.getLabel())),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(500))
                );
                final Sound warSound = Sound.sound(Key.key("entity.wither.spawn"), Sound.Source.VOICE, 0.5f, 1.0F);

                nation.getResidents().stream().filter(Resident::isOnline).map(Resident::getPlayer).toList().forEach(player -> {
                    player.showTitle(warTitle);
                    player.playSound(warSound);
                });
            }
            case TOWN -> {
                Town town = TownyAPI.getInstance().getTown(targetUuid);

                if (town == null)
                    throw CommandAPIPaper.failWithAdventureComponent(Component.translatable("alathranwars.commands.war.join.no-town"));

                if (war.isInWar(town))
                    throw CommandAPIPaper.failWithAdventureComponent(Component.translatable("alathranwars.commands.war.join.town-already-in"));

                final boolean canMayorJoin = (res.hasTown() && res.getTownOrNull().equals(town) && res.isMayor() && town.isMayor(res));

                if (!asAdmin && !canMayorJoin)
                    throw CommandAPIPaper.failWithAdventureComponent(Component.translatable("alathranwars.commands.war.join.town-rank"));

                if (!war.isEventWar()) {
                    if (side.isAttacker() && AlathranWarsAPIImpl.getInstance().hasAttackCooldown(town)) {
                        throw CommandAPIPaper.failWithAdventureComponent(Component.translatable("alathranwars.commands.war.join.town-cooldown-attack"));
                    } else if (side.isDefender() && AlathranWarsAPIImpl.getInstance().hasDefenseCooldown(town)) {
                        throw CommandAPIPaper.failWithAdventureComponent(Component.translatable("alathranwars.commands.war.join.town-cooldown-defense"));
                    }
                }

                if (!war.isEventWar()) {
                    if (side.isAttacker()) {
                        AlathranWarsAPIImpl.getInstance().setAttackCooldown(town);
                    } else {
                        AlathranWarsAPIImpl.getInstance().setDefenseCooldown(town);
                    }
                }
                side.add(town);
                town.getResidents().stream().filter(Resident::isOnline).map(Resident::getPlayer).toList().forEach(player -> NameColorHandler.getInstance().getPlayerNameColor(player));
                Bukkit.broadcast(Component.translatable(
                    "alathranwars.commands.war.join.broadcast-town",
                    Argument.component("prefix", Component.translatable("alathranwars.prefix")),
                    Argument.string("town", town.getName()),
                    Argument.string("war", war.getLabel()),
                    Argument.string("side", side.getName())
                ));
                final Title warTitle = Title.title(
                    Component.translatable("alathranwars.war.banner"),
                    Component.translatable(
                        "alathranwars.commands.war.join.subtitle",
                        Argument.string("war", war.getLabel())),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(500))
                );
                final Sound warSound = Sound.sound(Key.key("entity.wither.spawn"), Sound.Source.VOICE, 0.5f, 1.0F);

                town.getResidents().stream().filter(Resident::isOnline).map(Resident::getPlayer).toList().forEach(player -> {
                    player.showTitle(warTitle);
                    player.playSound(warSound);
                });
            }
            case PLAYER -> {
                Player player = Bukkit.getPlayer(targetUuid);

                if (player == null)
                    throw CommandAPIPaper.failWithAdventureComponent(Component.translatable("alathranwars.commands.war.join.no-player"));

                if (war.isInWar(player))
                    throw CommandAPIPaper.failWithAdventureComponent(Component.translatable("alathranwars.commands.war.join.player-already-in"));

                if (!asAdmin)
                    throw CommandAPIPaper.failWithAdventureComponent(Component.translatable("alathranwars.commands.war.join.player-not-allowed"));

                if (!war.isEventWar()) {
                    if (side.isAttacker() && AlathranWarsAPIImpl.getInstance().hasAttackCooldown(player)) {
                        throw CommandAPIPaper.failWithAdventureComponent(Component.translatable("alathranwars.commands.war.join.player-cooldown-attack"));
                    } else if (side.isDefender() && AlathranWarsAPIImpl.getInstance().hasDefenseCooldown(player)) {
                        throw CommandAPIPaper.failWithAdventureComponent(Component.translatable("alathranwars.commands.war.join.player-cooldown-defense"));
                    }
                }

                if (!war.isEventWar()) {
                    if (side.isAttacker()) {
                        AlathranWarsAPIImpl.getInstance().setAttackCooldown(player);
                    } else {
                        AlathranWarsAPIImpl.getInstance().setDefenseCooldown(player);
                    }
                }
                player.sendMessage(Component.translatable(
                    "alathranwars.commands.war.join.success",
                    Argument.component("prefix", Component.translatable("alathranwars.prefix"))));
                side.add(player);
                NameColorHandler.getInstance().calculatePlayerColors(player);
                final Title warTitle = Title.title(
                    Component.translatable("alathranwars.war.banner"),
                    Component.translatable(
                        "alathranwars.commands.war.join.subtitle",
                        Argument.string("war", war.getLabel())),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(500))
                );
                final Sound warSound = Sound.sound(Key.key("entity.wither.spawn"), Sound.Source.VOICE, 0.5f, 1.0F);

                player.showTitle(warTitle);
                player.playSound(warSound);
            }
            default -> {
                throw CommandAPIPaper.failWithAdventureComponent(Component.translatable("alathranwars.commands.war.join.target-invalid"));
            }
        }

        /*for (@NotNull Siege siege : war.getSieges()) {
            if (siege.getAttackerSide().equals(side)) {
                siege.addPlayer(argPlayer, BattleSide.ATTACKER);
            } else {
                siege.addPlayer(argPlayer, BattleSide.DEFENDER);
            }
        }*/
    }

    protected static void warJoinNear(@NotNull Player p, @NotNull CommandArguments args, boolean asAdmin) throws WrapperCommandSyntaxException {
        War war = (War) args.get("war");
        if (war == null) return;

        if (!(args.get("side") instanceof final Side side))
            throw CommandAPIPaper.failWithAdventureComponent(Component.translatable(
                "alathranwars.commands.war.no-side",
                Argument.component("prefix", Component.translatable("alathranwars.prefix"))));

        final Location location = p.getLocation();

        for (Player targetPlayer : Bukkit.getOnlinePlayers()) {
            if (p.equals(targetPlayer)) continue;
            final Location targetLocation = targetPlayer.getLocation();
            if (!location.getWorld().equals(targetLocation.getWorld())) continue;
            if (location.distance(targetLocation) > 25) continue;
            if (war.isInWar(targetPlayer)) continue;

            if (!war.isEventWar()) {
                if (side.isAttacker() && AlathranWarsAPIImpl.getInstance().hasAttackCooldown(targetPlayer)) {
                    targetPlayer.sendMessage(Component.translatable("alathranwars.commands.war.joinnear.self-cooldown-attack"));
                    p.sendMessage(Component.translatable(
                        "alathranwars.commands.war.joinnear.target-cooldown-attack",
                        Argument.component("target", targetPlayer.name())));
                    continue;
                } else if (side.isDefender() && AlathranWarsAPIImpl.getInstance().hasDefenseCooldown(targetPlayer)) {
                    targetPlayer.sendMessage(Component.translatable("alathranwars.commands.war.joinnear.self-cooldown-defense"));
                    p.sendMessage(Component.translatable(
                        "alathranwars.commands.war.joinnear.target-cooldown-defense",
                        Argument.component("target", targetPlayer.name())));
                    continue;
                }
            }

            if (!war.isEventWar()) {
                if (side.isAttacker()) {
                    AlathranWarsAPIImpl.getInstance().setAttackCooldown(targetPlayer);
                } else {
                    AlathranWarsAPIImpl.getInstance().setDefenseCooldown(targetPlayer);
                }
            }

            targetPlayer.sendMessage(Component.translatable(
                "alathranwars.commands.war.join.success",
                Argument.component("prefix", Component.translatable("alathranwars.prefix"))));
            side.add(targetPlayer);
            NameColorHandler.getInstance().calculatePlayerColors(targetPlayer);
            final Title warTitle = Title.title(
                Component.translatable("alathranwars.war.banner"),
                Component.translatable(
                    "alathranwars.commands.war.join.subtitle",
                    Argument.string("war", war.getLabel())),
                Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(500))
            );
            final Sound warSound = Sound.sound(Key.key("entity.wither.spawn"), Sound.Source.VOICE, 0.5f, 1.0F);

            targetPlayer.showTitle(warTitle);
            targetPlayer.playSound(warSound);
        }
    }

    protected static void warSurrender(@NotNull Player p, @NotNull CommandArguments args, boolean asAdmin) throws WrapperCommandSyntaxException {
        War war = (War) args.get("war");
        if (war == null) return;

        if (!(args.getOptional("player").orElse(p) instanceof Player argPlayer))
            throw CommandAPIPaper.failWithAdventureComponent(Component.translatable("alathranwars.commands.war.surrender.no-player"));

        // Towny Resident Object
        @Nullable Resident res = TownyAPI.getInstance().getResident(argPlayer);
        if (res == null)
            throw CommandAPIPaper.failWithAdventureComponent(Component.translatable("alathranwars.commands.war.resident-invalid"));

        // Town check
        @Nullable Town town = res.getTownOrNull();
        if (town == null)
            throw CommandAPIPaper.failWithAdventureComponent(Component.translatable("alathranwars.commands.war.surrender.no-town"));

        @Nullable Side side = war.getSide(town);
        if (side == null)
            throw CommandAPIPaper.failWithAdventureComponent(Component.translatable("alathranwars.commands.war.surrender.not-in-war"));

        final @Nullable Nation resNation = res.getNationOrNull();
        final @Nullable Town resTown = res.getTownOrNull();

        if (resNation != null && (p.hasPermission("AlathranWars.nationsurrender") || res.isKing() || asAdmin)) {
            // Has nation surrender permission
            argPlayer.sendMessage(Component.translatable(
                "alathranwars.commands.war.surrender.success",
                Argument.component("prefix", Component.translatable("alathranwars.prefix")),
                Argument.string("name", resNation.getName())));
            Bukkit.broadcast(Component.translatable(
                "alathranwars.commands.war.surrender.broadcast-nation",
                Argument.component("prefix", Component.translatable("alathranwars.prefix")),
                Argument.string("nation", resNation.getName())));
            war.surrender(resNation);
        } else if (resTown == null) {
            // Cannot surrender nation involvement
            throw CommandAPIPaper.failWithAdventureComponent(Component.translatable("alathranwars.commands.war.surrender.no-nation-permission"));
        }

        if (resTown != null && (p.hasPermission("AlathranWars.townsurrender") || res.isMayor() || asAdmin)) {
            // Is in indepdenent town & has surrender perms
            argPlayer.sendMessage(Component.translatable(
                "alathranwars.commands.war.surrender.success",
                Argument.component("prefix", Component.translatable("alathranwars.prefix")),
                Argument.string("name", resTown.getName())));
            Bukkit.broadcast(Component.translatable(
                "alathranwars.commands.war.surrender.broadcast-town",
                Argument.component("prefix", Component.translatable("alathranwars.prefix")),
                Argument.string("town", resTown.getName())));
            war.cancelSieges(resTown);
            war.surrender(resTown);
        } else {
            // No perms
            throw CommandAPIPaper.failWithAdventureComponent(Component.translatable("alathranwars.commands.war.surrender.no-town-permission"));
        }
    }

    protected static void warKick(@NotNull Player p, @NotNull CommandArguments args) throws WrapperCommandSyntaxException {
        War war = (War) args.get("war");
        if (war == null) return;

        if (!(args.get("target") instanceof CommandUtil.TownyIdentifierArgument target))
            throw CommandAPIPaper.failWithAdventureComponent(Component.translatable("alathranwars.commands.war.invalid-target"));

        CommandUtil.TownyIdentifierArgument.TownyIdentifierArgumentType targetType = target.getType();
        UUID targetUuid = target.getUUID();

        switch (targetType) {
            case PLAYER -> {
                OfflinePlayer player = Bukkit.getOfflinePlayer(targetUuid);
                Side sidePlayer = war.getPlayerSide(targetUuid);
                sidePlayer.kick(player.getUniqueId());
                p.sendMessage(Component.translatable("alathranwars.commands.war.kick.success"));
                if (player.getPlayer() != null)
                    NameColorHandler.getInstance().calculatePlayerColors(player.getPlayer());
            }
            case TOWN -> {
                Town town = TownyAPI.getInstance().getTown(targetUuid);
                Side sideTown = war.getSide(town);
                sideTown.kick(town);
                p.sendMessage(Component.translatable("alathranwars.commands.war.kick.success"));
                town.getResidents().stream().filter(Resident::isOnline).map(Resident::getPlayer).forEach(player1 -> NameColorHandler.getInstance().calculatePlayerColors(player1));
            }
            case NATION -> {
                Nation nation = TownyAPI.getInstance().getNation(targetUuid);
                Side sideNation = war.getSide(nation);
                sideNation.kick(nation);
                p.sendMessage(Component.translatable("alathranwars.commands.war.kick.success"));
                nation.getResidents().stream().filter(Resident::isOnline).map(Resident::getPlayer).forEach(player1 -> NameColorHandler.getInstance().calculatePlayerColors(player1));
            }
            default ->
                throw CommandAPIPaper.failWithAdventureComponent(Component.translatable("alathranwars.commands.war.kick.target-not-in-war"));
        }
    }

    private static void warList(@NotNull Player p, CommandArguments args) throws WrapperCommandSyntaxException {
        Set<War> wars = WarController.getInstance().getWars();

        if (wars.isEmpty()) {
            p.sendMessage(Component.translatable("alathranwars.commands.war.list.empty"));
            return;
        }

        final TextComponent.Builder msg = Component.text();

        for (War war : wars) {
            msg.append(Component.translatable(
                "alathranwars.commands.war.list.entry",
                Argument.string("label", war.getLabel()),
                Argument.string("side1", war.getSide1().getName()),
                Argument.string("side2", war.getSide2().getName()),
                Argument.numeric("score1", war.getSide1().getScore()),
                Argument.numeric("score2", war.getSide2().getScore())
            ));
        }

        p.sendMessage(msg.build());
    }

    private static void warInfo(Player p, CommandArguments args) throws WrapperCommandSyntaxException {
        War war = (War) args.get("war");
        if (war == null) return;

        final TextComponent.Builder msg = Component.text();

        for (Side side : war.getSides()) {
            msg.append(Component.translatable(
                "alathranwars.commands.war.info.entry",
                Argument.string("name", side.getName()),
                Argument.numeric("score", side.getScore()),
                Argument.numeric("nations", side.getNations().size()),
                Argument.numeric("nations_all", side.getNationsAll().size()),
                Argument.numeric("towns", side.getTowns().size()),
                Argument.numeric("towns_all", side.getTownsAll().size()),
                Argument.numeric("players", side.getPlayers().size()),
                Argument.numeric("players_all", side.getPlayersAll().size())
            ));
        }

        p.sendMessage(msg.build());
    }

    private static void warInfoSide(Player p, CommandArguments args) throws WrapperCommandSyntaxException {
        War war = (War) args.get("war");
        if (war == null) return;

        if (!(args.get("side") instanceof final Side side))
            throw CommandAPIPaper.failWithAdventureComponent(Component.translatable(
                "alathranwars.commands.war.no-side",
                Argument.component("prefix", Component.translatable("alathranwars.prefix"))));


        final TextComponent.Builder msg = Component.text();

        msg.append(Component.translatable("alathranwars.commands.war.infoside.nations-header"));
        for (Nation nation : side.getNations()) {
            msg.append(Component.translatable(
                "alathranwars.commands.war.infoside.entry",
                Argument.string("name", nation.getName())));
        }
        msg.append(Component.translatable("alathranwars.commands.war.infoside.towns-header"));
        for (Town town : side.getTowns()) {
            msg.append(Component.translatable(
                "alathranwars.commands.war.infoside.entry",
                Argument.string("name", town.getName())));
        }
        msg.append(Component.newline());

        p.sendMessage(msg.build());
    }
}
