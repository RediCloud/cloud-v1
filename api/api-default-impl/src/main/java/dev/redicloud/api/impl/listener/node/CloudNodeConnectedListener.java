package dev.redicloud.api.impl.listener.node;

import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.event.CloudListener;
import dev.redicloud.api.impl.CloudDefaultAPIImpl;
import dev.redicloud.api.impl.network.NetworkComponentManager;
import dev.redicloud.api.node.event.CloudNodeConnectedEvent;

public class CloudNodeConnectedListener {

    @CloudListener
    public void onCloudNodeConnected(CloudNodeConnectedEvent event) {
        NetworkComponentManager manager = (NetworkComponentManager) CloudDefaultAPIImpl.getInstance().getNetworkComponentManager();
        event.getCloudNodeAsync()
                .onFailure(e -> CloudAPI.getInstance().getConsole().error("Error while caching network components of node@" + event.getCloudNode().getIdentifier(), e))
                .onSuccess(manager::addCachedNode);
    }

}
