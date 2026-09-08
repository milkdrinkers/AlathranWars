package io.github.alathra.alathranwars.conflict.war;

import io.github.alathra.alathranwars.AlathranWars;
import io.github.alathra.alathranwars.event.WarTimeEndedEvent;
import io.github.alathra.alathranwars.event.WarTimeStartedEvent;
import io.github.alathra.alathranwars.utility.Cfg;
import org.jetbrains.annotations.Nullable;
import space.arim.morepaperlib.scheduling.ScheduledTask;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public class WarTimeManager {
    private final War war;
    private Instant scheduledWarTime;
    private ScheduledTask task;
    private ScheduledTask task2;

    public WarTimeManager(War war, Instant scheduledWarTime) {
        this.war = war;
        this.scheduledWarTime = scheduledWarTime;
        ensureTask();
    }

    @Nullable
    protected Instant getScheduledWarTime() {
        return scheduledWarTime;
    }

    protected void setScheduledWarTime(@Nullable Instant scheduledWarTime) {
        this.scheduledWarTime = scheduledWarTime;
        ensureTask();
    }

    private void ensureTask() {
        if (task != null && !task.isCancelled())
            task.cancel();

        if (task2 != null && !task2.isCancelled())
            task2.cancel();

        if (scheduledWarTime != null) {
            final Duration untilWarTime = Duration.between(Instant.now(), getScheduledWarTime()).abs();
            task = AlathranWars.getInstance().getPaperLib().scheduling().asyncScheduler().runDelayed(() -> AlathranWars.getInstance().getPaperLib().scheduling().globalRegionalScheduler().run(() -> {
                new WarTimeStartedEvent(war).callEvent();
            }), untilWarTime);

            final Duration untilWarTimeEnd = Duration.between(Instant.now(), Objects.requireNonNull(getScheduledWarTime()).plus(Duration.ofMinutes(Cfg.get().war.wartimeDuration))).abs();
            task2 = AlathranWars.getInstance().getPaperLib().scheduling().asyncScheduler().runDelayed(() -> AlathranWars.getInstance().getPaperLib().scheduling().globalRegionalScheduler().run(() -> {
                new WarTimeEndedEvent(war).callEvent();
            }), untilWarTimeEnd);
        }
    }
}
