package io.github.alathra.alathranwars.conflict.war;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Government;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import dev.jorel.commandapi.CommandAPIPaper;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import io.github.alathra.alathranwars.AbstractAlathranWars;
import io.github.alathra.alathranwars.AlathranWars;
import io.github.alathra.alathranwars.api.AlathranWarsAPIImpl;
import io.github.alathra.alathranwars.conflict.Conflict;
import io.github.alathra.alathranwars.conflict.Occupation;
import io.github.alathra.alathranwars.conflict.battle.raid.Raid;
import io.github.alathra.alathranwars.conflict.battle.siege.Siege;
import io.github.alathra.alathranwars.conflict.persistence.WarQueries;
import io.github.alathra.alathranwars.conflict.war.side.Side;
import io.github.alathra.alathranwars.conflict.war.side.SideCreationException;
import io.github.alathra.alathranwars.conflict.war.side.SideImpl;
import io.github.alathra.alathranwars.enums.ConflictType;
import io.github.alathra.alathranwars.enums.WarDeleteReason;
import io.github.alathra.alathranwars.enums.battle.BattleSide;
import io.github.alathra.alathranwars.enums.battle.BattleTeam;
import io.github.alathra.alathranwars.enums.battle.BattleVictoryReason;
import io.github.alathra.alathranwars.event.PreWarCreateEvent;
import io.github.alathra.alathranwars.event.PreWarDeleteEvent;
import io.github.alathra.alathranwars.event.WarCreateEvent;
import io.github.alathra.alathranwars.event.WarDeleteEvent;
import io.github.alathra.alathranwars.hook.Hook;
import io.github.alathra.alathranwars.hook.NameColorHandler;
import io.github.alathra.alathranwars.utility.Cfg;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The type New war.
 */
public class WarImpl extends Conflict implements War {
    private final String name; // Used in commands and such, like "TidalHaven.vs.Meme"
    private final String label;
    private final ConflictType conflictType = ConflictType.WAR;
    private boolean event;

    private final SideImpl side1;
    private final SideImpl side2;
    private final SideImpl attacker; // Reference variable to side1 or side2
    private final SideImpl defender; // Reference variable to side1 or side2

    private Set<Siege> sieges = new HashSet<>();
    private Set<Raid> raids = new HashSet<>();

    private final WarCapturePointManager capturePointManager;
    private final WarTimeManager warTimeManager;

    /**
     * Instantiates a existing War.
     *
     * @param uuid   the uuid
     * @param name   the name
     * @param label  the label
     * @param side1  the side 1
     * @param side2  the side 2
     * @param sieges the sieges
     * @param raids  the raids
     * @param event  the event
     */
    @ApiStatus.Internal
    WarImpl(
        UUID uuid,
        String name,
        String label,
        SideImpl side1,
        SideImpl side2,
        Set<Siege> sieges,
        Set<Raid> raids,
        boolean event,
        @Nullable Instant scheduledWarTime
    ) {
        super(uuid);
        this.name = name;
        this.label = label;

        this.side1 = side1;
        this.side2 = side2;

        this.attacker = side1.getSide().equals(BattleSide.ATTACKER) ? this.side1 : this.side2;
        this.defender = side1.getSide().equals(BattleSide.DEFENDER) ? this.side1 : this.side2;

        this.sieges = sieges;
        this.raids = raids;
        this.event = event;

        this.capturePointManager = new WarCapturePointManager(this);
        this.warTimeManager = new WarTimeManager(this, scheduledWarTime); // The time that this war is scheduled to start, null if not scheduled

        resume();
    }

