package net.suqatri.redicloud.api.impl.listener.service;

import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.event.CloudListener;
import net.suqatri.redicloud.api.impl.CloudDefaultAPIImpl;
import net.suqatri.redicloud.api.impl.network.NetworkComponentManager;
import net.suqatri.redicloud.api.service.event.CloudServiceStartedEvent;

public class CloudServiceStartedListener {

    @CloudListener
    public void onCloudServiceStarted(CloudServiceStartedEvent event) {
        NetworkComponentManager manager = (NetworkComponentManager) CloudDefaultAPIImpl.getInstance().getNetworkComponentManager();
        event.getServiceAsync()
                .onFailure(e -> CloudAPI.getInstance().getConsole().error("Error while caching net.suqatri.cloud.api.impl.network components of net.suqatri.cloud.api.impl.service@" + event.getService().getIdentifier(), e))
                .onSuccess(serviceHolder -> {
                    manager.addCachedService(serviceHolder.get());
                });
    }
}