package io.github.alathra.alathranwars.conflict.persistence;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import io.github.alathra.alathranwars.conflict.battle.siege.Siege;
import io.github.alathra.alathranwars.conflict.battle.siege.SiegeImpl;
import io.github.alathra.alathranwars.conflict.battle.siege.SiegePhase;
import io.github.alathra.alathranwars.conflict.war.WarController;
import io.github.alathra.alathranwars.conflict.war.WarCreationException;
import io.github.alathra.alathranwars.conflict.war.WarImpl;
import io.github.alathra.alathranwars.conflict.war.side.Side;
import io.github.alathra.alathranwars.conflict.war.side.SideCreationException;
import io.github.alathra.alathranwars.conflict.war.side.SideImpl;
import io.github.alathra.alathranwars.conflict.war.side.spawn.RallyPointImpl;
import io.github.alathra.alathranwars.conflict.war.side.spawn.SpawnType;
import io.github.alathra.alathranwars.database.QueryUtils;
import io.github.alathra.alathranwars.database.schema.tables.records.*;
import io.github.alathra.alathranwars.enums.battle.BattleSide;
import io.github.alathra.alathranwars.enums.battle.BattleTeam;
import io.github.alathra.alathranwars.utility.DB;
import io.github.alathra.alathranwars.utility.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.exception.DataAccessException;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.github.alathra.alathranwars.database.QueryUtils.UUIDUtil;
import static io.github.alathra.alathranwars.database.schema.Tables.*;

/**
 * A class providing access to all war domain SQL queries.
 */
@SuppressWarnings({"LoggingSimilarMessage", "StringConcatenationArgumentToLogCall"})
public final class WarQueries {
    public static void saveAll() {
        try (
            Connection con = DB.getConnection()
        ) {
            DSLContext context = DB.getContext(con);

            for (WarImpl war : WarController.getInstance().getWarImpls()) {
                final SideImpl warSide1 = (SideImpl) war.getSide1();
                final SideImpl warSide2 = (SideImpl) war.getSide2();

                context
                    .insertInto(LIST, LIST.UUID, LIST.NAME, LIST.LABEL, LIST.SIDE1, LIST.SIDE2, LIST.EVENT, LIST.WAR_TIME)
                    .values(
                        QueryUtils.UUIDUtil.toBytes(war.getUUID()),
                        war.getName(),
                        war.getLabel(),
                        QueryUtils.UUIDUtil.toBytes(warSide1.getUUID()),
                        QueryUtils.UUIDUtil.toBytes(warSide2.getUUID()),
                        war.isEventWar() ? (byte) 1 : (byte) 0,
                        QueryUtils.InstantUtil.toDateTime(war.getScheduledWarTime())
                    )
                    .onDuplicateKeyUpdate()
                    .set(LIST.NAME, war.getName())
                    .set(LIST.LABEL, war.getLabel())
                    .set(LIST.SIDE1, QueryUtils.UUIDUtil.toBytes(warSide1.getUUID()))
                    .set(LIST.SIDE2, QueryUtils.UUIDUtil.toBytes(warSide2.getUUID()))
                    .set(LIST.EVENT, war.isEventWar() ? (byte) 1 : (byte) 0)
                    .set(LIST.WAR_TIME, QueryUtils.InstantUtil.toDateTime(war.getScheduledWarTime()))
                    .execute();


                // Save sides
                for (Side side : war.getSides()) {
                    saveSide(context, (SideImpl) side);
                }

                // Save sieges
                for (Siege siege : war.getSieges()) {
                    saveSiege(context, (SiegeImpl) siege);
                }
            }
        } catch (SQLException e) {
            Logger.get().error("SQL Query failed to save data!", e);
        }
    }