    /**
     * Instantiates a new War.
     *
     * @param uuid      the uuid
     * @param label     the label
     * @param aggressor the aggressor
     * @param victim    the victim
     * @param event     the event
     * @throws SideCreationException         the side creation exception
     * @throws WrapperCommandSyntaxException the wrapper command syntax exception
     */
    @ApiStatus.Internal
    WarImpl(
        UUID uuid,
        String label,
        Government aggressor,
        Government victim,
        boolean event,
        @Nullable Instant scheduledWarTime
    ) throws SideCreationException, WrapperCommandSyntaxException {
        super(uuid);
        this.label = label;

        this.name = "%s.vs.%s".formatted(aggressor.getName(), victim.getName());
        if (WarController.getInstance().getWar(this.name) != null)
            throw CommandAPIPaper.failWithAdventureComponent(Component.translatable(
                "alathranwars.war.duplicate-name",
                Argument.component("prefix", Component.translatable("alathranwars.prefix"))));

        this.side1 = SideImpl.builder()
            .setWarUUID(getUUID())
            .setUuid(UUID.randomUUID())
            .setLeader(aggressor)
            .setSide(BattleSide.ATTACKER)
            .setTeam(BattleTeam.SIDE_1)
            .build();
        this.side2 = SideImpl.builder()
            .setWarUUID(getUUID())
            .setUuid(UUID.randomUUID())
            .setLeader(victim)
            .setSide(BattleSide.DEFENDER)
            .setTeam(BattleTeam.SIDE_2)
            .build();

        this.attacker = side1.getSide().equals(BattleSide.ATTACKER) ? this.side1 : this.side2;
        this.defender = side1.getSide().equals(BattleSide.DEFENDER) ? this.side1 : this.side2;
        this.event = event;
        this.capturePointManager = new WarCapturePointManager(this);
        this.warTimeManager = new WarTimeManager(this, scheduledWarTime);

        if (!new PreWarCreateEvent(this).callEvent())
            return;

        // TODO All logic below this point should be separated out
        WarController.getInstance().addWar(this);

        start();
        if (!isEventWar()) {
            attacker.getNations().forEach(n -> AlathranWarsAPIImpl.getInstance().setAttackCooldown(n));
            attacker.getTowns().forEach(t -> AlathranWarsAPIImpl.getInstance().setAttackCooldown(t));
            attacker.getPlayers().forEach(p -> AlathranWarsAPIImpl.getInstance().setAttackCooldown(TownyAPI.getInstance().getResident(p.getUniqueId())));
            defender.getNations().forEach(n -> AlathranWarsAPIImpl.getInstance().setDefenseCooldown(n));
            defender.getTowns().forEach(t -> AlathranWarsAPIImpl.getInstance().setDefenseCooldown(t));
            defender.getPlayers().forEach(p -> AlathranWarsAPIImpl.getInstance().setDefenseCooldown(TownyAPI.getInstance().getResident(p.getUniqueId())));
        }

        new WarCreateEvent(this).callEvent();
    }

    @Override
    public void onLoad(AbstractAlathranWars plugin) {
        attacker.onLoad(plugin);
        defender.onLoad(plugin);
    }

    @Override
    public void onEnable(AbstractAlathranWars plugin) {
        attacker.onEnable(plugin);
        defender.onEnable(plugin);
    }

    @Override
    public void onDisable(AbstractAlathranWars plugin) {
        attacker.onDisable(plugin);
        defender.onDisable(plugin);
    }

    /**
     * Gets name of the war.
     *
     * @return the name
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Gets label of the war.
     *
     * @return the label
     */
    @Override
    @NotNull
    public String getLabel() {
        return label;
    }

    /**
     * Compare war by names.
     *
     * @param warName the war name
     * @return the boolean
     */
    public boolean equals(String warName) {
        return this.name.equals(warName);
    }

    /**
     * Gets side 1.
     *
     * @return the side 1
     */
    @Override
    @NotNull
    public Side getSide1() {
        return side1;
    }

    /**
     * Gets side 2.
     *
     * @return the side 2
     */
    @Override
    @NotNull
    public Side getSide2() {
        return side2;
    }

    /**
     * Gets sides in a list.
     *
     * @return the sides
     */
    @Override
    @NotNull
    public Set<Side> getSides() {
        return Set.of(
            side1,
            side2
        );
    }

    /**
     * Gets attacker side.
     *
     * @return the attacker
     */
    @Override
    @NotNull
    public Side getAttacker() {
        return attacker;
    }

