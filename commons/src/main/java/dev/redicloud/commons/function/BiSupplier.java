package dev.redicloud.commons.function;

public interface BiSupplier<V, E> {

    E supply(V v);
}
