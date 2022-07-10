package net.suqatri.cloud.commons.function.future;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
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

    public FutureAction<V> onFailure(FutureAction futureAction) {
        this.whenComplete((v, t) -> {
            if(t != null) {
                futureAction.completeExceptionally(t);
            }
        });
        return this;
    }

    public <R> FutureAction<R> map(FutureMapper<V, R> mapper) {
        FutureAction<R> future = new FutureAction<>();
        this.whenComplete((v, t) -> {
            if(t != null) {
                future.completeExceptionally(t);
            } else {
                future.complete(mapper.get(v));
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

    public FutureAction<V> onFailure(Consumer<Throwable> consumer){
        this.whenComplete((v, t) -> {
            if(t != null) {
                consumer.accept(t);
            }
        });
        return this;
    }

}
