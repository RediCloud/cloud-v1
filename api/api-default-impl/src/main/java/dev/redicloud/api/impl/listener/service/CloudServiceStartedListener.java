package dev.redicloud.api.impl.listener.service;

import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.event.CloudListener;
import dev.redicloud.api.impl.CloudDefaultAPIImpl;
import dev.redicloud.api.impl.network.NetworkComponentManager;
import dev.redicloud.api.service.event.CloudServiceStartedEvent;

public class CloudServiceStartedListener {

    @CloudListener
    public void onCloudServiceStarted(CloudServiceStartedEvent event) {
        NetworkComponentManager manager = (NetworkComponentManager) CloudDefaultAPIImpl.getInstance().getNetworkComponentManager();
        event.getServiceAsync()
                .onFailure(e -> CloudAPI.getInstance().getConsole().error("Error while caching network components of service@" + event.getService().getIdentifier(), e))
                .onSuccess(manager::addCachedService);
    }
}