    private static void saveSide(DSLContext context, SideImpl side) {
        context
            .insertInto(SIDES, SIDES.WAR, SIDES.UUID, SIDES.SIDE, SIDES.TEAM, SIDES.NAME, SIDES.TOWN, SIDES.SIEGE_GRACE, SIDES.RAID_GRACE)
            .values(
                QueryUtils.UUIDUtil.toBytes(((WarImpl) Objects.requireNonNull(side.getWar(), "war is null when saving")).getUUID()),
                QueryUtils.UUIDUtil.toBytes(side.getUUID()),
                side.getSide().toString(),
                side.getTeam().toString(),
                side.getName(),
                QueryUtils.UUIDUtil.toBytes(side.getTown().getUUID()),
                LocalDateTime.ofInstant(side.getSiegeGrace(), ZoneOffset.UTC),
                LocalDateTime.ofInstant(side.getRaidGrace(), ZoneOffset.UTC)
            )
            .onDuplicateKeyUpdate()
            .set(SIDES.SIDE, side.getSide().toString())
            .set(SIDES.TEAM, side.getTeam().toString())
            .set(SIDES.NAME, side.getName())
            .set(SIDES.TOWN, QueryUtils.UUIDUtil.toBytes(side.getTown().getUUID()))
            .set(SIDES.SIEGE_GRACE, LocalDateTime.ofInstant(side.getSiegeGrace(), ZoneOffset.UTC))
            .set(SIDES.RAID_GRACE, LocalDateTime.ofInstant(side.getRaidGrace(), ZoneOffset.UTC))
            .execute();

        saveSideNations(context, side);
        saveSideTowns(context, side);
        saveSidePlayers(context, side);
        saveSideSpawns(context, side);
    }

    private static void saveSideNations(DSLContext context, SideImpl side) {
        context.transaction(config -> {
            DSLContext ctx = config.dsl();

            ctx
                .deleteFrom(SIDES_NATIONS)
                .where(SIDES_NATIONS.SIDE.equal(QueryUtils.UUIDUtil.toBytes(side.getUUID())))
                .execute();

            ctx.batchInsert(
                side.getNations().stream().map(nation -> new SidesNationsRecord(
                    UUIDUtil.toBytes(side.getUUID()),
                    UUIDUtil.toBytes(nation.getUUID()),
                    (byte) 0
                )).toList()
            ).execute();

            ctx.batchInsert(
                side.getNationsSurrendered().stream().map(nation -> new SidesNationsRecord(
                    UUIDUtil.toBytes(side.getUUID()),
                    UUIDUtil.toBytes(nation.getUUID()),
                    (byte) 1
                )).toList()
            ).execute();
        });
    }

    private static void saveSideTowns(DSLContext context, SideImpl side) {
        context.transaction(config -> {
            DSLContext ctx = config.dsl();

            ctx
                .deleteFrom(SIDES_TOWNS)
                .where(SIDES_TOWNS.SIDE.equal(QueryUtils.UUIDUtil.toBytes(side.getUUID())))
                .execute();

            ctx.batchInsert(
                side.getTowns().stream().map(town -> new SidesTownsRecord(
                    UUIDUtil.toBytes(side.getUUID()),
                    UUIDUtil.toBytes(town.getUUID()),
                    (byte) 0
                )).toList()
            ).execute();

            ctx.batchInsert(
                side.getTownsSurrendered().stream().map(town -> new SidesTownsRecord(
                    UUIDUtil.toBytes(side.getUUID()),
                    UUIDUtil.toBytes(town.getUUID()),
                    (byte) 1
                )).toList()
            ).execute();
        });
    }

    private static void saveSidePlayers(DSLContext context, SideImpl side) {
        context.transaction(config -> {
            DSLContext ctx = config.dsl();

            ctx
                .deleteFrom(SIDES_PLAYERS)
                .where(SIDES_PLAYERS.SIDE.equal(QueryUtils.UUIDUtil.toBytes(side.getUUID())))
                .execute();

            ctx.batchInsert(
                side.getPlayersAll().stream().map(p ->
                    new SidesPlayersRecord(
                        QueryUtils.UUIDUtil.toBytes(side.getUUID()),
                        QueryUtils.UUIDUtil.toBytes(p.getUniqueId()),
                        (byte) 0
                    )
                ).toList()
            ).execute();

            ctx.batchInsert(
                side.getPlayersSurrendered().stream().map(p ->
                    new SidesPlayersRecord(
                        QueryUtils.UUIDUtil.toBytes(side.getUUID()),
                        QueryUtils.UUIDUtil.toBytes(p.getUniqueId()),
                        (byte) 1
                    )
                ).toList()
            ).execute();
        });
    }

