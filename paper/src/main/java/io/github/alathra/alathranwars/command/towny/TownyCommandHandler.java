package io.github.alathra.alathranwars.command.towny;

import io.github.alathra.alathranwars.AbstractAlathranWars;
import io.github.alathra.alathranwars.Reloadable;

public class TownyCommandHandler implements Reloadable {
    @Override
    public void onLoad(AbstractAlathranWars plugin) {

    }

    @Override
    public void onEnable(AbstractAlathranWars plugin) {
        new TownAdminCommands();
        new TownCommands();
    }

    @Override
    public void onDisable(AbstractAlathranWars plugin) {

    }
}
