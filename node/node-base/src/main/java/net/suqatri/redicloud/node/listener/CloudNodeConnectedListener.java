package net.suqatri.redicloud.node.listener;

import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.event.CloudListener;
import net.suqatri.redicloud.api.node.event.CloudNodeConnectedEvent;
import net.suqatri.redicloud.node.NodeLauncher;

public class CloudNodeConnectedListener {

    @CloudListener
    public void onCloudNodeConnected(CloudNodeConnectedEvent event) {
        event.getCloudNodeAsync()
                .whenComplete((nodeHolder, t) -> {
                    if (t != null) {
                        CloudAPI.getInstance().getConsole().error("Error while getting new connected node information!", t);
                        return;
                    }
                    if (nodeHolder.getUniqueId().equals(NodeLauncher.getInstance().getNode().getUniqueId()))
                        return;
                    CloudAPI.getInstance().getConsole().info("Node %hc" + nodeHolder.getName() + "%tc has been connected to the cluster!");
                });
    }

}
