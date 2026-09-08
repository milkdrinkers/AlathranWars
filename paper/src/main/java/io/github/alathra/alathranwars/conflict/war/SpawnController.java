package io.github.alathra.alathranwars.conflict.war;

import io.github.alathra.alathranwars.AbstractAlathranWars;
import io.github.alathra.alathranwars.AlathranWars;
import io.github.alathra.alathranwars.Reloadable;
import io.github.alathra.alathranwars.conflict.war.side.SideImpl;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.ApiStatus;
import space.arim.morepaperlib.scheduling.ScheduledTask;

/**
 * The singleton Spawn controller.
 */
public class SpawnController implements Reloadable {
    private static SpawnController instance;

    private SpawnController() {
        if (instance != null)
            Bukkit.getServer().getLogger().warning("Tried to re-initialize singleton");
    }

    /**
     * Gets or creates an instance of the spawn controller.
     *
     * @return the instance
     */
    public static SpawnController getInstance() {
        if (instance == null)
            instance = new SpawnController();

        return instance;
    }

    /**
     * Load all data into memory from database.
     */
    @ApiStatus.Internal
    public void loadAll() {

    }

    private ScheduledTask task;

    @Override
    public void onEnable(AbstractAlathranWars plugin) {
        task = AlathranWars.getInstance()
            .getPaperLib()
            .scheduling()
            .globalRegionalScheduler()
            .runAtFixedRate(() -> WarController.getInstance()
                .getWars()
                .forEach(war -> {
                    if (war.isWarTime()) {
                        war.getSides().forEach(side -> ((SideImpl) side).update());
                    }
                }), 1L, 1);
    }

    @Override
    public void onDisable(AbstractAlathranWars plugin) {
        if (!task.isCancelled())
            task.cancel();
    }
}
