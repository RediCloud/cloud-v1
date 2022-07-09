package net.suqatri.cloud.node.scheduler;

import lombok.Data;
import net.suqatri.cloud.api.scheduler.IScheduler;
import net.suqatri.cloud.api.scheduler.ISchedulerTask;

import java.util.TimerTask;

@Data
public class SchedulerTask implements ISchedulerTask<TimerTask> {

    private final Scheduler scheduler;
    private TimerTask task;
    private int id;

    @Override
    public void setTask(TimerTask task) {
        this.task = task;
    }

    @Override
    public TimerTask getTask() {
        return this.task;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public void cancel() {
        this.task.cancel();
    }

    @Override
    public IScheduler getScheduler() {
        return this.scheduler;
    }
}
