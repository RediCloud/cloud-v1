package net.suqatri.redicloud.api.scheduler;

public interface ISchedulerTask<T> {

    void setTask(T task);
    T getTask();
    void setId(int id);
    void cancel();
    IScheduler getScheduler();

}