    private static void saveSideSpawns(DSLContext context, SideImpl side) {
        context.transaction(config -> {
            DSLContext ctx = config.dsl();

            ctx
                .deleteFrom(SIDES_SPAWNS)
                .where(SIDES_SPAWNS.SIDE.equal(QueryUtils.UUIDUtil.toBytes(side.getUUID())))
                .execute();

            ctx.batchInsert(
                side.getSpawnManager().getRallies()
                    .stream()
                    .map(RallyPointImpl::deserialize)
                    .collect(Collectors.toList())
            ).execute();
        });
    }

    private static void saveSiege(DSLContext context, SiegeImpl siege) {
        context.insertInto(SIEGES, SIEGES.WAR, SIEGES.UUID, SIEGES.TOWN, SIEGES.SIEGE_LEADER, SIEGES.END_TIME, SIEGES.LAST_TOUCHED, SIEGES.SIEGE_PROGRESS, SIEGES.PHASE_CURRENT, SIEGES.PHASE_PROGRESS, SIEGES.PHASE_START_TIME)
            .values(
                QueryUtils.UUIDUtil.toBytes(((WarImpl) siege.getWar()).getUUID()),
                QueryUtils.UUIDUtil.toBytes(siege.getUUID()),
                QueryUtils.UUIDUtil.toBytes(siege.getTown().getUUID()),
                QueryUtils.UUIDUtil.toBytes(siege.getSiegeLeader().getUniqueId()),
                LocalDateTime.ofInstant(siege.getEndTime(), ZoneOffset.UTC),
                LocalDateTime.ofInstant(siege.getLastTouched(), ZoneOffset.UTC),
                siege.getProgressManager().get(),
                siege.getPhaseManager().get().name(),
                siege.getPhaseManager().getProgress(),
                LocalDateTime.ofInstant(siege.getPhaseManager().getStartTime(), ZoneOffset.UTC)
            )
            .onDuplicateKeyUpdate()
            .set(SIEGES.WAR, QueryUtils.UUIDUtil.toBytes(((WarImpl) siege.getWar()).getUUID()))
            .set(SIEGES.TOWN, QueryUtils.UUIDUtil.toBytes(siege.getTown().getUUID()))
            .set(SIEGES.SIEGE_LEADER, QueryUtils.UUIDUtil.toBytes(siege.getSiegeLeader().getUniqueId()))
            .set(SIEGES.END_TIME, LocalDateTime.ofInstant(siege.getEndTime(), ZoneOffset.UTC))
            .set(SIEGES.LAST_TOUCHED, LocalDateTime.ofInstant(siege.getLastTouched(), ZoneOffset.UTC))
            .set(SIEGES.SIEGE_PROGRESS, siege.getProgressManager().get())
            .set(SIEGES.PHASE_CURRENT, siege.getPhaseManager().get().name())
            .set(SIEGES.PHASE_PROGRESS, siege.getPhaseManager().getProgress())
            .set(SIEGES.PHASE_START_TIME, LocalDateTime.ofInstant(siege.getPhaseManager().getStartTime(), ZoneOffset.UTC))
            .execute();

        saveSiegePlayers(context, siege);
    }

