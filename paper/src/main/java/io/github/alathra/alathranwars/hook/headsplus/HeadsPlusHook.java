package io.github.alathra.alathranwars.hook.headsplus;

import io.github.alathra.alathranwars.AbstractAlathranWars;
import io.github.alathra.alathranwars.AlathranWars;
import io.github.alathra.alathranwars.conflict.war.WarController;
import io.github.alathra.alathranwars.hook.AbstractHook;
import io.github.alathra.alathranwars.hook.Hook;
import io.github.thatsmusic99.headsplus.api.events.PlayerHeadDropEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class HeadsPlusHook extends AbstractHook implements Listener {
    public HeadsPlusHook(AlathranWars plugin) {
        super(plugin);
    }

    @Override
    public void onLoad(AbstractAlathranWars plugin) {
        if (!isPluginPresent(Hook.HeadsPlus.getPluginName())) {
        }
    }

    @Override
    public void onEnable(AbstractAlathranWars plugin) {
        if (!isPluginEnabled(Hook.HeadsPlus.getPluginName())) {
        }
    }

    @Override
    public void onDisable(AbstractAlathranWars plugin) {
        if (!isPluginEnabled(Hook.HeadsPlus.getPluginName())) {
        }
    }

    @Override
    public boolean isHookLoaded() {
        return isPluginPresent(Hook.HeadsPlus.getPluginName());
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onHeadDrop(PlayerHeadDropEvent e) {
        if (WarController.getInstance().isInActiveWar(e.getDeadPlayer()))
            e.setCancelled(true);
    }
}
