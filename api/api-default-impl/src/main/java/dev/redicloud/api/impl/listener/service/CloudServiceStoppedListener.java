package dev.redicloud.api.impl.listener.service;

import dev.redicloud.api.event.CloudListener;
import dev.redicloud.api.impl.CloudDefaultAPIImpl;
import dev.redicloud.api.impl.network.NetworkComponentManager;
import dev.redicloud.api.service.event.CloudServiceStoppedEvent;

public class CloudServiceStoppedListener {

    @CloudListener
    public void onCloudServiceStopped(CloudServiceStoppedEvent event) {
        NetworkComponentManager manager = (NetworkComponentManager) CloudDefaultAPIImpl.getInstance().getNetworkComponentManager();
        manager.removeCachedService(event.getServerId());
    }

}
