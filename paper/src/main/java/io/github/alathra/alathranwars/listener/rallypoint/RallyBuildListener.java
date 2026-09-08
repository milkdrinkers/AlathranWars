package io.github.alathra.alathranwars.listener.rallypoint;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.actions.TownyBuildEvent;
import com.palmergames.bukkit.towny.event.actions.TownyDestroyEvent;
import com.palmergames.bukkit.towny.object.Resident;
import io.github.alathra.alathranwars.conflict.war.WarController;
import io.github.alathra.alathranwars.conflict.war.side.Side;
import io.github.alathra.alathranwars.conflict.war.side.spawn.*;
import io.github.alathra.alathranwars.event.spawn.RallyPlaceEvent;
import io.github.alathra.alathranwars.utility.BattleUtils;
import io.github.alathra.alathranwars.utility.Cfg;
import io.github.alathra.alathranwars.utility.SideUtils;
import io.github.alathra.alathranwars.utility.SpawnUtils;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import io.github.milkdrinkers.wordweaver.Translation;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

public class RallyBuildListener implements Listener {
    public static Key KEY_WAR_BANNER_COOLDOWN = Key.key("alathra", "war_banner");

    public static boolean hasBannerCooldown(Player player) {
        return player.getCooldown(KEY_WAR_BANNER_COOLDOWN) != 0;
    }

    public static Duration getBannerCooldown(Player player) {
        return Duration.ofSeconds(player.getCooldown(KEY_WAR_BANNER_COOLDOWN) / 50);
    }

