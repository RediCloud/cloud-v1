package net.suqatri.cloud.api.impl.listener.service;

import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.event.CloudListener;
import net.suqatri.cloud.api.impl.CloudDefaultAPIImpl;
import net.suqatri.cloud.api.impl.network.NetworkComponentManager;
import net.suqatri.cloud.api.service.event.CloudServiceStartedEvent;

public class CloudServiceStartedListener {

    @CloudListener
    public void onCloudServiceStarted(CloudServiceStartedEvent event) {
        NetworkComponentManager manager = (NetworkComponentManager) CloudDefaultAPIImpl.getInstance().getNetworkComponentManager();
        event.getServiceAsync()
                .onFailure(e -> CloudAPI.getInstance().getConsole().error("Error while caching network components of service@" + event.getService().getIdentifier(), e))
                .onSuccess(serviceHolder -> {
                    manager.addCachedService(serviceHolder.get());
                });
    }
}