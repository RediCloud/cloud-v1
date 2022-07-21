package net.suqatri.cloud.node.scheduler;

import net.suqatri.cloud.api.scheduler.IRepeatScheduler;
import net.suqatri.cloud.api.scheduler.IScheduler;

import java.util.concurrent.*;
import java.util.function.Consumer;

public class Scheduler implements IScheduler<SchedulerTask, RepeatSchedulerTask> {

    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);
    private final ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);

    @Override
    public void runTaskAsync(Runnable runnable) {
        this.executorService.execute(runnable);
    }

    @Override
    public void runTaskLaterAsync(Runnable runnable, long d, TimeUnit timeUnit) {
        this.scheduledExecutorService.schedule(runnable, d, timeUnit);
    }

    @Override
    public RepeatSchedulerTask scheduleTaskAsync(Runnable runnable, long period, long interval, TimeUnit timeUnit) {
        RepeatSchedulerTask task = new RepeatSchedulerTask(this);
        ScheduledFuture scheduledFuture = this.scheduledExecutorService.scheduleAtFixedRate(createRepeatTask(task, runnable), period, interval, timeUnit);
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
    public RepeatSchedulerTask scheduleTask(Runnable runnable, long period, long interval, TimeUnit timeUnit) {
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
