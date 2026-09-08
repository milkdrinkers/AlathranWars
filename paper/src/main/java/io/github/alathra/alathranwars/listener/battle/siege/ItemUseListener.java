package io.github.alathra.alathranwars.listener.battle.siege;

import com.palmergames.bukkit.towny.event.actions.TownyItemuseEvent;
import io.github.alathra.alathranwars.conflict.war.WarController;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ItemUseListener implements Listener {
    /**
     * Prevent using ender pearls in a combat zone.
     *
     * @param e the e
     */
    @EventHandler
    public void onEnderPearl(TownyItemuseEvent e) {
        if (!WarController.getInstance().isInActiveWar(e.getPlayer())) return;

        if (!e.getMaterial().equals(Material.ENDER_PEARL)) return;

        e.setCancelMessage("You cannot throw ender pearls during a siege!");
        e.setCancelled(true);
    }
}
