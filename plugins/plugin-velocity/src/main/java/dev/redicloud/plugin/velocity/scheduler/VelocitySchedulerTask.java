package dev.redicloud.plugin.velocity.scheduler;

import com.velocitypowered.api.scheduler.ScheduledTask;
import lombok.Data;
import dev.redicloud.api.scheduler.ISchedulerTask;

@Data
public class VelocitySchedulerTask implements ISchedulerTask<ScheduledTask> {

    private final VelocityScheduler scheduler;
    private ScheduledTask task;

    @Override
    public void setTask(ScheduledTask task) {
        this.task = task;
    }

    @Override
    public void setId(int id) {}

    @Override
    public void cancel() {
        if (task == null) return;
        this.task.cancel();
    }
}
