package net.suqatri.cloud.api.scheduler;

import java.util.List;
import java.util.function.Consumer;

public interface IRepeatScheduler<T> extends ISchedulerTask<T>{

    List<ITaskFilter> getFilters();
    boolean isAsyncFilter();
    default void addFilter(ITaskFilter filter) {
        getFilters().add(filter);
    }
    default void removeFilter(ITaskFilter filter) {
        getFilters().remove(filter);
    }
    default boolean filters() {
        if(this.getFilters() == null) return true;
        for (ITaskFilter taskFilter : this.getFilters()) {
            if(!taskFilter.filter()) return false;
        }
        return true;
    }

    default void filters(Consumer<Boolean> consumer){
        if(this.getFilters() != null) {
            this.getScheduler().runTaskAsync(() -> {
                for (ITaskFilter taskFilter : this.getFilters()) {
                    if(!taskFilter.filter()) {
                        consumer.accept(false);
                        return;
                    }
                }
            });
        }
        consumer.accept(true);
    }

}
