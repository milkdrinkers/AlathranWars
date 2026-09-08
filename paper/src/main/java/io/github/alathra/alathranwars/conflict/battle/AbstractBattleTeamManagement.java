package io.github.alathra.alathranwars.conflict.battle;

import io.github.alathra.alathranwars.conflict.war.War;
import io.github.alathra.alathranwars.conflict.war.side.Side;
import io.github.alathra.alathranwars.enums.battle.BattleSide;
import io.github.alathra.alathranwars.event.battle.PlayerEnteredBattlefieldEvent;
import io.github.alathra.alathranwars.event.battle.PlayerLeftBattlefieldEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides methods for handling all members of a Battle
 */
public abstract class AbstractBattleTeamManagement {
    // SECTION Player management

    private final Side attackerSide;
    private final Side defenderSide;

    protected AbstractBattleTeamManagement(Side attackerSide, Side defenderSide) {
        this.attackerSide = attackerSide;
        this.defenderSide = defenderSide;
    }

    /**
     * Get all online players eligible to fight in the battle
     *
     * @param side battle side
     * @return set of players
     * @apiNote This does not mean the players are inside the battle zone, only that they are in the related war
     */
    public Set<Player> getPlayersInBattle(BattleSide side) {
        return switch (side) {
            case ATTACKER -> attackerSide.getPlayersOnline(); // Get online players from side as they are tracked there
            case DEFENDER -> defenderSide.getPlayersOnline(); // Get online players from side as they are tracked there
            case SPECTATOR -> Bukkit.getOnlinePlayers().stream() // Get all online players minus attackers & defenders
                .map(OfflinePlayer::getPlayer)
                .filter(Objects::nonNull)
                .filter(p -> !this.isInBattle(p))
                .collect(Collectors.toSet());
        };
    }

    // Player info methods

    /**
     * Get which team the player belongs to in the battle
     *
     * @param p player
     * @return the team, or spectator
     */
    public BattleSide getPlayerBattleSide(@Nullable Player p) {
        if (p == null)
            return BattleSide.SPECTATOR;

        if (isAttacker(p))
            return BattleSide.ATTACKER;

        if (isDefender(p))
            return BattleSide.DEFENDER;

        return BattleSide.SPECTATOR;
    }

    /**
     * Check if a player is on the attacking side
     *
     * @param p player
     * @return boolean
     */
    public boolean isAttacker(Player p) {
        return attackerSide.isOnSide(p);
    }

    /**
     * Check if a player is on the defending side
     *
     * @param p player
     * @return boolean
     */
    public boolean isDefender(Player p) {
        return defenderSide.isOnSide(p);
    }

    /**
     * Check if a player is on the spectator side
     *
     * @param p player
     * @return boolean
     */
    public boolean isSpectator(Player p) {
        return !isInBattle(p);
    }

    /**
     * Checks if the player is considered part of this battle
     *
     * @param p the player
     * @return yes if they are in the associated war
     * @apiNote This does not mean the player is inside the battle zone, only that they are in the associated war
     */
    public boolean isInBattle(@Nullable Player p) {
        if (p == null)
            return false;

        return isAttacker(p) || isDefender(p);
    }

    /**
     * Checks if the player is considered part of this battle
     *
     * @param uuid the player uuid
     * @return yes if they have joined the battle
     * @apiNote This does not mean the player is inside the battle zone, only that they are in the associated war
     */
    public boolean isInBattle(UUID uuid) {
        return isInBattle(Bukkit.getOfflinePlayer(uuid).getPlayer());
    }

    // SECTION Players in battle zone management

    private final Set<Player> attackers = new HashSet<>(); // Players inside the battle zone
    private final Set<Player> defenders = new HashSet<>(); // Players inside the battle zone
    private final Set<Player> spectators = new HashSet<>(); // Players inside the battle zone

