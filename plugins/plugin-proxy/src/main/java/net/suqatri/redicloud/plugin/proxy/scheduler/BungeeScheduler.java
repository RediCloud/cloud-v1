package net.suqatri.redicloud.plugin.proxy.scheduler;

import lombok.AllArgsConstructor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.suqatri.redicloud.api.scheduler.IRepeatScheduler;
import net.suqatri.redicloud.api.scheduler.IScheduler;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@AllArgsConstructor
public class BungeeScheduler implements IScheduler<BungeeSchedulerTask, BungeeRepeatSchedulerTask> {

    private final Plugin plugin;

    @Override
    public void runTaskAsync(Runnable runnable) {
        ProxyServer.getInstance().getScheduler().runAsync(plugin, runnable);
    }

    @Override
    public void runTaskLaterAsync(Runnable runnable, long d, TimeUnit timeUnit) {
        ProxyServer.getInstance().getScheduler().schedule(plugin, runnable, d, timeUnit);
    }

    @Override
    public BungeeRepeatSchedulerTask scheduleTaskAsync(Runnable runnable, long period, long interval, TimeUnit timeUnit) {
        BungeeRepeatSchedulerTask task = new BungeeRepeatSchedulerTask(this);
        ScheduledTask scheduledFuture = ProxyServer.getInstance().getScheduler().schedule(this.plugin, createRepeatTask(task, runnable), period, interval, timeUnit);
        task.setTask(scheduledFuture);
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
    public BungeeRepeatSchedulerTask scheduleTask(Runnable runnable, long period, long interval, TimeUnit timeUnit) {
        return this.scheduleTaskAsync(runnable, period, interval, timeUnit);
    }

    @Override
    public boolean isMainThread() {
        return false;
    }

    @Override
    public Runnable createRepeatTask(IRepeatScheduler schedulerTask, Runnable runnable) {
        Runnable task = () -> {
            if((schedulerTask.isAsyncFilter() && !isMainThread()) || !schedulerTask.isAsyncFilter()){
                if(!schedulerTask.filters()) {
                    schedulerTask.cancel();
                    return;
                }
                runnable.run();
            }else {
                schedulerTask.filters((Consumer<Boolean>) filterState -> {
                    if(!filterState){
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
