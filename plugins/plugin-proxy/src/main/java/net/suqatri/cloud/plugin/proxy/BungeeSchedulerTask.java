package net.suqatri.cloud.plugin.proxy;

import lombok.Data;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.suqatri.cloud.api.scheduler.ISchedulerTask;

@Data
public class BungeeSchedulerTask implements ISchedulerTask<ScheduledTask> {

    private final BungeeScheduler scheduler;
    private ScheduledTask task;
    private int id;

    @Override
    public void setTask(ScheduledTask task) {
        this.task = task;
        this.id = task.getId();
    }

    @Override
    public void cancel() {
        if(task == null) return;
        this.task.cancel();
    }
}
