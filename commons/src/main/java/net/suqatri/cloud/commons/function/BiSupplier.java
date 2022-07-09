package net.suqatri.cloud.commons.function;

public interface BiSupplier<V, E> {

    E supply(V v);
}
