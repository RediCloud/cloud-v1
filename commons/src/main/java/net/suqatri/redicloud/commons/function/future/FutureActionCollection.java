package net.suqatri.redicloud.commons.function.future;

import net.suqatri.redicloud.commons.StringUtils;
import net.suqatri.redicloud.commons.function.Predicates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FutureActionCollection<K, T> {

    private final HashMap<K, FutureAction<? extends T>> toProcess;

    public FutureActionCollection(){
        this.toProcess = new HashMap<>();
    }

    /**
     * Add an action to the collection.
     * @param identifier
     * @param futureAction
     */
    public void addToProcess(K identifier, FutureAction<T> futureAction){
        this.toProcess.put(identifier, futureAction);
    }

    public void addToProcess(FutureAction<T> futureAction){
        Predicates.notNull(futureAction, "futureAction cannot be null");

        this.toProcess.put((K) StringUtils.randomString(StringUtils.ALL, 5), futureAction);
    }

    /**
     * Process all the actions in the collection.
     * @return
     */
    public FutureAction<HashMap<K, T>> process(){
        return processNext(new FutureAction<>(), new HashMap<>());
    }

    /**
     * Process the next action in the collection.
     * Only internal use.
     * @param futureAction
     * @param results
     * @return
     */
    private FutureAction<HashMap<K, T>> processNext(FutureAction<HashMap<K, T>> futureAction, HashMap<K, T> results){
        if(this.toProcess.isEmpty()){
            futureAction.complete(results);
            return futureAction;
        }
        K identifier = new ArrayList<>(this.toProcess.keySet()).get(0);
        FutureAction<? extends T> entry = this.toProcess.get(identifier);
        entry.whenComplete((result, t) -> {
            if(t != null) {
                futureAction.completeExceptionally(t);
                return;
            }
            results.put(identifier, result);
            this.toProcess.remove(identifier);
            processNext(futureAction, results);
        });
        return futureAction;
    }

    /**
     * Create a new future action collection from a list of actions without an identifier.
     * @param futureActions The future action to add to the collection.
     * @param <R> The type of the result of the future action collection.
     * @return The future action collection.
     */
    public static <R> FutureActionCollection<String, R> withoutIdentifier(FutureAction<R> ... futureActions){
        FutureActionCollection<String, R> collection = new FutureActionCollection<>();
        for (FutureAction<R> futureAction : futureActions) {
            collection.addToProcess(StringUtils.randomString(StringUtils.ALL, 5), futureAction);
        }
        return collection;
    }

    /**
     * Create a new future action collection from a list of actions with an identifier.
     * @param futureActions The future action to add to the collection.
     * @param <R> The type of the result of the future action collection.
     * @return A future action collection with an identifier.
     */
    public static <R> FutureActionCollection<String, R> withoutIdentifier(List<FutureAction<R>> futureActions){
        FutureActionCollection<String, R> collection = new FutureActionCollection<>();
        for (FutureAction<R> futureAction : futureActions) {
            collection.addToProcess(StringUtils.randomString(StringUtils.ALL, 5), futureAction);
        }
        return collection;
    }

}
