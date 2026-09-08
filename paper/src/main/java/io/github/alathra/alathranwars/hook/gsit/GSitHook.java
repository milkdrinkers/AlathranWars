package io.github.alathra.alathranwars.hook.gsit;

import dev.geco.gsit.api.event.PreEntitySitEvent;
import io.github.alathra.alathranwars.AbstractAlathranWars;
import io.github.alathra.alathranwars.AlathranWars;
import io.github.alathra.alathranwars.conflict.war.WarController;
import io.github.alathra.alathranwars.hook.AbstractHook;
import io.github.alathra.alathranwars.hook.Hook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class GSitHook extends AbstractHook implements Listener {
    public GSitHook(AlathranWars plugin) {
        super(plugin);
    }

    @Override
    public void onLoad(AbstractAlathranWars plugin) {
        if (!isPluginPresent(Hook.GSit.getPluginName())) {
        }
    }

    @Override
    public void onEnable(AbstractAlathranWars plugin) {
        if (!isPluginEnabled(Hook.GSit.getPluginName())) {
        }
    }

    @Override
    public void onDisable(AbstractAlathranWars plugin) {
        if (!isPluginEnabled(Hook.GSit.getPluginName())) {
        }
    }

    @Override
    public boolean isHookLoaded() {
        return isPluginPresent(Hook.GSit.getPluginName());
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onSit(PreEntitySitEvent e) {
        if (!(e.getEntity() instanceof Player p))
            return;

        if (WarController.getInstance().isInActiveWar(p))
            e.setCancelled(true);
    }
}
