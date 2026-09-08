package io.github.alathra.alathranwars.listener.war;

import com.palmergames.bukkit.towny.event.TownAddResidentEvent;
import com.palmergames.bukkit.towny.event.TownPreAddResidentEvent;
import com.palmergames.bukkit.towny.event.TownPreClaimEvent;
import com.palmergames.bukkit.towny.event.TownPreRenameEvent;
import com.palmergames.bukkit.towny.event.economy.TownPreTransactionEvent;
import com.palmergames.bukkit.towny.event.town.*;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.economy.transaction.TransactionType;
import io.github.alathra.alathranwars.conflict.war.War;
import io.github.alathra.alathranwars.conflict.war.WarController;
import io.github.alathra.alathranwars.conflict.war.side.Side;
import io.github.alathra.alathranwars.hook.NameColorHandler;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.time.Duration;

public class TownListener implements Listener {
    @EventHandler
    public void onRename(TownPreRenameEvent e) {
        Town town = e.getTown();

        if (WarController.getInstance().isInAnyWars(town)) {
            e.setCancelMessage("You can't rename a town while it's in a war.");
            e.setCancelled(true);
        }

        // TODO Allow renaming in war, update some vars pretty much
    }

    @EventHandler
    public void onPlayerPreInvite(TownPreInvitePlayerEvent e) {
        Town town = e.getTown();
        Player p = e.getInvitedResident().getPlayer();

        if (WarController.getInstance().isInAnyWars(town)) {
            e.setCancelMessage("You cannot invite this player as your town is in a war.");
            e.setCancelled(true);
            return;
        }

        if (p == null)
            return;

        if (WarController.getInstance().isInAnyWars(p)) {
            e.setCancelMessage("You cannot invite this player because they are in a war.");
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerPreAdd(TownPreAddResidentEvent e) {
        Town town = e.getTown();
        Player p = e.getResident().getPlayer();

        if (WarController.getInstance().isInAnyWars(town)) {
            e.setCancelMessage("You cannot join this town because it is in a war.");
            e.setCancelled(true);
            return;
        }

        if (p == null)
            return;

        if (WarController.getInstance().isInAnyWars(p)) {
            e.setCancelMessage("You cannot join this town because you are in a war.");
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerAdd(TownAddResidentEvent e) {
        Town town = e.getTown();
        Player p = e.getResident().getPlayer();
        if (p == null) return;

        if (WarController.getInstance().isInAnyWars(town)) {
            final Title warTitle = Title.title(
                Component.translatable("alathranwars.war.banner"),
                Component.translatable("alathranwars.town.at-war-subtitle"),
                Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(500))
            );
            final Sound warSound = Sound.sound(Key.key("entity.wither.spawn"), Sound.Source.VOICE, 0.5f, 1.0F);

            p.showTitle(warTitle);
            p.playSound(warSound);
        }

        NameColorHandler.getInstance().calculatePlayerColors(p);
    }

    @EventHandler
    public void onPlayerLeave(TownLeaveEvent e) {
        Town town = e.getTown();
        Player p = e.getResident().getPlayer();
        if (p == null) return;

        for (War war : WarController.getInstance().getWars(town)) {
            Side side = war.getSide(town);
            if (side == null) continue;
            side.remove(p);
        }

        NameColorHandler.getInstance().calculatePlayerColors(p);
    }

    @EventHandler
    public void onPlayerKick(TownKickEvent e) {
        Town town = e.getTown();
        Player p = e.getKickedResident().getPlayer();
        if (p == null) return;

        for (War war : WarController.getInstance().getWars(town)) {
            Side side = war.getSide(town);
            if (side == null) continue;
            side.remove(p);
        }

        NameColorHandler.getInstance().calculatePlayerColors(p);
    }

    /*@EventHandler
    public void onLeaderChange(TownMayorChangedEvent e) {
        Town town = e.getTown();
        Resident newMayor = e.getNewMayor();
        Resident oldMayor = e.getOldMayor();

        if (e.isKingChange() && e.isNationCapital()) {

        }
    }

    @EventHandler
    public void onLeaderChangeSuccession(TownMayorChosenBySuccessionEvent e) {
        Town town = e.getTown();
        Resident newMayor = e.getNewMayor();
        Resident oldMayor = e.getOldMayor();

        if (e.isKingChange() && e.isNationCapital()) {
        }
    }*/

    @EventHandler
    public void onMerge(TownPreMergeEvent e) {
        Town town = e.getRemainingTown();
        Town town2 = e.getSuccumbingTown();

        if (WarController.getInstance().isInAnyWars(town) || WarController.getInstance().isInAnyWars(town2)) {
            e.setCancelMessage("You can't merge towns while they are in a war.");
            e.setCancelled(true);
        }

        // TODO Allow merging towns in war, literally just ensure that players are added to sieges and sides
    }

    // TODO On town ruin, bow town out of any active wars, if leader of war surrender
    // TODO On town ruin, leave or cancel sieges where the town is present
    // TODO On town ruin, leave or cancel raids where the town is present
    @EventHandler
    public void onRuin(TownRuinedEvent e) {
        Town town = e.getTown();

        for (War war : WarController.getInstance().getWars(town)) {
            Side side = war.getSide(town);
            if (side == null) continue;
            war.unsurrender(town);
            side.remove(town);
            side.processSurrenders();
        }
    }

    @EventHandler
    public void onTransaction(TownPreTransactionEvent e) {
        if (!e.getTransaction().getType().equals(TransactionType.DEPOSIT) && !e.getTransaction().getType().equals(TransactionType.WITHDRAW))
            return;

        if (e.getTransaction().getType().equals(TransactionType.DEPOSIT)) {

        } else if (e.getTransaction().getType().equals(TransactionType.WITHDRAW)) {
            if (WarController.getInstance().isInAnyWars(e.getTown())) {
                e.setCancelMessage("You can't withdraw money from a town while it's in a war.");
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onSpawnMove(TownSetSpawnEvent e) {
        if (WarController.getInstance().isInActiveWar(e.getTown())) {
            e.setCancelMessage("You can't move a town spawn during battle time.");
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onSpawnMove(TownSetOutpostSpawnEvent e) {
        if (WarController.getInstance().isInActiveWar(e.getTown())) {
            e.setCancelMessage("You can't move a town outpost during battle time.");
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onSpawnMove(TownPreSetHomeBlockEvent e) {
        if (WarController.getInstance().isInActiveWar(e.getTown())) {
            e.setCancelMessage("You can't move a home block during battle time.");
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onClaim(TownPreClaimEvent e) {
        if (WarController.getInstance().isInActiveWar(e.getTown())) {
            e.setCancelMessage("You can't modify claims during battle time.");
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onUnClaimCmd(TownPreUnclaimCmdEvent e) {
        if (WarController.getInstance().isInActiveWar(e.getTown())) {
            e.setCancelMessage("You can't modify claims during battle time.");
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onUnClaim(TownPreUnclaimEvent e) {
        if (WarController.getInstance().isInActiveWar(e.getTown())) {
            e.setCancelMessage("You can't modify claims during battle time.");
            e.setCancelled(true);
        }
    }
}
