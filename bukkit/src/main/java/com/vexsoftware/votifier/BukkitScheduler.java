package com.vexsoftware.votifier;

import com.vexsoftware.votifier.platform.scheduler.ScheduledVotifierTask;
import com.vexsoftware.votifier.platform.scheduler.VotifierScheduler;
import space.arim.morepaperlib.scheduling.GracefulScheduling;
import space.arim.morepaperlib.scheduling.ScheduledTask;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

// Uses MorePaperLib to ensure compatibility with Folia, Paper, and Paper forks
class BukkitScheduler implements VotifierScheduler {
    private final GracefulScheduling scheduling;

    public BukkitScheduler(GracefulScheduling scheduling) {
        this.scheduling = scheduling;
    }

    @Override
    public ScheduledVotifierTask delayedOnPool(Runnable runnable, int delay, TimeUnit unit) {
        Duration duration = Duration.ofMillis(unit.toMillis(delay));
        return new MorePaperLibTaskWrapper(scheduling.asyncScheduler().runDelayed(runnable, duration));
    }

    @Override
    public ScheduledVotifierTask repeatOnPool(Runnable runnable, int delay, int repeat, TimeUnit unit) {
        Duration initialDelay = Duration.ofMillis(unit.toMillis(delay));
        Duration period = Duration.ofMillis(unit.toMillis(repeat));
        return new MorePaperLibTaskWrapper(scheduling.asyncScheduler().runAtFixedRate(runnable, initialDelay, period));
    }

    private static class MorePaperLibTaskWrapper implements ScheduledVotifierTask {
        private final ScheduledTask task;

        private MorePaperLibTaskWrapper(ScheduledTask task) {
            this.task = task;
        }

        @Override
        public void cancel() {
            task.cancel();
        }
    }
}