    /**
     * Get all online players inside the battle zone.
     *
     * @return set of players
     */
    public Set<Player> getPlayersInZone() {
        return Stream.concat(
            attackers.stream(),
            Stream.concat(
                defenders.stream(),
                spectators.stream()
            )
        ).collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Get all online players inside the battle zone.
     *
     * @param side battle side
     * @return set of players
     */
    public Set<Player> getPlayersInZone(BattleSide side) {
        return switch (side) {
            case ATTACKER -> attackers;
            case DEFENDER -> defenders;
            case SPECTATOR -> spectators;
        };
    }

    /**
     * Checks if the player is inside the battle zone
     *
     * @param p the player
     * @return yes if they are inside the battle zone
     */
    public boolean isPlayerInZone(@Nullable Player p) {
        if (p == null)
            return false;

        return attackers.contains(p) || defenders.contains(p) || spectators.contains(p);
    }

    /**
     * Checks if the player is inside the battle zone
     *
     * @param uuid the player uuid
     * @return yes if they are inside the battle zone
     */
    public boolean isPlayerInZone(UUID uuid) {
        return isPlayerInZone(Bukkit.getOfflinePlayer(uuid).getPlayer());
    }

    /**
     * Check if a player on the attacking side is inside the battle zone
     *
     * @param p player
     * @return boolean
     */
    public boolean isAttackerInZone(Player p) {
        return attackers.contains(p);
    }

    /**
     * Check if a player on the defending side is inside the battle zone
     *
     * @param p player
     * @return boolean
     */
    public boolean isDefenderInZone(Player p) {
        return defenders.contains(p);
    }

    /**
     * Check if a player on the spectator side is inside the battle zone
     *
     * @param p player
     * @return boolean
     */
    public boolean isSpectatorInZone(Player p) {
        return spectators.contains(p);
    }

    /**
     * Recalculate which players are within the zone and send events for leaving/entering players
     *
     * @param location location
     * @param range    range
     * @param war      war
     * @param battle   battle
     * @apiNote Leaving/entering events are only emitted for connected players
     */
    public void calculateBattlefieldPlayers(Location location, int range, War war, Battle battle) {
        // Make copied sets of players who were previously inside the zone
        final Set<Player> previousAttackers = Set.copyOf(getPlayersInZone(BattleSide.ATTACKER));
        final Set<Player> previousDefenders = Set.copyOf(getPlayersInZone(BattleSide.DEFENDER));
        final Set<Player> previousSpectators = Set.copyOf(getPlayersInZone(BattleSide.SPECTATOR));

        emitLeaving(attackers, previousAttackers, BattleSide.ATTACKER, war, battle); // Emit Leaving attackers events
        emitLeaving(defenders, previousDefenders, BattleSide.DEFENDER, war, battle); // Emit Leaving defenders events
        emitLeaving(spectators, previousSpectators, BattleSide.SPECTATOR, war, battle); // Emit Leaving spectators events

        // Clear sets and repopulate with people who are now in the zones
        attackers.clear();
        attackers.addAll(getPlayersInZone(BattleSide.ATTACKER, location, range));

        defenders.clear();
        defenders.addAll(getPlayersInZone(BattleSide.DEFENDER, location, range));

        spectators.clear();
        spectators.addAll(getPlayersInZone(BattleSide.SPECTATOR, location, range));

        // Attackers
        emitEntering(attackers, previousAttackers, BattleSide.ATTACKER, war, battle); // Emit Entering attackers events

        // Defenders
        emitEntering(defenders, previousDefenders, BattleSide.DEFENDER, war, battle); // Emit Entering defenders events

        // Spectators
        emitEntering(spectators, previousSpectators, BattleSide.SPECTATOR, war, battle); // Emit Entering spectators events
    }

    /**
     * Get a set of all players on a battle side who are inside the battle zone
     *
     * @param battleSide battle side
     * @param location   location
     * @param range      range
     * @return set of players in range of zone
     */
    private Set<Player> getPlayersInZone(BattleSide battleSide, Location location, int range) {
        return getPlayersInBattle(battleSide).stream()
            .filter(p -> !p.getGameMode().equals(GameMode.SPECTATOR) && !p.getGameMode().equals(GameMode.CREATIVE) && AbstractBattleTeamManagement.isInRange(p, location, range))
            .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Used to check if a player is within range of a location
     *
     * @param p        player
     * @param location location
     * @param range    range
     * @return boolean
     */
    private static boolean isInRange(final Player p, final Location location, final int range) {
        return p.getWorld().equals(location.getWorld()) && p.getLocation().distanceSquared(location) < Math.pow(range, 2);
    }

    /**
     * Emit battle zone entering events for all current players
     *
     * @param currentPlayers  set of players currently in the zone
     * @param battleSide      battle side
     * @param previousPlayers set of players previously in the zone
     * @param war             war
     * @param battle          battle
     */
    private static void emitEntering(Set<Player> currentPlayers, Set<Player> previousPlayers, BattleSide battleSide, War war, Battle battle) {
        currentPlayers.stream()
            .filter(p -> !previousPlayers.contains(p)) // Do not emit for players still inside the zone
            .collect(Collectors.toSet())
            .forEach(p -> new PlayerEnteredBattlefieldEvent(p, war, battle, battleSide).callEvent());
    }

    /**
     * Emit battle zone leaving events for all previous players
     *
     * @param currentPlayers  set of players currently in the zone
     * @param battleSide      battle side
     * @param previousPlayers set of players previously in the zone
     * @param war             war
     * @param battle          battle
     */
    private static void emitLeaving(Set<Player> currentPlayers, Set<Player> previousPlayers, BattleSide battleSide, War war, Battle battle) {
        previousPlayers.stream()
            .filter(p -> p.isConnected() && !currentPlayers.contains(p)) // Do not emit for players still inside the zone
            .collect(Collectors.toSet())
            .forEach(p -> new PlayerLeftBattlefieldEvent(p, war, battle, battleSide).callEvent());
    }
}