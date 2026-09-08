package io.github.alathra.alathranwars.listener.battle.siege;

import com.palmergames.bukkit.towny.event.actions.TownyBuildEvent;
import com.palmergames.bukkit.towny.event.actions.TownyDestroyEvent;
import io.github.alathra.alathranwars.conflict.battle.Battle;
import io.github.alathra.alathranwars.conflict.battle.siege.Siege;
import io.github.alathra.alathranwars.conflict.battle.siege.SiegeImpl;
import io.github.alathra.alathranwars.utility.BattleUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.translation.Argument;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Set;

public class BlockBreakPlaceListener implements Listener {
    private final static Set<Material> DESTROY_SPECIFIC = Set.of(
    );

    private final static Set<Material> DESTROY_GLOBAL = Set.of(
        // Siege engines
        Material.ARMOR_STAND
    );

    private final static Set<Material> PLACE_NEUTRAL = Set.of(
        // Siege engines
        Material.CARVED_PUMPKIN
    );

    private final static Set<Material> PLACE_OWNED = Set.of(
    );

    private final static Set<Material> PLACE_GLOBAL = Set.of(
    );

    @EventHandler
    public void onBlockBreak(TownyDestroyEvent e) {
        final Player p = e.getPlayer();
        final Battle battle = BattleUtils.getClosestBattle(p.getLocation(), 2000, b -> {
            if (b instanceof Siege siege) {
                return siege.isInBattle(p);
            }
            return false;
        }).orElse(null);

        if (!(battle instanceof Siege siege))
            return;

        if (!BattleUtils.isOnBattlefield(p, siege))
            return;

        final boolean isInsideTownClaims = e.hasTownBlock();
        final boolean isDefender = siege.isDefender(e.getPlayer());

        // Allow destroying beds anywhere
        if (DESTROY_GLOBAL.contains(e.getMaterial())) {
            e.setCancelled(false);
            return;
        }

        // Allow destroying outside claims
        if (
            ((isDefender || !isInsideTownClaims) && DESTROY_SPECIFIC.contains(e.getMaterial()))
        ) {
            e.setCancelled(false);
            return;
        }

        if (e.getPlayer().hasPermission("AlathranWars.break")) {
            e.setCancelled(false);
            return;
        }

        final double msgDist = Math.sqrt(p.getLocation().distanceSquared(siege.getControlPoint()));

        e.setCancelled(true);
        e.setCancelMessage("");
        e.getPlayer().sendMessage(
            Component.translatable(
                "alathranwars.battle.siege.building.destroy",
                Argument.numeric("distance", msgDist),
                Argument.numeric("range", SiegeImpl.BATTLEFIELD_RANGE)
            )
        );
    }

    @EventHandler
    public void onBlockPlace(TownyBuildEvent e) {
        final Player p = e.getPlayer();
        final Battle battle = BattleUtils.getClosestBattle(p.getLocation(), 2000, b -> {
            if (b instanceof Siege siege) {
                return siege.isInBattle(p);
            }
            return false;
        }).orElse(null);

        if (!(battle instanceof Siege siege))
            return;

        if (!BattleUtils.isOnBattlefield(p, siege))
            return;

        final boolean isInsideClaims = e.hasTownBlock();
        final boolean isDefender = siege.isDefender(e.getPlayer());

        final boolean isDefenderAllowed = (!isInsideClaims || !e.getTownBlock().isOutpost()) && isDefender; // Defenders can build on the battlefield and in towns
        final boolean isAttackerAllowed = (!isInsideClaims || e.getTownBlock().isOutpost()) && !isDefender; // Attackers can build on the battlefield and in outposts

        // Allow placing outside claims and in claims your side owns
        if ((isAttackerAllowed || isDefenderAllowed) && PLACE_NEUTRAL.contains(e.getMaterial())) {
            e.setCancelled(false);
            return;
        }

        // Allow placing beds in claims your side owns
        if (isInsideClaims && (isAttackerAllowed || isDefenderAllowed) && PLACE_OWNED.contains(e.getMaterial())) {
            e.setCancelled(false);
            return;
        }

        // Allow placing everywhere
        if (PLACE_GLOBAL.contains(e.getMaterial())) {
            e.setCancelled(false);
            return;
        }

        if (e.getPlayer().hasPermission("AlathranWars.place")) {
            e.setCancelled(false);
            return;
        }

        final double msgDist = Math.sqrt(p.getLocation().distanceSquared(siege.getControlPoint()));

        e.setCancelled(true);
        e.setCancelMessage("");
        e.getPlayer().sendMessage(
            Component.translatable(
                "alathranwars.battle.siege.building.place",
                Argument.numeric("distance", msgDist),
                Argument.numeric("range", SiegeImpl.BATTLEFIELD_RANGE)
            )
        );
    }
}
