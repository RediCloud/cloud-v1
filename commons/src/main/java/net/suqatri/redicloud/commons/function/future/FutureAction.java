package net.suqatri.redicloud.commons.function.future;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class FutureAction<V> extends CompletableFuture<V> {

    private V result;
    private Throwable error;

    public FutureAction() {}

    public FutureAction(Throwable t){
        this.completeExceptionally(t);
    }

    public FutureAction(V value) {
        this.result = value;
        this.complete(value);
    }

    public FutureAction(CompletionStage<V> future){
        future.whenComplete((v, t) -> {
            if(t != null) {
                this.error = t;
                this.completeExceptionally(t);
            } else {
                this.result = v;
                this.complete(v);
            }
        });
    }

    public boolean isFinishedAnyway() {
        return this.isDone() || this.isCompletedExceptionally() || this.isCancelled();
    }

    public V getBlockOrNull(){
        try {
            this.result = this.get();
            return this.result;
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
                this.error = t;
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
                this.result = v;
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

    @Override
    public boolean complete(V value) {
        this.result = value;
        return super.complete(value);
    }

    @Override
    public boolean completeExceptionally(Throwable ex) {
        this.error = ex;
        return super.completeExceptionally(ex);
    }

    public <R> FutureAction<R> map(FutureMapper<V, R> mapper) {
        FutureAction<R> future = new FutureAction<>();
        if(this.isFinishedAnyway()){
            if(this.error != null){
                future.completeExceptionally(this.error);
            }else {
                future.complete(mapper.get(this.result));
            }
        }
        this.whenComplete((v, t) -> {
            if(t != null) {
                this.error = t;
                future.completeExceptionally(t);
            } else {
                this.result = v;
                R value = mapper.get(v);
                future.complete(value);
            }
        });
        return future;
    }

    public FutureAction<V> onSuccess(Consumer<V> consumer){
        if(isDone()) {
            consumer.accept(this.result);
            return this;
        }
        this.whenComplete((v, t) -> {
            if(t == null) {
                this.result = v;
                consumer.accept(v);
            }
        });
        return this;
    }

    public FutureAction<V> onFailure(Consumer<Exception> consumer){
        if(isCancelled() || isCompletedExceptionally()){
            consumer.accept((Exception) this.error);
            return this;
        }
        this.whenComplete((v, t) -> {
            if(t != null) {
                this.error = t;
                consumer.accept((Exception) t);
            }
        });
        return this;
    }

}