    /**
     * Gets defender side.
     *
     * @return the defender
     */
    @NotNull
    public Side getDefender() {
        return defender;
    }

    /**
     * Gets side by name.
     *
     * @param name the name
     * @return the side
     */
    @Override
    @Nullable
    public Side getSide(String name) {
        if (side1.equals(name))
            return side1;

        if (side2.equals(name))
            return side2;

        return null;
    }

    /**
     * Gets side by the side UUID.
     *
     * @param uuid the uuid
     * @return the side
     */
    @Nullable
    public Side getSide(UUID uuid) {
        if (side1.equals(uuid))
            return side1;

        if (side2.equals(uuid))
            return side2;

        return null;
    }

    // SECTION Battles

    /**
     * Gets sieges in a list.
     *
     * @return the sieges
     */
    @Override
    @NotNull
    public Set<Siege> getSieges() {
        return sieges;
    }

    /**
     * Sets sieges for the war.
     *
     * @param sieges the sieges
     */
    @ApiStatus.Internal
    public void setSieges(Set<Siege> sieges) {
        this.sieges = sieges;
    }

    /**
     * Gets siege by the siege UUID.
     *
     * @param uuid the uuid
     * @return the siege
     */
    @Nullable
    public Siege getSiege(UUID uuid) {
        return sieges.stream()
            .filter(s -> s.equals(uuid))
            .findAny()
            .orElse(null);
    }

    /**
     * Add siege to war.
     *
     * @param siege the siege
     */
    @Override
    @ApiStatus.Internal
    public void addSiege(Siege siege) {
        sieges.add(siege);
    }

    /**
     * Remove siege from war.
     *
     * @param siege the siege
     */
    @ApiStatus.Internal
    public void removeSiege(Siege siege) {
        sieges.remove(siege);
    }

    /**
     * Gets raids.
     *
     * @return the raids
     */
    @NotNull
    public Set<Raid> getRaids() {
        return raids;
    }

    /**
     * Sets raids for the war.
     *
     * @param raids the raids
     */
    @ApiStatus.Internal
    public void setRaids(Set<Raid> raids) {
        this.raids = raids;
        // TODO Raids
//        this.raids.forEach(Raid::resume);
    }

    /**
     * Gets raid by the raid UUID.
     *
     * @param uuid the uuid
     * @return the raid
     */
    @Nullable
    public Raid getRaid(UUID uuid) {
        return raids.stream()
            .filter(s -> s.equals(uuid))
            .findAny()
            .orElse(null);
    }

    /**
     * Add raid to war.
     *
     * @param raid the raid
     */
    @ApiStatus.Internal
    public void addRaid(Raid raid) {
        raids.add(raid);
    }

    /**
     * Remove raid from war.
     *
     * @param raid the raid
     */
    @ApiStatus.Internal
    public void removeRaid(Raid raid) {
        raids.remove(raid);
    }

    /**
     * Is town under siege in this war.
     *
     * @param town the town
     * @return the boolean
     */
    public boolean isTownUnderSiege(Town town) {
        return sieges.stream().anyMatch(siege -> siege.getTown().equals(town));
    }

    /**
     * Is town under raid in this war.
     *
     * @param town the town
     * @return the boolean
     */
    public boolean isTownUnderRaid(Town town) {
        // TODO Raids
        return false;
//        return raids.stream().anyMatch(raid -> raid.getTown().equals(town));
    }

    // SECITON Nation & Town Getters

    /**
     * Gets nations in the war excluding surrendered ones.
     *
     * @return the nations
     */
    @Override
    public Set<Nation> getNations() {
        return Stream.concat(
                side1.getNations().stream(),
                side2.getNations().stream()
            )
            .collect(Collectors.toSet());
    }

    /**
     * Gets all surrendered nations in the war.
     *
     * @return the surrendered nations
     */
    @Override
    public Set<Nation> getNationsSurrendered() {
        return Stream.concat(
                side1.getNationsSurrendered().stream(),
                side2.getNationsSurrendered().stream()
            )
            .collect(Collectors.toSet());
    }

