package dev.redicloud.api.impl.listener.node;

import dev.redicloud.api.event.CloudListener;
import dev.redicloud.api.impl.CloudDefaultAPIImpl;
import dev.redicloud.api.impl.network.NetworkComponentManager;
import dev.redicloud.api.node.event.CloudNodeDisconnectEvent;

public class CloudNodeDisconnectListener {

    @CloudListener
    public void onCloudNodeDisconnect(CloudNodeDisconnectEvent event) {
        NetworkComponentManager manager = (NetworkComponentManager) CloudDefaultAPIImpl.getInstance().getNetworkComponentManager();
        manager.removeCachedNode(event.getNodeId());
    }

}
