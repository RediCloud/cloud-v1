package net.suqatri.redicloud.api.scheduler;

import java.util.concurrent.TimeUnit;

public interface IScheduler<S extends ISchedulerTask, R extends IRepeatScheduler> {

    void runTaskAsync(Runnable runnable);
    void runTaskLaterAsync(Runnable runnable, long d, TimeUnit timeUnit);
    R scheduleTaskAsync(Runnable runnable, long period, long interval, TimeUnit timeUnit);

    void runTask(Runnable runnable);
    void runTaskLater(Runnable runnable, long d, TimeUnit timeUnit);
    R scheduleTask(Runnable runnable, long period, long interval, TimeUnit timeUnit);

    boolean isMainThread();

    Runnable createRepeatTask(IRepeatScheduler schedulerTask, Runnable runnable);

}
