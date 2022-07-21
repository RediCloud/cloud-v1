package net.suqatri.redicloud.commons.function.future;

public interface FutureMapper<T, R> {
    R get(T resultToMap);
}
