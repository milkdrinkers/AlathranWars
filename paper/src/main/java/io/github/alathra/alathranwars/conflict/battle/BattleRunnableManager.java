package io.github.alathra.alathranwars.conflict.battle;

import io.github.alathra.alathranwars.AlathranWars;
import org.jetbrains.annotations.Nullable;
import space.arim.morepaperlib.scheduling.ScheduledTask;

import java.util.ArrayList;
import java.util.List;

public class BattleRunnableManager implements BattleManager {
    private final List<BattleRunnable> battleRunnables = new ArrayList<>();
    private final List<ScheduledTask> scheduledTasks = new ArrayList<>();
    private boolean isRunning = false;

    public BattleRunnableManager() {
    }

    public BattleRunnableManager(List<BattleRunnable> initial) {
        battleRunnables.addAll(initial);
    }

    /**
     * Add a runnable that runs as long as this battle is active.
     *
     * @param runnable a battle runnable
     * @implNote Adding runnables after a {@link BattleRunnableManager} has already executed the {@link #start()} method will have no effect, as the new runnables are never scheduled to run.
     */
    public void add(@Nullable BattleRunnable runnable) {
        if (runnable != null)
            battleRunnables.add(runnable);
    }

    /**
     * Add a runnable that runs as long as this battle is active.
     *
     * @param runnables a battle runnable
     * @implNote Adding runnables after a {@link BattleRunnableManager} has already executed the {@link #start()} method will have no effect, as the new runnables are never scheduled to run.
     */
    public void add(@Nullable BattleRunnable... runnables) {
        for (@Nullable BattleRunnable runnable : runnables) {
            add(runnable);
        }
    }

    /**
     * Start the runnables for this battle.
     */
    @Override
    public void start() {
        if (isRunning)
            return;

        battleRunnables.forEach(runnable -> scheduledTasks.add(
            AlathranWars.getInstance().getPaperLib().scheduling().globalRegionalScheduler().runAtFixedRate(runnable, 0L, runnable.getTickRate())
        ));

        isRunning = true;
    }

    /**
     * Stop the runnables for this battle.
     */
    @Override
    public void stop() {
        if (!isRunning)
            return;

        battleRunnables.forEach(BattleRunnable::stop);
        scheduledTasks.forEach(task -> {
            if (!task.isCancelled())
                task.cancel();
        });
        scheduledTasks.clear();

        isRunning = false;
    }
}
