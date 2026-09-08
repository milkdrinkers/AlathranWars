package io.github.alathra.alathranwars.listener.battle;

import com.palmergames.bukkit.towny.event.deathprice.NationPaysDeathPriceEvent;
import com.palmergames.bukkit.towny.event.deathprice.PlayerPaysDeathPriceEvent;
import com.palmergames.bukkit.towny.event.deathprice.TownPaysDeathPriceEvent;
import io.github.alathra.alathranwars.conflict.battle.Battle;
import io.github.alathra.alathranwars.conflict.battle.siege.Siege;
import io.github.alathra.alathranwars.conflict.war.WarController;
import io.github.alathra.alathranwars.event.battle.BattleDeathEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.Nullable;

public class PlayerDeathListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPaysDeathPrice(PlayerPaysDeathPriceEvent e) {
        Player p = e.getDeadResident().getPlayer();
        if (p == null)
            return;

        if (WarController.getInstance().isInActiveWar(p))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPaysDeathPrice(TownPaysDeathPriceEvent e) {
        Player p = e.getDeadResident().getPlayer();
        if (p == null)
            return;

        if (WarController.getInstance().isInActiveWar(p))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPaysDeathPrice(NationPaysDeathPriceEvent e) {
        Player p = e.getDeadResident().getPlayer();
        if (p == null)
            return;

        if (WarController.getInstance().isInActiveWar(p))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player victim = e.getPlayer();
        Player attacker = e.getPlayer().getKiller();
        if (attacker == null)
            return;

        if (!WarController.getInstance().isInActiveWar(victim))
            return;

        final Location victimLoc = victim.getLocation();
        final Siege battle = WarController.getInstance().getWars(victim) // Calculate the war in which this kill happened, based on the victim and attacker
            .stream()
            .filter(war -> !war.getPlayersSurrenderedOnline().contains(victim))
            .flatMap(war -> war.getSieges().stream())
            .filter(b -> b.isInBattle(victim) && b.isInBattle(attacker))
            .reduce((b1, b2) -> { // Get nearest battle to victim
                final Location loc1 = b1.getControlPoint();
                final Location loc2 = b2.getControlPoint();

                final double dist1 = loc1.distanceSquared(victimLoc);
                final double dist2 = loc2.distanceSquared(victimLoc);

                return dist1 < dist2 ? b1 : b2;
            }).orElse(null);
        battleKill(e, battle);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerSuicide(PlayerDeathEvent e) {
        Player victim = e.getPlayer();
        Player attacker = e.getPlayer().getKiller();
        if (attacker != null)
            return;

        if (!WarController.getInstance().isInActiveWar(victim))
            return;

        final Location victimLoc = victim.getLocation();
        final Siege battle = WarController.getInstance().getWars(victim) // Calculate the war in which this suicide happened
            .stream()
            .filter(war -> !war.getPlayersSurrenderedOnline().contains(victim))
            .flatMap(war -> war.getSieges().stream())
            .filter(b -> b.isInBattle(victim))
            .reduce((b1, b2) -> { // Get nearest battle to victim
                final Location loc1 = b1.getControlPoint();
                final Location loc2 = b2.getControlPoint();

                final double dist1 = loc1.distanceSquared(victimLoc);
                final double dist2 = loc2.distanceSquared(victimLoc);

                return dist1 < dist2 ? b1 : b2;
            }).orElse(null);
        battleKill(e, battle);
    }

    /**
     *
     * @param e event
     */
    private void battleKill(PlayerDeathEvent e, @Nullable Battle battle) {
        e.setKeepInventory(true);
        e.getDrops().clear();
        e.setKeepLevel(true);
        e.setDroppedExp(0);

        new BattleDeathEvent(e, battle).callEvent();
    }

    /**
     *
     * @param e event
     */
    private void oocKill(PlayerDeathEvent e, @Nullable Battle battle) {
        e.setKeepInventory(true);
        e.getDrops().clear();
        e.setKeepLevel(true);
        e.setDroppedExp(0);

        new BattleDeathEvent(e, battle).callEvent();
    }
}
