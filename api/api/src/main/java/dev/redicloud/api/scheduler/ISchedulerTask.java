package dev.redicloud.api.scheduler;

public interface ISchedulerTask<T> {

    T getTask();

    void setTask(T task);

    void setId(int id);

    void cancel();

    IScheduler getScheduler();

}
