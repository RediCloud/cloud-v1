package net.suqatri.redicloud.api.impl.listener.node;

import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.event.CloudListener;
import net.suqatri.redicloud.api.impl.CloudDefaultAPIImpl;
import net.suqatri.redicloud.api.impl.network.NetworkComponentManager;
import net.suqatri.redicloud.api.node.event.CloudNodeConnectedEvent;

public class CloudNodeConnectedListener {

    @CloudListener
    public void onCloudNodeConnected(CloudNodeConnectedEvent event) {
        NetworkComponentManager manager = (NetworkComponentManager) CloudDefaultAPIImpl.getInstance().getNetworkComponentManager();
        event.getCloudNodeAsync()
                .onFailure(e -> CloudAPI.getInstance().getConsole().error("Error while caching network components of node@" + event.getCloudNode().getIdentifier(), e))
                .onSuccess(nodeHolder -> {
                    manager.addCachedNode(nodeHolder.get());
                });
    }

}
