package net.suqatri.cloud.node.listener;

import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.event.CloudListener;
import net.suqatri.cloud.api.node.event.CloudNodeDisconnectEvent;
import net.suqatri.cloud.node.NodeLauncher;

public class CloudNodeDisconnectListener {

    @CloudListener
    public void onCloudNodeDisconnect(CloudNodeDisconnectEvent event) {
        event.getCloudNodeAsync()
            .whenComplete((nodeHolder, t) -> {
               if(t != null){
                   CloudAPI.getInstance().getConsole().error("Error while getting disconnected node information!", t);
                   return;
               }
               if(nodeHolder.get().getUniqueId().equals(NodeLauncher.getInstance().getCloudNode().get().getUniqueId())) return;
               CloudAPI.getInstance().getConsole().info("Node " + NodeLauncher.getInstance().getConsole().getHighlightColor() + nodeHolder.get().getName() + NodeLauncher.getInstance().getConsole().getTextColor() + " has been disconnected from the cluster!");
            });
    }

}