    private static void saveSiegePlayers(DSLContext context, SiegeImpl siege) { // TODO We no longer save players in battles
        context.transaction(config -> {
            DSLContext ctx = config.dsl();

            ctx
                .deleteFrom(SIEGE_PLAYERS)
                .where(SIEGE_PLAYERS.SIEGE.equal(QueryUtils.UUIDUtil.toBytes(siege.getUUID())))
                .execute();

            ctx.batchInsert(
                siege.getPlayersInBattle(BattleSide.ATTACKER).stream().map(p ->
                    new SiegePlayersRecord(
                        QueryUtils.UUIDUtil.toBytes(siege.getUUID()),
                        QueryUtils.UUIDUtil.toBytes(p.getUniqueId()),
                        (byte) 0
                    )
                ).toList()
            ).execute();

            ctx.batchInsert(
                siege.getPlayersInBattle(BattleSide.DEFENDER).stream().map(p ->
                    new SiegePlayersRecord(
                        QueryUtils.UUIDUtil.toBytes(siege.getUUID()),
                        QueryUtils.UUIDUtil.toBytes(p.getUniqueId()),
                        (byte) 1
                    )
                ).toList()
            ).execute();
        });
    }

    // Getters
    public static @NotNull Set<WarImpl> loadAll() {
        Set<WarImpl> wars = new HashSet<>();

        try (
            Connection con = DB.getConnection()
        ) {
            DSLContext context = DB.getContext(con);

            Result<ListRecord> result = context
                .selectFrom(LIST)
                .fetch();

            for (ListRecord r : result) {
                UUID uuid = QueryUtils.UUIDUtil.fromBytes(r.getUuid());
                @Nullable SideImpl side1 = loadSide(con, uuid, QueryUtils.UUIDUtil.fromBytes(r.getSide1()));
                @Nullable SideImpl side2 = loadSide(con, uuid, QueryUtils.UUIDUtil.fromBytes(r.getSide2()));
                boolean event = QueryUtils.BooleanUtil.fromByte(r.getEvent());

                wars.add(
                    WarImpl.builder()
                        .setUuid(uuid)
                        .setName(r.getName())
                        .setLabel(r.getLabel())
                        .setSide1(side1)
                        .setSide2(side2)
                        .setSieges(new HashSet<>())
                        .setRaids(new HashSet<>())
                        .setEvent(event)
                        .setScheduledWarTime(QueryUtils.InstantUtil.fromDateTime(r.getWarTime()))
                        .resume()
                );
            }

            for (WarImpl war : wars) {
                @NotNull Set<Siege> sieges = loadSieges(con, war);
                war.setSieges(sieges);
                sieges.forEach(Siege::resume);
            }
        } catch (SQLException | DataAccessException e) {
            Logger.get().error("SQL Query threw an error!", e);
        } catch (WarCreationException e) {
            Logger.get().error("Failed to re-create war from database!", e);
        }

        return wars;
    }