    public static void setBannerCooldown(Player player) {
        player.setCooldown(KEY_WAR_BANNER_COOLDOWN, ((int) Duration.ofMinutes(Cfg.get().respawns.rallies.placementCooldown).toSeconds()) * 50);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRallyGriefDestroy(TownyDestroyEvent e) {
        if (!isValidBanner(e.getBlock()))
            return;

        final Optional<Spawn> spawnOptional = SpawnUtils.getSpawns().stream()
            .filter(s -> s.getType().equals(SpawnType.RALLY))
            .filter(s -> s.getLocation().getWorld().equals(e.getLocation().getWorld()))
            .reduce((spawn1, spawn2) -> {
                final Location loc1 = spawn1.getLocation();
                final double dist1 = loc1.distanceSquared(e.getLocation());

                final Location loc2 = spawn2.getLocation();
                final double dist2 = loc2.distanceSquared(e.getLocation());

                return dist1 < dist2 ? spawn1 : spawn2;
            });

        if (spawnOptional.isEmpty())
            return;

        final Spawn spawn = spawnOptional.get();
        if (spawn.withinBoundingBox(e.getLocation())) {
            e.setCancelled(true);
            e.setCancelMessage("<red>You cannot build around a rally point.");
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRallyGriefBuild(TownyBuildEvent e) {
        if (!isValidBanner(e.getBlock()))
            return;

        final Optional<Spawn> spawnOptional = SpawnUtils.getSpawns().stream()
            .filter(s -> s.getType().equals(SpawnType.RALLY))
            .filter(s -> s.getLocation().getWorld().equals(e.getLocation().getWorld()))
            .reduce((spawn1, spawn2) -> {
                final Location loc1 = spawn1.getLocation();
                final double dist1 = loc1.distanceSquared(e.getLocation());

                final Location loc2 = spawn2.getLocation();
                final double dist2 = loc2.distanceSquared(e.getLocation());

                return dist1 < dist2 ? spawn1 : spawn2;
            });

        if (spawnOptional.isEmpty())
            return;

        final Spawn spawn = spawnOptional.get();
        if (spawn.withinBoundingBox(e.getLocation())) {
            e.setCancelled(true);
            e.setCancelMessage("<red>You cannot build around a rally point.");
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRallyPlace(TownyBuildEvent e) {
        // Check if placing rally
        if (!isValidBanner(e.getBlock()))
            return;

        final Player p = e.getPlayer();
        final Resident res = TownyAPI.getInstance().getResident(p);
        final Location location = e.getLocation();

        if (!WarController.getInstance().isInActiveWar(p))
            return;

        if (res == null)
            return;

        try {
            if (!BattleUtils.isCaptain(p) && !res.isAdmin())
                throw new SpawnCreationException(Component.translatable("alathranwars.rally.placement.not-captain"));

            if (e.hasTownBlock())
                throw new SpawnCreationException(Component.translatable("alathranwars.rally.placement.inside-claim"));

            // Check for item placement cooldown
            if (hasBannerCooldown(p))
                throw new SpawnCreationException(Component.empty());

            // Get side that this rally will be placed for
            final Optional<Side> sideOptional = SpawnUtils.calculateSpawnSide(location, p);
            if (sideOptional.isEmpty())
                throw new SpawnCreationException(Component.empty());

            final Side side = SideUtils.getOpponent(sideOptional.get());

            // Check if valid placement position (Throws if invalid)
            SpawnUtils.isValidSpawnPlacement(location, side);

            // Apply cooldown to player
            setBannerCooldown(p);

            // Rally point
            final Spawn spawn = new SpawnBuilder()
                .setName("Rally point ")
                .setLocation(location)
                .setType(SpawnType.RALLY)
                .setSide(side)
                .setBlock(e.getBlock())
                .setCreator(p)
                .build();
            side.addSpawn(spawn);
            new RallyPlaceEvent((RallyPoint) spawn).callEvent();
            e.setCancelled(false);

            // Viewer scoped: .papi(p)/.mini(p) need the player. The GlobalTranslator bridge is handed
            // a locale, never a viewer, so this cannot become a translatable.
            Audience.audience(side.getPlayersOnline())
                .sendMessage(ColorParser.of(Translation.of("alathranwars.rally.placement.success-others"))
                    .papi(p)
                    .mini(p)
                    .with("name", spawn.getName())
                    .with("creator", ((RallyPoint) spawn).getCreator().getName())
                    .build()
                );
            // Viewer scoped: see above.
            p.sendMessage(ColorParser.of(Translation.of("alathranwars.rally.placement.success-self"))
                .papi(p)
                .mini(p)
                .with("name", spawn.getName())
                .with("creator", ((RallyPoint) spawn).getCreator().getName())
                .build());

        } catch (SpawnCreationException exception) {
            e.setCancelled(true);
            e.setCancelMessage(MiniMessage.miniMessage().serialize(exception.getComponent()));
        }
    }

    /**
     * Check if this is a valid banner placement
     *
     * @param block banner block
     * @return boolean
     */
    private static boolean isValidBanner(Block block) {
        return isCustomizedBanner(block) /*&& canBuildRally(block)*/;
    }

    private static final List<Material> VALID_MATERIAL = List.of(
        Material.BLACK_BANNER,
        Material.BLUE_BANNER,
        Material.BROWN_BANNER,
        Material.CYAN_BANNER,
        Material.GRAY_BANNER,
        Material.GREEN_BANNER,
        Material.LIGHT_BLUE_BANNER,
        Material.LIGHT_GRAY_BANNER,
        Material.LIME_BANNER,
        Material.MAGENTA_BANNER,
        Material.ORANGE_BANNER,
        Material.PINK_BANNER,
        Material.PURPLE_BANNER,
        Material.RED_BANNER,
        Material.WHITE_BANNER,
        Material.YELLOW_BANNER
    );

    /**
     * Check if block is a banner and is not a default white one
     *
     * @param block block
     * @return true if customized
     */
    private static boolean isCustomizedBanner(Block block) {
        if (!Tag.BANNERS.isTagged(block.getType()) && !Tag.ITEMS_BANNERS.isTagged(block.getType()))
            return false;

        // Check if standing banner
        if (!VALID_MATERIAL.contains(block.getType()))
            return false;

        // Check if customized
        if (block.getState() instanceof Banner banner)
            return banner.numberOfPatterns() > 0 || !banner.getBaseColor().equals(DyeColor.WHITE);

        return false;
    }

    /**
     * Check if this block can be placed on the one under it
     *
     * @param block the block being placed
     * @return boolean
     */
    private static boolean canBuildRally(Block block) {
        return block.getRelative(BlockFace.DOWN).canPlace(block.getBlockData());
    }
}