    /**
     * Gets all nations in the war including surrendered.
     *
     * @return the all nations
     */
    @Override
    public Set<Nation> getNationsAll() {
        return Stream.concat(
                side1.getNations().stream(),
                Stream.concat(
                    side2.getNations().stream(),
                    getNationsSurrendered().stream()
                )
            )
            .collect(Collectors.toSet());
    }

    /**
     * Gets towns in the war excluding surrendered ones.
     *
     * @return the towns
     */
    @Override
    public Set<Town> getTowns() {
        return Stream.concat(
                side1.getTowns().stream(),
                side2.getTowns().stream()
            )
            .collect(Collectors.toSet());
    }

    /**
     * Gets all surrendered towns in the war.
     *
     * @return the surrendered towns
     */
    @Override
    public Set<Town> getTownsSurrendered() {
        return Stream.concat(
                side1.getTownsSurrendered().stream(),
                side2.getTownsSurrendered().stream()
            )
            .collect(Collectors.toSet());
    }

    /**
     * Gets all towns in the war including surrendered towns.
     *
     * @return the all towns
     */
    @Override
    public Set<Town> getTownsAll() {
        return Stream.concat(
                side1.getTowns().stream(),
                Stream.concat(
                    side2.getTowns().stream(),
                    getTownsSurrendered().stream()
                )
            )
            .collect(Collectors.toSet());
    }

    // SECTION Players Getters

    /**
     * Get all players in the war (excluding surrendered)
     *
     * @return player list
     */
    public List<OfflinePlayer> getPlayers() {
        return Stream.concat(
            side1.getPlayers().stream(),
            side2.getPlayers().stream()
        ).toList();
    }

    /**
     * Get all players in the war (excludes non-surrendered players)
     *
     * @return player list
     */
    public List<OfflinePlayer> getPlayersSurrendered() {
        return Stream.concat(
            side1.getPlayersSurrendered().stream(),
            side2.getPlayersSurrendered().stream()
        ).toList();
    }

    /**
     * Get all players in the war (includes surrendered players)
     *
     * @return player list
     */
    @Override
    public List<OfflinePlayer> getPlayersAll() {
        return Stream.concat(side1.getPlayersAll().stream(), side2.getPlayersAll().stream()).toList();
    }

    /**
     * Get all online players in the war (excluding surrendered)
     *
     * @return player list
     */
    @Override
    public List<Player> getPlayersOnline() {
        return Stream.concat(
            side1.getPlayersOnline().stream(),
            side2.getPlayersOnline().stream()
        ).toList();
    }

    /**
     * Get all online players in the war (excludes non-surrendered players)
     *
     * @return player list
     */
    @Override
    public List<Player> getPlayersSurrenderedOnline() {
        return Stream.concat(
            side1.getPlayersSurrenderedOnline().stream(),
            side2.getPlayersSurrenderedOnline().stream()
        ).toList();
    }

    /**
     * Get all online players in the war (includes surrendered players)
     *
     * @return player list
     */
    @Override
    public List<Player> getPlayersOnlineAll() {
        return Stream.concat(
            side1.getPlayersOnlineAll().stream(),
            side2.getPlayersOnlineAll().stream()
        ).toList();
    }

    // SECTION Misc

    /**
     * Check if player is in war.
     *
     * @param p the p
     * @return the boolean
     */
    @Override
    public boolean isInWar(Player p) {
        return side1.isOnSide(p) || side2.isOnSide(p);
    }

    /**
     * Check if player is in war.
     *
     * @param uuid the uuid
     * @return the boolean
     */
    public boolean isInWar(UUID uuid) {
        return side1.isOnSide(uuid) || side2.isOnSide(uuid);
    }

    /**
     * Gets the side of the player or null if they are not in the war.
     *
     * @param p the p
     * @return the player side
     */
    @Override
    @Nullable
    public Side getPlayerSide(Player p) {
        return getPlayerSide(p.getUniqueId());
    }

