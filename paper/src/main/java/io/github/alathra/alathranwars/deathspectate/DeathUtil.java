package io.github.alathra.alathranwars.deathspectate;

import io.github.alathra.alathranwars.AlathranWars;
import io.github.alathra.alathranwars.deathspectate.event.DeathSpectatingEvent;
import io.github.alathra.alathranwars.deathspectate.task.SpectateTask;
import io.github.alathra.alathranwars.utility.Cfg;
import io.github.alathra.alathranwars.utility.Logger;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.time.Duration;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class DeathUtil {
    private final static String PDC_PREFIX = "alathran_wars_death";

    // Death PDC
    public final static NamespacedKey PDC_IS_DEAD = NamespacedKey.fromString(PDC_PREFIX + "_is_dead");
    public final static NamespacedKey PDC_RESPAWN_TIMER = NamespacedKey.fromString(PDC_PREFIX + "_respawn_timer");
    public final static NamespacedKey PDC_RESPAWN_GAMEMODE = NamespacedKey.fromString(PDC_PREFIX + "_respawn_previous_gamemode");
    public final static NamespacedKey PDC_RESPAWN_FLYSPEED = NamespacedKey.fromString(PDC_PREFIX + "_respawn_flyspeed");

    // Respawning PDC
    public final static NamespacedKey PDC_AWAITING_RESPAWN = NamespacedKey.fromString(PDC_PREFIX + "_respawn_needed"); // Exists on a player while they're being respawned

    // Teleporting PDC
    public final static NamespacedKey PDC_TELEPORTING = NamespacedKey.fromString(PDC_PREFIX + "_respawn_teleporting"); // Exists on a player if they're trying to teleport while dead

    public static boolean isSpectating(Player p) {
        return p.getGameMode().equals(GameMode.SPECTATOR) && PDC_IS_DEAD != null &&
            p.getPersistentDataContainer().has(PDC_IS_DEAD, PersistentDataType.BOOLEAN);
    }

    private static void addSpectator(Player p, GameMode gameMode, Duration respawnTime, float flySpeed) throws StartSpectateException {
        final PersistentDataContainer pdc = p.getPersistentDataContainer();

        Objects.requireNonNull(PDC_IS_DEAD, "Is Dead is null");
        Objects.requireNonNull(PDC_RESPAWN_TIMER, "Respawn timer is null");
        Objects.requireNonNull(PDC_RESPAWN_GAMEMODE, "Respawn game mode is null");
        Objects.requireNonNull(PDC_RESPAWN_FLYSPEED, "Respawn fly speed is null");

        pdc.set(PDC_IS_DEAD, PersistentDataType.BOOLEAN, true);
        pdc.set(PDC_RESPAWN_TIMER, PersistentDataType.LONG, respawnTime.getSeconds());
        pdc.set(PDC_RESPAWN_GAMEMODE, PersistentDataType.STRING, gameMode.name());
        pdc.set(PDC_RESPAWN_FLYSPEED, PersistentDataType.FLOAT, flySpeed);
        p.setGameMode(GameMode.SPECTATOR);
        if (p.getGameMode() != GameMode.SPECTATOR) {
            Logger.get().warn(ColorParser.of("Another plugin prevented the player from entering the spectator gamemode!").build());
            throw new StartSpectateException();
        }
        p.setFlySpeed(0.0f);
    }

    private static void removeSpectator(Player p) {
        final AlathranWars plugin = AlathranWars.getInstance();
        final PersistentDataContainer pdc = p.getPersistentDataContainer();

        Objects.requireNonNull(PDC_IS_DEAD, "Is Dead is null");
        Objects.requireNonNull(PDC_RESPAWN_TIMER, "Respawn timer is null");
        Objects.requireNonNull(PDC_RESPAWN_GAMEMODE, "Respawn game mode is null");
        Objects.requireNonNull(PDC_RESPAWN_FLYSPEED, "Respawn fly speed is null");

        final GameMode previousGamemode = GameMode.valueOf(pdc.getOrDefault(PDC_RESPAWN_GAMEMODE, PersistentDataType.STRING, plugin.getServer().getDefaultGameMode().name()));
        p.setGameMode(previousGamemode);

        final float previousFlySpeed = pdc.getOrDefault(PDC_RESPAWN_FLYSPEED, PersistentDataType.FLOAT, 1.0F);
        p.setFlySpeed(previousFlySpeed);

        pdc.remove(PDC_RESPAWN_TIMER);
        pdc.remove(PDC_RESPAWN_GAMEMODE);
        pdc.remove(PDC_RESPAWN_FLYSPEED);
        pdc.remove(PDC_AWAITING_RESPAWN);
        pdc.remove(PDC_TELEPORTING);
        pdc.remove(PDC_IS_DEAD);
    }

    public static void respawnPlayerVanilla(Player p) {
        if (!isSpectating(p) || p.isDead())
            return;

        final PersistentDataContainer pdc = p.getPersistentDataContainer();

        Location spawnLocation = p.getRespawnLocation(true);
        boolean isBedSpawn = true;
        if (spawnLocation == null) {
            spawnLocation = AlathranWars.getInstance().getServer().getWorlds().getFirst().getSpawnLocation();
            isBedSpawn = false;
        }

        pdc.set(PDC_AWAITING_RESPAWN, PersistentDataType.BOOLEAN, true);

        final PlayerRespawnEvent respawnEvent = new PlayerRespawnEvent(p, spawnLocation, isBedSpawn, false, false, PlayerRespawnEvent.RespawnReason.DEATH);
        respawnEvent.setRespawnLocation(spawnLocation);
        respawnEvent.callEvent();

        try {
            final AttributeInstance maxHealth = p.getAttribute(Attribute.MAX_HEALTH);
            if (maxHealth != null)
                p.setHealth(maxHealth.getValue());

            p.setFireTicks(0);
            p.setFallDistance(0);
            p.setFoodLevel(20);
            p.setSaturation(5f);
            p.setExhaustion(0);
            p.setTicksLived(1);
            p.setArrowsInBody(0, false);
            p.clearActivePotionEffects();
            p.closeInventory();
            p.setLastDamageCause(null);
        } catch (Throwable ignored) {
        }

        p.teleportAsync(respawnEvent.getRespawnLocation(), PlayerTeleportEvent.TeleportCause.UNKNOWN)
            .thenAccept(success -> {
                removeSpectator(p);
                pdc.remove(PDC_AWAITING_RESPAWN);
                if (success == false)
                    p.teleport(respawnEvent.getRespawnLocation());
            });
    }

    public static void respawnPlayerWar(Player p, Location location) {
        if (!isSpectating(p) || p.isDead())
            return;

        final PersistentDataContainer pdc = p.getPersistentDataContainer();

        pdc.set(PDC_AWAITING_RESPAWN, PersistentDataType.BOOLEAN, true);

        final PlayerRespawnEvent respawnEvent = new PlayerRespawnEvent(p, location, true, false, false, PlayerRespawnEvent.RespawnReason.DEATH);
        respawnEvent.setRespawnLocation(location);
        respawnEvent.callEvent();

        try {
            final AttributeInstance maxHealth = p.getAttribute(Attribute.MAX_HEALTH);
            if (maxHealth != null)
                p.setHealth(maxHealth.getValue());

            p.setFireTicks(0);
            p.setFallDistance(0);
            p.setFoodLevel(20);
            p.setSaturation(5f);
            p.setExhaustion(0);
            p.setTicksLived(1);
            p.setArrowsInBody(0, false);
            p.clearActivePotionEffects();
            p.closeInventory();
            p.setLastDamageCause(null);
        } catch (Throwable ignored) {
        }

        p.teleportAsync(respawnEvent.getRespawnLocation(), PlayerTeleportEvent.TeleportCause.UNKNOWN)
            .thenAccept(success -> {
                removeSpectator(p);
                pdc.remove(PDC_AWAITING_RESPAWN);
                if (success == false)
                    p.teleport(respawnEvent.getRespawnLocation());
            });
    }

    public static boolean startDeathSpectating(Player player, PlayerDeathEvent deathEvent) {
        if (isSpectating(player))
            return false;

        final AlathranWars plugin = AlathranWars.getInstance();
        final Duration respawnTime = Duration.ofSeconds(Cfg.get().respawns.time);

        try {
            final World world = player.getWorld();

            final boolean keepInventory = deathEvent.getKeepInventory() || player.getGameMode().equals(GameMode.SPECTATOR);
            final boolean showDeathMessages = Boolean.TRUE.equals(world.getGameRuleValue(GameRule.SHOW_DEATH_MESSAGES));

            try {
                addSpectator(player, player.getGameMode(), respawnTime, player.getFlySpeed());
            } catch (StartSpectateException e) {
                return false;
            }

            final Component deathMessage = deathEvent.deathMessage();
            if (deathMessage != null && !deathMessage.compact().equals(Component.empty().compact()) && showDeathMessages)
                plugin.getServer().broadcast(deathMessage);

            // Drop items
            if (!keepInventory) {
                player.getInventory().clear();
                for (ItemStack itemStack : deathEvent.getDrops()) {
                    if (itemStack != null && !itemStack.getType().equals(Material.AIR))
                        world.dropItemNaturally(player.getLocation(), itemStack);
                }

                if (!deathEvent.getKeepLevel()) {
                    player.setTotalExperience(deathEvent.getNewTotalExp());
                    player.setLevel(deathEvent.getNewLevel());
                    player.setExp(deathEvent.getNewExp());
                }
            }

            player.closeInventory();
            player.setSpectatorTarget(null);

            // Drop experience
            if (deathEvent.getDroppedExp() > 0)
                world.spawn(player.getLocation(), ExperienceOrb.class).setExperience(deathEvent.getDroppedExp());

            // Increment/reset death statistics
            player.incrementStatistic(Statistic.DEATHS);
            player.setStatistic(Statistic.TIME_SINCE_DEATH, 0);

            player.clearActivePotionEffects();

            // Play death sound
            final Set<Player> players = new HashSet<>(world.getNearbyPlayers(player.getLocation(), 50));
            players.remove(player);

            final net.kyori.adventure.sound.Sound deathSound = net.kyori.adventure.sound.Sound.sound()
                .type(Key.key("entity.player.death"))
                .source(net.kyori.adventure.sound.Sound.Source.PLAYER)
                .volume(1.0f)
                .pitch(1.0f)
                .build();

            Audience.audience(players).playSound(deathSound, player);

            // Determine killer of the player
            Entity killer = player.getKiller();

            if (player.getKiller() == null && player.getLastDamageCause() instanceof EntityDamageByEntityEvent entity) {
                killer = entity.getDamager();

                if ((killer instanceof Projectile projectile) && (projectile.getShooter() instanceof LivingEntity shooter)) {
                    killer = shooter;
                }
            }

            // Don't count suicide
            if (killer == player)
                killer = null;

            // Stats handling
            if (killer != null) {
                try {
                    player.incrementStatistic(Statistic.ENTITY_KILLED_BY, killer.getType());
                } catch (IllegalArgumentException | NullPointerException ignored) {
                }
            }

            if (killer != null && killer.getType().equals(EntityType.PLAYER)) {
                Player playerKiller = (Player) killer;
                playerKiller.incrementStatistic(Statistic.PLAYER_KILLS);
            }

            final SpectateTask task = new SpectateTask(player, respawnTime);
            new DeathSpectatingEvent(task).callEvent();
            task.runTaskTimer(plugin, 1L, 1L);

            if (!DeathConfig.getYouDiedMessage().isEmpty())
                player.sendMessage(DeathConfig.formatter(DeathConfig.getYouDiedMessage()));

            return true;
        } catch (Exception ignored) {
            removeSpectator(player);
            return false;
        }
    }

    public static boolean resumeDeathSpectating(Player p) {
        if (!isSpectating(p))
            return false;

        final AlathranWars plugin = AlathranWars.getInstance();
        final PersistentDataContainer pdc = p.getPersistentDataContainer();

        // Get previous spectator data from pdc

        Objects.requireNonNull(PDC_RESPAWN_TIMER, "Respawn timer is null");
        Objects.requireNonNull(PDC_RESPAWN_GAMEMODE, "Respawn game mode is null");
        Objects.requireNonNull(PDC_RESPAWN_FLYSPEED, "Respawn fly speed is null");

        final Duration previousRespawnTime = Duration.ofSeconds(pdc.getOrDefault(PDC_RESPAWN_TIMER, PersistentDataType.LONG, 0L));
        final GameMode previousGamemode = GameMode.valueOf(pdc.getOrDefault(PDC_RESPAWN_GAMEMODE, PersistentDataType.STRING, plugin.getServer().getDefaultGameMode().name()));
        final float previousFlySpeed = pdc.getOrDefault(PDC_RESPAWN_FLYSPEED, PersistentDataType.FLOAT, 1.0F);

        try {
            try {
                addSpectator(p, previousGamemode, previousRespawnTime, previousFlySpeed);
            } catch (StartSpectateException e) {
                return false;
            }

            p.closeInventory();
            p.setSpectatorTarget(null);

            final SpectateTask task = new SpectateTask(p, previousRespawnTime);
            new DeathSpectatingEvent(task).callEvent();
            task.runTaskTimer(plugin, 1L, 1L);

            return true;
        } catch (Exception ignored) {
            removeSpectator(p);
            return false;
        }
    }
}
