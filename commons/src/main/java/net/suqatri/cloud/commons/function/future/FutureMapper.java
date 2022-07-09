package net.suqatri.cloud.commons.function.future;

public interface FutureMapper<T, R> {
    R get(T resultToMap);
}
