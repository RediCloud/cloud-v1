package dev.redicloud.plugin.bungeecord.scheduler;

import lombok.Data;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import dev.redicloud.api.scheduler.ISchedulerTask;

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
        if (task == null) return;
        this.task.cancel();
    }
}
