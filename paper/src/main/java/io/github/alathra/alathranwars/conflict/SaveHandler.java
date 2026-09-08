package io.github.alathra.alathranwars.conflict;

import io.github.alathra.alathranwars.AbstractAlathranWars;
import io.github.alathra.alathranwars.AlathranWars;
import io.github.alathra.alathranwars.Reloadable;
import io.github.alathra.alathranwars.conflict.persistence.WarQueries;
import space.arim.morepaperlib.scheduling.ScheduledTask;

import java.time.Duration;

public class SaveHandler implements Reloadable {
    private ScheduledTask autoSaveTask = null;

    @Override
    public void onEnable(AbstractAlathranWars plugin) {
        autoSaveTask = AlathranWars.getInstance().getPaperLib().scheduling().asyncScheduler().runAtFixedRate(WarQueries::saveAll, Duration.ofMinutes(10), Duration.ofMinutes(10));
    }

    @Override
    public void onDisable(AbstractAlathranWars plugin) {
        if (autoSaveTask != null && !autoSaveTask.isCancelled())
            autoSaveTask.cancel();
        autoSaveTask = null;
        WarQueries.saveAll();
        AlathranWars.getInstance().getPaperLib().scheduling().cancelGlobalTasks();
    }
}
