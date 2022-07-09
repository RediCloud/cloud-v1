package net.suqatri.cloud.node.scheduler;

import lombok.Setter;
import net.suqatri.cloud.api.scheduler.IRepeatScheduler;
import net.suqatri.cloud.api.scheduler.IScheduler;
import net.suqatri.cloud.api.scheduler.ITaskFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

public class RepeatSchedulerTask implements IRepeatScheduler<ScheduledFuture> {

    private final List<ITaskFilter> filters;
    @Setter
    private boolean asyncFilter;
    private ScheduledFuture task;
    private Scheduler scheduler;
    private int taskId;

    public RepeatSchedulerTask(Scheduler scheduler) {
        this.scheduler = scheduler;
        this.filters = new ArrayList<>();
        this.taskId = -1;
    }

    @Override
    public List<ITaskFilter> getFilters() {
        return this.filters;
    }

    @Override
    public boolean isAsyncFilter() {
        return this.asyncFilter;
    }

    @Override
    public void setTask(ScheduledFuture task) {
        this.task = task;
    }

    @Override
    public ScheduledFuture getTask() {
        return this.task;
    }

    @Override
    public void setId(int id) {
        this.taskId = id;
    }

    @Override
    public void cancel() {
        this.task.cancel(true);
    }

    @Override
    public IScheduler getScheduler() {
        return this.scheduler;
    }
}