    /**
     * Gets player side by player UUID or null if they are not in the war.
     *
     * @param uuid the uuid
     * @return the player side
     */
    @Override
    @Nullable
    public Side getPlayerSide(UUID uuid) {
        if (side1.isOnSide(uuid))
            return side1;

        if (side2.isOnSide(uuid))
            return side2;

        return null;
    }

    /**
     * Is nation or town in war.
     *
     * @param government the nation or town
     * @return the boolean
     */
    @Override
    public boolean isInWar(Government government) {
        return side1.isOnSide(government) || side2.isOnSide(government);
    }

    /**
     * Gets nation or town side, or null if not in war.
     *
     * @param government the nation or town
     * @return the town side
     */
    @Override
    @Nullable
    public Side getSide(Government government) {
        if (side1.isOnSide(government))
            return side1;

        if (side2.isOnSide(government))
            return side2;

        return null;
    }

    /**
     * Check if a side exists with the specified name.
     *
     * @param sideName the side name
     * @return the boolean
     */
    @Override
    public boolean isSideValid(String sideName) {
        return side1.equals(sideName) || side2.equals(sideName);
    }

    /**
     * Cancel all active sieges in the war.
     */
    @ApiStatus.Internal
    public void cancelSieges() {
        getTowns().forEach(this::cancelSieges);
    }

    /**
     * Cancel all active sieges in the war. Ends the sieges in a draw.
     *
     * @param town the town
     */
    @Override
    @ApiStatus.Internal
    public void cancelSieges(Town town) {
        if (this != null) {
            Side townSide = getSide(town);

            if (townSide == null) return;

            getSieges().forEach(siege -> { // TODO INFINITE LOOP, runs surrenderTown
                if (siege.getTown().equals(town)) {
                    if (siege.getAttackerSide().equals(townSide)) {
                        siege.defendersWin(BattleVictoryReason.ADMIN_CANCEL);
                    } else {
                        siege.attackersWin(BattleVictoryReason.ADMIN_CANCEL);
                    }
                }
            });
        }
    }

    /**
     * Makes a government surrender (Recursively surrenders subjects AKA nations, towns, players)
     *
     * @param government the government
     */
    @Override
    public void surrender(Government government) {
        if (government instanceof Nation nation) {
            final SideImpl nationSide = (SideImpl) getSide(nation);
            Objects.requireNonNull(nationSide, "Nation side is null");

            final @Nullable Nation occupier = nationSide.equals(side1) ? side2.getTown().getNationOrNull() : side1.getTown().getNationOrNull();
            Occupation.setOccupied(nation, occupier);

            nationSide.surrender(nation);
            // TODO Cancel in progress sieges for towns?
            nationSide.processSurrenders();
        } else if (government instanceof Town town) {
            final SideImpl townSide = (SideImpl) getSide(town);
            Objects.requireNonNull(townSide, "Town side is null");

            final @Nullable Nation townNation = town.getNationOrNull();
            final @Nullable Nation occupier = townSide.equals(side1) ? side2.getTown().getNationOrNull() : side1.getTown().getNationOrNull();
            Occupation.setOccupied(town, occupier);
            townSide.surrender(town);

            if (townNation != null && townSide.shouldSurrender(townNation)) {
                surrender(townNation);
            }

            // TODO Cancel in progress sieges for towns?
            townSide.processSurrenders();
        }
    }

    /**
     * Makes a government un-surrender (Recursively un-surrenders subjects AKA nations, towns, players)
     *
     * @param government the government
     */
    @Override
    public void unsurrender(Government government) {
        if (government instanceof Nation nation) {
            SideImpl nationSide = (SideImpl) getSide(nation);
            if (nationSide == null) return;

            nationSide.unsurrender(nation);
            nation.getTowns().forEach(this::unsurrender);
        } else if (government instanceof Town town) {
            SideImpl townSide = (SideImpl) getSide(town);
            if (townSide == null) return;

            Occupation.removeOccupied(town);

            townSide.unsurrender(town);
        }
    }

