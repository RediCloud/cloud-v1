package net.suqatri.cloud.node.listener;

import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.event.CloudListener;
import net.suqatri.cloud.api.node.event.CloudNodeConnectedEvent;
import net.suqatri.cloud.node.NodeLauncher;

public class CloudNodeConnectedListener {

    @CloudListener
    public void onCloudNodeConnected(CloudNodeConnectedEvent event) {
        event.getCloudNodeAsync()
            .whenComplete((nodeHolder, t) -> {
               if(t != null){
                   CloudAPI.getInstance().getConsole().error("Error while getting new connected node information!", t);
                   return;
               }
               if(nodeHolder.get().getUniqueId().equals(NodeLauncher.getInstance().getNode().getUniqueId())) return;
               CloudAPI.getInstance().getConsole().info("Node " + NodeLauncher.getInstance().getConsole().getHighlightColor() + nodeHolder.get().getName() + NodeLauncher.getInstance().getConsole().getTextColor() + " has been connected to the cluster!");
            });
    }

}
