package net.suqatri.redicloud.plugin.minecraft.scheduler;

import lombok.AllArgsConstructor;
import net.suqatri.redicloud.api.scheduler.IRepeatScheduler;
import net.suqatri.redicloud.api.scheduler.IScheduler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@AllArgsConstructor
public class BukkitScheduler implements IScheduler<BukkitSchedulerTask, BukkitRepeatSchedulerTask> {

    private final JavaPlugin javaPlugin;

    @Override
    public void runTaskAsync(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(this.javaPlugin, runnable);
    }

    @Override
    public void runTaskLaterAsync(Runnable runnable, long d, TimeUnit timeUnit) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(this.javaPlugin, runnable, timeUnit.toMillis(d) / 50);
    }

    @Override
    public BukkitRepeatSchedulerTask scheduleTaskAsync(Runnable runnable, long period, long interval, TimeUnit timeUnit) {
        BukkitRepeatSchedulerTask task = new BukkitRepeatSchedulerTask(this);
        BukkitTask scheduledFuture = Bukkit.getScheduler().runTaskTimer(this.javaPlugin, createRepeatTask(task, runnable), period, interval);
        task.setTask(scheduledFuture);
        return task;
    }

    @Override
    public void runTask(Runnable runnable) {
        Bukkit.getScheduler().runTask(this.javaPlugin, runnable);
    }

    @Override
    public void runTaskLater(Runnable runnable, long d, TimeUnit timeUnit) {
        Bukkit.getScheduler().runTaskLater(this.javaPlugin, runnable, timeUnit.toMillis(d) / 50);
    }

    @Override
    public BukkitRepeatSchedulerTask scheduleTask(Runnable runnable, long period, long interval, TimeUnit timeUnit) {
        BukkitRepeatSchedulerTask task = new BukkitRepeatSchedulerTask(this);
        BukkitTask scheduledFuture = Bukkit.getScheduler().runTaskTimerAsynchronously(this.javaPlugin, createRepeatTask(task, runnable), period, interval);
        task.setTask(scheduledFuture);
        return task;
    }

    @Override
    public boolean isMainThread() {
        return Bukkit.isPrimaryThread();
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
