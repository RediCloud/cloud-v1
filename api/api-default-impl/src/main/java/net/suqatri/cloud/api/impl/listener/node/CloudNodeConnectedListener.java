package net.suqatri.cloud.api.impl.listener.node;

import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.event.CloudListener;
import net.suqatri.cloud.api.impl.CloudDefaultAPIImpl;
import net.suqatri.cloud.api.impl.network.NetworkComponentManager;
import net.suqatri.cloud.api.node.event.CloudNodeConnectedEvent;

public class CloudNodeConnectedListener {

    @CloudListener
    public void onCloudNodeConnected(CloudNodeConnectedEvent event) {
        NetworkComponentManager manager = (NetworkComponentManager) CloudDefaultAPIImpl.getInstance().getNetworkComponentManager();
        event.getCloudNodeAsync()
                .onFailure(e -> CloudAPI.getInstance().getConsole().error("Error while caching net.suqatri.cloud.api.impl.network components of net.suqatri.cloud.api.impl.node@" + event.getCloudNode().getIdentifier(), e))
                .onSuccess(nodeHolder -> {
                    manager.addCachedNode(nodeHolder.get());
                });
    }

}
