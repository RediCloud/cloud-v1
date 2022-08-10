package dev.redicloud.plugin.velocity.scheduler;

import com.velocitypowered.api.scheduler.ScheduledTask;
import lombok.Getter;
import lombok.Setter;
import dev.redicloud.api.scheduler.IRepeatScheduler;
import dev.redicloud.api.scheduler.ITaskFilter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class VelocityRepeatSchedulerTask implements IRepeatScheduler<ScheduledTask> {

    private final List<ITaskFilter> filters;
    private boolean asyncFilter;
    private ScheduledTask task;
    private VelocityScheduler scheduler;

    public VelocityRepeatSchedulerTask(VelocityScheduler scheduler) {
        this.scheduler = scheduler;
        this.filters = new ArrayList<>();
    }

    @Override
    public void setTask(ScheduledTask task) {
        this.task = task;
    }

    @Override
    public void setId(int id) {}

    @Override
    public void cancel() {
        if (this.task == null) return;
        this.task.cancel();
    }
}
