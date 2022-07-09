package net.suqatri.cloud.api.event;

public interface ICloudEventManager {

    void postLocal(CloudEvent event);
    void postGlobal(CloudGlobalEvent event);

    void register(Object listener);
    void unregister(Object listener);

}
