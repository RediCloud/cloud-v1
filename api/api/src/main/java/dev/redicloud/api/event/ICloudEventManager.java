package dev.redicloud.api.event;

import java.util.function.Consumer;

public interface ICloudEventManager {

    <T extends CloudEvent> void postLocal(T event);

    <T extends CloudGlobalEvent> void postGlobal(T event);

    <T extends CloudGlobalEvent> void postGlobalAsync(T event);

    void register(Object listener);

    void unregister(Object listener);

    <T extends CloudEvent> void register(Class<T> eventClass, Consumer<T> consumer);
    <T extends CloudEvent> void registerWithoutBlockWarning(Class<T> eventClass, Consumer<T> consumer);

    <T extends CloudEvent> void unregister(Class<T> eventClass, Consumer<T> consumer);

}
