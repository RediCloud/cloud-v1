package net.suqatri.cloud.node.scheduler;

import lombok.Getter;
import lombok.Setter;
import net.suqatri.cloud.api.scheduler.IRepeatScheduler;
import net.suqatri.cloud.api.scheduler.ITaskFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

@Getter
@Setter
public class RepeatSchedulerTask implements IRepeatScheduler<ScheduledFuture> {

    private final List<ITaskFilter> filters;
    private boolean asyncFilter;
    private ScheduledFuture task;
    private final Scheduler scheduler;
    private int id;

    public RepeatSchedulerTask(Scheduler scheduler) {
        this.scheduler = scheduler;
        this.filters = new ArrayList<>();
        this.id = -1;
    }

    @Override
    public void cancel() {
        this.task.cancel(true);
    }
}