    /**
     * Start a new war after creation.
     */
    @ApiStatus.Internal
    public void start() {
        Bukkit.broadcast(Component.translatable(
            "alathranwars.war.started.broadcast",
            Argument.component("prefix", Component.translatable("alathranwars.prefix")),
            Argument.string("war", getLabel()),
            Argument.string("attacker", getAttacker().getName()),
            Argument.string("defender", getDefender().getName()))
        );

        final Title warTitle = Title.title(
            Component.translatable("alathranwars.war.banner"),
            Component.translatable(
                "alathranwars.war.started.subtitle",
                Argument.string("war", getLabel())),
            Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(500))
        );
        final Sound warSound = Sound.sound(Key.key("entity.wither.spawn"), Sound.Source.VOICE, 0.5f, 1.0F);

        side1.getPlayersOnline().forEach(player -> {
            player.showTitle(warTitle);
            player.playSound(warSound);
        });
        side2.getPlayersOnline().forEach(player -> {
            player.showTitle(warTitle);
            player.playSound(warSound);
        });

        side1.applyNameTags();
        side2.applyNameTags();
        capturePointManager.start();
        if (Hook.PacketEvents.isLoaded())
            Bukkit.getServer().getPluginManager().registerEvents(capturePointManager, AlathranWars.getInstance());
    }

    /**
     * Resume a pre-existing war after loading it from db.
     */
    @ApiStatus.Internal
    public void resume() {
        side1.applyNameTags();
        side2.applyNameTags();
        capturePointManager.start();
        if (Hook.PacketEvents.isLoaded())
            Bukkit.getServer().getPluginManager().registerEvents(capturePointManager, AlathranWars.getInstance());
    }

    /**
     * End a war for the specified reason. Triggered internally by {@link WarImpl#defeat(Side)} {@literal &} {@link WarImpl#draw()}.
     *
     * @param reason the reason
     */
    @ApiStatus.Internal
    public void end(WarDeleteReason reason) {
        if (!new PreWarDeleteEvent(this, reason).callEvent())
            return;

        capturePointManager.stop();
        getSieges().forEach(Siege::stop);
        getTownsAll().forEach(Occupation::removeOccupied);

        WarQueries.deleteWar(this);
        WarController.getInstance().removeWar(this);

        // Run check after war is removed to cleanup player tags
        getPlayersOnline().forEach(p -> NameColorHandler.getInstance().calculatePlayerColors(p));

        new WarDeleteEvent(this, reason).callEvent();
    }

    /**
     * End the war in defeat for the specified side. Internally runs {@link WarImpl#end(WarDeleteReason)}.
     *
     * @param loserSide the loser side
     */
    public void defeat(Side loserSide) {
        Side loser = loserSide.equals(side1) ? side1 : side2;
        Side winner = loserSide.equals(side1) ? side2 : side1;

        Bukkit.broadcast(Component.translatable(
            "alathranwars.war.ended.broadcast",
            Argument.component("prefix", Component.translatable("alathranwars.prefix")),
            Argument.string("war", getLabel()),
            Argument.string("winner", winner.getName()),
            Argument.string("loser", loser.getName()))
        );

        end(WarDeleteReason.DEFEAT);
    }

    /**
     * End the war in a white peace/draw. Internally runs {@link WarImpl#end(WarDeleteReason)}.
     */
    @Override
    public void draw() {
        Bukkit.broadcast(Component.translatable(
            "alathranwars.war.ended.white-peace",
            Argument.component("prefix", Component.translatable("alathranwars.prefix")),
            Argument.string("war", getLabel()))
        );

        end(WarDeleteReason.DRAW);
    }

    /**
     * Sets if this is an event war.
     *
     * @param event the event
     */
    public void setEventWar(boolean event) {
        this.event = event;
    }

    /**
     * Gets if this is an event war. Event wars skip or execute certain logic as they are for player events.
     *
     * @return the boolean
     */
    @Override
    public boolean isEventWar() {
        return event;
    }

    @Override
    public boolean isWarTime() {
        return isEventWar() || (getScheduledWarTime() != null &&
            Instant.now().isAfter(getScheduledWarTime()) &&
            Instant.now().isBefore(getScheduledWarTime().plus(Duration.ofMinutes(Cfg.get().war.wartimeDuration))));
    }

    @Nullable
    public Instant getScheduledWarTime() {
        return warTimeManager.getScheduledWarTime();
    }

    @Override
    public void setScheduledWarTime(@Nullable Instant scheduledWarTime) {
        warTimeManager.setScheduledWarTime(scheduledWarTime);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private @Nullable UUID uuid;
        private @Nullable String name;
        private @Nullable String label;
        private ConflictType conflictType = ConflictType.WAR;
        private boolean event = false;
        private @Nullable Instant scheduledWarTime = null;
        private @Nullable SideImpl side1;
        private @Nullable SideImpl side2;
        private @Nullable Set<Siege> sieges = new HashSet<>();
        private @Nullable Set<Raid> raids = new HashSet<>();

        private @Nullable Government aggressor;
        private @Nullable Government victim;

        private Builder() {
        }

        /**
         * Build a new war from save data
         *
         * @return a new war
         * @throws WarCreationException exception
         */
        public WarImpl resume() throws WarCreationException {
            if (uuid == null)
                throw new WarCreationException("Missing state uuid required to create War!");

            if (name == null)
                throw new WarCreationException("Missing state name required to create War!");

            if (label == null)
                throw new WarCreationException("Missing state label required to create War!");

            if (side1 == null)
                throw new WarCreationException("Missing state side1 required to create War!");

            if (side2 == null)
                throw new WarCreationException("Missing state side2 required to create War!");

            if (sieges == null)
                throw new WarCreationException("Missing state sieges required to create War!");

            if (raids == null)
                throw new WarCreationException("Missing state raids required to create War!");

            return new WarImpl(
                uuid,
                name,
                label,
                side1,
                side2,
                sieges,
                raids,
                event,
                scheduledWarTime
            );
        }

        /**
         * Build a new War
         *
         * @return a new war
         * @throws WrapperCommandSyntaxException exception
         * @throws SideCreationException         exception
         */
        public WarImpl create() throws WrapperCommandSyntaxException, SideCreationException {
            this.setUuid(UUID.randomUUID());

            if (uuid == null)
                throw new WarCreationException("Missing state uuid required to create War!");

            if (label == null)
                throw new WarCreationException("Missing state label required to create War!");

            if (aggressor == null)
                throw new WarCreationException("Missing state aggressor required to create War!");

            if (victim == null)
                throw new WarCreationException("Missing state victim required to create War!");

            if (!canStartWar())
                throw new WarCreationException("The attacking or defending side is on cooldown!");

            return new WarImpl(
                uuid,
                label,
                aggressor,
                victim,
                event,
                scheduledWarTime
            );
        }

        public Builder setUuid(UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setLabel(String label) {
            this.label = label;
            return this;
        }

        public Builder setConflictType(ConflictType conflictType) {
            this.conflictType = conflictType;
            return this;
        }

        public Builder setSide1(SideImpl side1) {
            this.side1 = side1;
            return this;
        }

        public Builder setSide2(SideImpl side2) {
            this.side2 = side2;
            return this;
        }

        public Builder setEvent(boolean event) {
            this.event = event;
            return this;
        }

        public Builder setScheduledWarTime(@Nullable Instant scheduledWarTime) {
            this.scheduledWarTime = scheduledWarTime;
            return this;
        }

        public Builder setSieges(Set<Siege> sieges) {
            this.sieges = sieges;
            return this;
        }

        public Builder setRaids(Set<Raid> raids) {
            this.raids = raids;
            return this;
        }

        public Builder setAggressor(Government aggressor) {
            this.aggressor = aggressor;
            return this;
        }

        public Builder setVictim(Government victim) {
            this.victim = victim;
            return this;
        }

        /**
         * Checks cooldowns for starting a war.
         *
         * @return true if war can be started, false otherwise
         */
        private boolean canStartWar() {
            if (event)
                return true;

            if (AlathranWarsAPIImpl.getInstance().hasAttackCooldown(aggressor))
                return false;

            return !AlathranWarsAPIImpl.getInstance().hasDefenseCooldown(victim);
        }
    }
}