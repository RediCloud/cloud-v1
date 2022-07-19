package minecraft.scheduler;

import lombok.Getter;
import lombok.Setter;
import net.suqatri.cloud.api.scheduler.IRepeatScheduler;
import net.suqatri.cloud.api.scheduler.IScheduler;
import net.suqatri.cloud.api.scheduler.ITaskFilter;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class BukkitRepeatSchedulerTask implements IRepeatScheduler<BukkitTask> {

    private final List<ITaskFilter> filters;
    private boolean asyncFilter;
    private BukkitTask task;
    private BukkitScheduler scheduler;
    private int id;

    public BukkitRepeatSchedulerTask(BukkitScheduler scheduler) {
        this.scheduler = scheduler;
        this.filters = new ArrayList<>();
        this.id = -1;
    }

    @Override
    public void setTask(BukkitTask task) {
        this.task = task;
        this.id = task.getTaskId();
    }

    @Override
    public void cancel() {
        if(this.task == null) return;
        this.task.cancel();
    }

}
