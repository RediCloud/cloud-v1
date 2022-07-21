package net.suqatri.cloud.plugin.minecraft.scheduler;

import lombok.Data;
import net.suqatri.cloud.api.scheduler.ISchedulerTask;
import org.bukkit.scheduler.BukkitTask;

@Data
public class BukkitSchedulerTask implements ISchedulerTask<BukkitTask> {

    private final BukkitScheduler scheduler;
    private BukkitTask task;
    private int id;

    @Override
    public void setTask(BukkitTask task) {
        this.task = task;
        this.id = task.getTaskId();
    }

    @Override
    public void cancel() {
        if(task == null) return;
        this.task.cancel();
    }
}
