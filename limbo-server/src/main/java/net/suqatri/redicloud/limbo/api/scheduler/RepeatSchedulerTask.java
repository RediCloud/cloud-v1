package net.suqatri.redicloud.limbo.api.scheduler;

import lombok.Getter;
import lombok.Setter;
import net.suqatri.redicloud.api.scheduler.IRepeatScheduler;
import net.suqatri.redicloud.api.scheduler.ITaskFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

@Getter
@Setter
public class RepeatSchedulerTask implements IRepeatScheduler<ScheduledFuture> {

    private final List<ITaskFilter> filters;
    private final Scheduler scheduler;
    private boolean asyncFilter;
    private ScheduledFuture task;
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
