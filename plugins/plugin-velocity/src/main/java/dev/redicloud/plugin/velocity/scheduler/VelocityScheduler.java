package dev.redicloud.plugin.velocity.scheduler;

import com.velocitypowered.api.scheduler.ScheduledTask;
import dev.redicloud.plugin.velocity.VelocityCloudAPI;
import dev.redicloud.plugin.velocity.VelocityCloudPlugin;
import lombok.AllArgsConstructor;
import dev.redicloud.api.scheduler.IRepeatScheduler;
import dev.redicloud.api.scheduler.IScheduler;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@AllArgsConstructor
public class VelocityScheduler implements IScheduler<VelocitySchedulerTask, VelocityRepeatSchedulerTask> {

    private final VelocityCloudPlugin plugin;

    @Override
    public void runTaskAsync(Runnable runnable) {
        VelocityCloudAPI.getInstance().getProxyServer().getScheduler()
                .buildTask(plugin, runnable).schedule();
    }

    @Override
    public void runTaskLaterAsync(Runnable runnable, long d, TimeUnit timeUnit) {
        VelocityCloudAPI.getInstance().getProxyServer().getScheduler()
                .buildTask(plugin, runnable).delay(d, timeUnit).schedule();
    }

    @Override
    public VelocityRepeatSchedulerTask scheduleTaskAsync(Runnable runnable, long period, long interval, TimeUnit timeUnit) {
        VelocityRepeatSchedulerTask task = new VelocityRepeatSchedulerTask(this);
        ScheduledTask scheduledTask = VelocityCloudAPI.getInstance().getProxyServer().getScheduler()
                .buildTask(plugin, createRepeatTask(task, runnable)).repeat(interval, timeUnit).schedule();
        task.setTask(scheduledTask);
        return task;
    }

    @Override
    public void runTask(Runnable runnable) {
        this.runTaskAsync(runnable);
    }

    @Override
    public void runTaskLater(Runnable runnable, long d, TimeUnit timeUnit) {
        this.runTaskLaterAsync(runnable, d, timeUnit);
    }

    @Override
    public VelocityRepeatSchedulerTask scheduleTask(Runnable runnable, long period, long interval, TimeUnit timeUnit) {
        return this.scheduleTaskAsync(runnable, period, interval, timeUnit);
    }

    @Override
    public boolean isMainThread() {
        return false;
    }

    @Override
    public Runnable createRepeatTask(IRepeatScheduler schedulerTask, Runnable runnable) {
        Runnable task = () -> {
            if ((schedulerTask.isAsyncFilter() && !isMainThread()) || !schedulerTask.isAsyncFilter()) {
                if (!schedulerTask.filters()) {
                    schedulerTask.cancel();
                    return;
                }
                runnable.run();
            } else {
                schedulerTask.filters((Consumer<Boolean>) filterState -> {
                    if (!filterState) {
                        schedulerTask.cancel();
                        return;
                    }
                    runnable.run();
                });
            }
        };
        return task;
    }
}
