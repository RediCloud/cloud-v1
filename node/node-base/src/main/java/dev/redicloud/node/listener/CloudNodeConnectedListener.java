package dev.redicloud.node.listener;

import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.event.CloudListener;
import dev.redicloud.api.node.event.CloudNodeConnectedEvent;
import dev.redicloud.node.NodeLauncher;

public class CloudNodeConnectedListener {

    @CloudListener
    public void onCloudNodeConnected(CloudNodeConnectedEvent event) {
        event.getCloudNodeAsync()
                .whenComplete((node, t) -> {
                    if (t != null) {
                        CloudAPI.getInstance().getConsole().error("Error while getting new connected node information!", t);
                        return;
                    }
                    if (node.getUniqueId().equals(NodeLauncher.getInstance().getNode().getUniqueId()))
                        return;
                    CloudAPI.getInstance().getConsole().info("Node %hc" + node.getName() + "%tc has been connected to the cluster!");
                });
    }

}
