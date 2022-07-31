package net.suqatri.redicloud.node.listener;

import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.event.CloudListener;
import net.suqatri.redicloud.api.node.event.CloudNodeDisconnectEvent;
import net.suqatri.redicloud.node.NodeLauncher;

import java.util.concurrent.TimeUnit;

public class CloudNodeDisconnectListener {

    @CloudListener
    public void onCloudNodeDisconnect(CloudNodeDisconnectEvent event) {
        event.getCloudNodeAsync()
                .whenComplete((nodeHolder, t) -> {
                    if (t != null) {
                        CloudAPI.getInstance().getConsole().error("Error while getting disconnected node information!", t);
                        return;
                    }
                    if (nodeHolder.get().getUniqueId().equals(NodeLauncher.getInstance().getNode().getUniqueId()))
                        return;
                    CloudAPI.getInstance().getConsole().info("Node %hc" + nodeHolder.get().getName() + " %tchas been disconnected from the cluster!");
                    CloudAPI.getInstance().getScheduler().runTaskLaterAsync(
                            () -> NodeLauncher.getInstance().getServiceManager().checkOldService(nodeHolder.get().getUniqueId()),
                            3, TimeUnit.SECONDS);
                });
    }

}
