package net.suqatri.redicloud.api.impl.listener.service;

import net.suqatri.redicloud.api.event.CloudListener;
import net.suqatri.redicloud.api.impl.CloudDefaultAPIImpl;
import net.suqatri.redicloud.api.impl.network.NetworkComponentManager;
import net.suqatri.redicloud.api.service.event.CloudServiceStoppedEvent;

public class CloudServiceStoppedListener {

    @CloudListener
    public void onCloudServiceStopped(CloudServiceStoppedEvent event) {
        NetworkComponentManager manager = (NetworkComponentManager) CloudDefaultAPIImpl.getInstance().getNetworkComponentManager();
        manager.removeCachedService(event.getServerId());
    }

}
