package net.suqatri.redicloud.commons.function.future;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class FutureAction<V> extends CompletableFuture<V> {

    public FutureAction() {}

    public FutureAction(Throwable t){
        this.completeExceptionally(t);
    }

    public FutureAction(V value) {
        this.complete(value);
    }

    public FutureAction(CompletionStage<V> future){
        future.whenComplete((v, t) -> {
            if(t != null) {
                this.completeExceptionally(t);
            } else {
                this.complete(v);
            }
        });
    }

    public V getBlockOrNull(){
        try {
            return this.get();
        } catch (Exception e) {
            return null;
        }
    }

    public FutureAction<V> orTimeout(long timeout, TimeUnit unit) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if(!isDone() && !isCancelled() && !isCompletedExceptionally()) {
                    completeExceptionally(new TimeoutException());
                }
            }
        }, unit.toMillis(timeout));
        return this;
    }

    public FutureAction<V> onFailure(FutureAction futureAction) {
        this.whenComplete((v, t) -> {
            if(t != null) {
                futureAction.completeExceptionally(t);
            }
        });
        return this;
    }

    public <R> FutureAction<R> map(FutureMapper<V, R> mapper, Class<R> clazz) {
        FutureAction<R> future = new FutureAction<>();
        this.whenComplete((v, t) -> {
            if(t != null) {
                this.error = t;
                future.completeExceptionally(t);
            } else {
                R value = mapper.get(v);
                if(value instanceof Throwable) {
                    future.completeExceptionally((Throwable) value);
                    return;
                }
                future.complete(value);
            }
        });
        return future;
    }

    public <R> FutureAction<R> map(FutureMapper<V, R> mapper) {
        FutureAction<R> future = new FutureAction<>();
        this.whenComplete((v, t) -> {
            if(t != null) {
                future.completeExceptionally(t);
            } else {
                R value = mapper.get(v);
                future.complete(value);
            }
        });
        return future;
    }

    public FutureAction<V> onSuccess(Consumer<V> consumer){
        this.whenComplete((v, t) -> {
            if(t == null) {
                consumer.accept(v);
            }
        });
        return this;
    }

    public FutureAction<V> onFailure(Consumer<Exception> consumer){
        this.whenComplete((v, t) -> {
            if(t != null) {
                consumer.accept((Exception) t);
            }
        });
        return this;
    }

}
