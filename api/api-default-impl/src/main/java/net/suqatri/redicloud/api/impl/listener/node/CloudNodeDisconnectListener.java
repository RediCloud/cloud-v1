package net.suqatri.redicloud.api.impl.listener.node;

import net.suqatri.redicloud.api.event.CloudListener;
import net.suqatri.redicloud.api.impl.CloudDefaultAPIImpl;
import net.suqatri.redicloud.api.impl.network.NetworkComponentManager;
import net.suqatri.redicloud.api.node.event.CloudNodeDisconnectEvent;

public class CloudNodeDisconnectListener {

    @CloudListener
    public void onCloudNodeDisconnect(CloudNodeDisconnectEvent event) {
        NetworkComponentManager manager = (NetworkComponentManager) CloudDefaultAPIImpl.getInstance().getNetworkComponentManager();
        manager.removeCachedNode(event.getNodeId());
    }

}
