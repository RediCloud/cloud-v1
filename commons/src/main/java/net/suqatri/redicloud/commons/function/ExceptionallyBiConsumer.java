package net.suqatri.redicloud.commons.function;

import net.suqatri.redicloud.commons.collection.WrappedException;

import java.util.function.BiConsumer;

public interface ExceptionallyBiConsumer<T, U> extends BiConsumer<T, U> {

    @Override
    default void accept(T t, U u) {
        try {
            acceptExceptionally(t, u);
        } catch (Exception ex) {
            throw WrappedException.rethrow(ex);
        }
    }

    void acceptExceptionally(T t, U u) throws Exception;
}