    @Contract("_, _, _ -> new")
    public static @Nullable SideImpl loadSide(Connection con, UUID warUUID, UUID uuid) {
        try {
            DSLContext context = DB.getContext(con);

            SidesRecord r = context
                .selectFrom(SIDES)
                .where(SIDES.WAR.equal(QueryUtils.UUIDUtil.toBytes(warUUID)))
                .and(SIDES.UUID.equal(QueryUtils.UUIDUtil.toBytes(uuid)))
                .fetchOne();

            if (r == null)
                return null;

            // Side data
            BattleSide side = BattleSide.valueOf(r.getSide());
            BattleTeam team = BattleTeam.valueOf(r.getTeam());
            String name = r.getName();
            @Nullable Town town = TownyAPI.getInstance().getTown(QueryUtils.UUIDUtil.fromBytes(r.getTown()));
            Instant siegeGrace = r.getSiegeGrace().toInstant(ZoneOffset.UTC);
            Instant raidGrace = r.getRaidGrace().toInstant(ZoneOffset.UTC);

            // Load players
            Set<UUID> players = new HashSet<>();
            Set<UUID> playersSurrendered = new HashSet<>();
            context.selectFrom(SIDES_PLAYERS)
                .where(SIDES_PLAYERS.SIDE.equal(QueryUtils.UUIDUtil.toBytes(uuid)))
                .fetch()
                .forEach(record -> {
                    if (record.get(SIDES_PLAYERS.SURRENDERED).equals((byte) 0)) {
                        players.add(QueryUtils.UUIDUtil.fromBytes(record.getPlayer()));
                    } else {
                        playersSurrendered.add(QueryUtils.UUIDUtil.fromBytes(record.getPlayer()));
                    }
                });

            // Load nations
            Set<Nation> nations = new HashSet<>();
            Set<Nation> nationsSurrendered = new HashSet<>();
            context.selectFrom(SIDES_NATIONS)
                .where(SIDES_NATIONS.SIDE.equal(QueryUtils.UUIDUtil.toBytes(uuid)))
                .fetch()
                .forEach(
                    record -> {
                        final UUID identifier = QueryUtils.UUIDUtil.fromBytes(record.getNation());
                        if (record.getSurrendered().equals((byte) 0)) {
                            @Nullable Nation nation = TownyAPI.getInstance().getNation(identifier);
                            if (nation != null)
                                nations.add(nation);
                        } else {
                            @Nullable Nation nation = TownyAPI.getInstance().getNation(identifier);
                            if (nation != null)
                                nationsSurrendered.add(nation);
                        }
                    });

            // Load towns
            Set<Town> towns = new HashSet<>();
            Set<Town> townsSurrendered = new HashSet<>();
            context.selectFrom(SIDES_TOWNS)
                .where(SIDES_TOWNS.SIDE.equal(QueryUtils.UUIDUtil.toBytes(uuid)))
                .fetch()
                .forEach(record -> {
                    final UUID identifier = QueryUtils.UUIDUtil.fromBytes(record.getTown());
                    if (record.getSurrendered().equals((byte) 0)) {
                        @Nullable Town town2 = TownyAPI.getInstance().getTown(identifier);
                        if (town2 != null)
                            towns.add(town2);
                    } else {
                        @Nullable Town town2 = TownyAPI.getInstance().getTown(identifier);
                        if (town2 != null)
                            townsSurrendered.add(town2);
                    }
                });

            // Load rallies
            Set<RallyPointImpl> rallies = context.selectFrom(SIDES_SPAWNS)
                .where(SIDES_SPAWNS.SIDE.endsWith(QueryUtils.UUIDUtil.toBytes(uuid)))
                .fetch()
                .stream()
                .map(spawn -> {
                    final @Nullable World world = Bukkit.getWorld(UUIDUtil.fromBytes(spawn.getWorld()));
                    if (world == null)
                        return null;

                    final Location location = new Location(
                        world,
                        spawn.getX(),
                        spawn.getY(),
                        spawn.getZ(),
                        spawn.getYaw().floatValue(),
                        spawn.getPitch().floatValue()
                    );

                    final @Nullable World blockWorld = Bukkit.getWorld(UUIDUtil.fromBytes(spawn.getBlockWorld()));
                    if (blockWorld == null)
                        return null;

                    final Block block = blockWorld.getBlockAt(spawn.getX(), spawn.getY(), spawn.getZ());

                    return new RallyPointImpl(
                        spawn.get_Name(),
                        location,
                        SpawnType.RALLY,
                        null,
                        block,
                        Bukkit.getOfflinePlayer(UUIDUtil.fromBytes(spawn.getCreator()))
                    );
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

            return SideImpl.builder() // TODO IllegalStateException
                .setWarUUID(warUUID)
                .setUuid(uuid)
                .setLeader(town)
                .setSide(side)
                .setTeam(team)
                .setName(name)
                .setTowns(towns)
                .setNations(nations)
                .setPlayers(players)
                .setTownsSurrendered(townsSurrendered)
                .setNationsSurrendered(nationsSurrendered)
                .setPlayersSurrendered(playersSurrendered)
                .setSiegeGrace(siegeGrace)
                .setRaidGrace(raidGrace)
                .setRallies(rallies)
                .rebuild();
        } catch (SideCreationException e) {
            Logger.get().error("SQL Query threw an error!", e);
        }
        return null;
    }

    public static @NotNull Set<Siege> loadSieges(Connection con, WarImpl war) {
        Set<Siege> sieges = new HashSet<>();

        try {
            DSLContext context = DB.getContext(con);

            Result<Record> result = context.select()
                .from(SIEGES)
                .where(SIEGES.WAR.equal(QueryUtils.UUIDUtil.toBytes(war.getUUID())))
                .fetch();

            for (Record r : result) {
                UUID uuid = QueryUtils.UUIDUtil.fromBytes(r.get(SIEGES.UUID));
                @Nullable Town town = TownyAPI.getInstance().getTown(QueryUtils.UUIDUtil.fromBytes(r.get(SIEGES.TOWN)));
                OfflinePlayer siegeLeader = Bukkit.getOfflinePlayer(QueryUtils.UUIDUtil.fromBytes(r.get(SIEGES.SIEGE_LEADER)));
                Instant endTime = r.get(SIEGES.END_TIME).toInstant(ZoneOffset.UTC);
                Instant lastTouched = r.get(SIEGES.LAST_TOUCHED).toInstant(ZoneOffset.UTC);
                int siegeProgress = r.get(SIEGES.SIEGE_PROGRESS);
                Set<UUID> attackersIncludingOffline = new HashSet<>();
                Set<UUID> defendersIncludingOffline = new HashSet<>();

                context.select()
                    .from(SIEGE_PLAYERS)
                    .where(SIEGE_PLAYERS.SIEGE.equal(QueryUtils.UUIDUtil.toBytes(uuid)))
                    .fetch()
                    .forEach(record -> {
                        if (record.get(SIEGE_PLAYERS.TEAM).equals((byte) 0)) {
                            attackersIncludingOffline.add(QueryUtils.UUIDUtil.fromBytes(record.get(SIEGE_PLAYERS.PLAYER)));
                        } else {
                            defendersIncludingOffline.add(QueryUtils.UUIDUtil.fromBytes(record.get(SIEGE_PLAYERS.PLAYER)));
                        }
                    });

                SiegePhase phase;
                try {
                    phase = SiegePhase.valueOf(r.get(SIEGES.PHASE_CURRENT));
                } catch (IllegalArgumentException e) {
                    phase = SiegePhase.SIEGE;
                }
                int phaseProgress = r.get(SIEGES.PHASE_PROGRESS);
                Instant phaseStartTime = r.get(SIEGES.PHASE_START_TIME).toInstant(ZoneOffset.UTC);


                sieges.add(new SiegeImpl(
                    war,
                    uuid,
                    town,
                    siegeLeader,
                    endTime,
                    lastTouched,
                    siegeProgress,
                    attackersIncludingOffline,
                    defendersIncludingOffline,
                    phase,
                    phaseProgress,
                    phaseStartTime
                ));
            }
        } catch (DataAccessException e) {
            Logger.get().error("SQL Query threw an error!", e);
        }

        return sieges;
    }

    // Deleters

    public static void deleteWar(WarImpl war) {
        try (
            Connection con = DB.getConnection()
        ) {
            DSLContext context = DB.getContext(con);

            context
                .deleteFrom(LIST)
                .where(LIST.UUID.equal(QueryUtils.UUIDUtil.toBytes(war.getUUID())))
                .execute();
        } catch (SQLException | DataAccessException e) {
            Logger.get().error("SQL Query threw an error!", e);
        }
    }

    public static void deleteSiege(Siege siege) {
        try (
            Connection con = DB.getConnection()
        ) {
            DSLContext context = DB.getContext(con);

            context
                .deleteFrom(SIEGES)
                .where(SIEGES.UUID.equal(QueryUtils.UUIDUtil.toBytes(siege.getUUID())))
                .execute();
        } catch (SQLException | DataAccessException e) {
            Logger.get().error("SQL Query threw an error!", e);
        }
    }
}
