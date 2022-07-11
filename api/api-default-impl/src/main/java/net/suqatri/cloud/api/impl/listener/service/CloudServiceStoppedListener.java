package net.suqatri.cloud.api.impl.listener.service;

import net.suqatri.cloud.api.event.CloudListener;
import net.suqatri.cloud.api.impl.CloudDefaultAPIImpl;
import net.suqatri.cloud.api.impl.network.NetworkComponentManager;
import net.suqatri.cloud.api.service.event.CloudServiceStartedEvent;
import net.suqatri.cloud.api.service.event.CloudServiceStoppedEvent;

public class CloudServiceStoppedListener {

    @CloudListener
    public void onCloudServiceStopped(CloudServiceStoppedEvent event) {
        NetworkComponentManager manager = (NetworkComponentManager) CloudDefaultAPIImpl.getInstance().getNetworkComponentManager();
        manager.removeCachedService(event.getServerId());
    }

}
