package net.suqatri.redicloud.node.listener;

import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.event.CloudListener;
import net.suqatri.redicloud.api.node.ICloudNode;
import net.suqatri.redicloud.api.node.event.CloudNodeDisconnectEvent;
import net.suqatri.redicloud.node.NodeLauncher;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class CloudNodeDisconnectListener {

    @CloudListener
    public void onCloudNodeDisconnect(CloudNodeDisconnectEvent event) {
        event.getCloudNodeAsync()
                .whenComplete((node, t) -> {
                    if (t != null) {
                        CloudAPI.getInstance().getConsole().error("Error while getting disconnected node information!", t);
                        return;
                    }
                    if (node.getUniqueId().equals(NodeLauncher.getInstance().getNode().getUniqueId()))
                        return;
                    CloudAPI.getInstance().getConsole().info("Node %hc" + node.getName() + " %tchas been disconnected from the cluster!");
                    if(event.getNodeId().equals(NodeLauncher.getInstance().getNode().getUniqueId())) return;
                    NodeLauncher.getInstance().getNodeManager().getNodesAsync()
                            .onFailure(t1 -> CloudAPI.getInstance().getConsole().error("Error while getting nodes!", t1))
                            .onSuccess(nodes -> {
                               nodes.removeIf(
                                       holder -> holder.getUniqueId().equals(NodeLauncher.getInstance().getNode().getUniqueId())
                                               || holder.getUniqueId().equals(event.getNodeId()));
                               Optional<ICloudNode> optional = nodes.stream().findFirst();
                               if(optional.isPresent()){
                                   NodeLauncher.getInstance().getServiceManager().checkOldService(event.getNodeId());
                               }
                            });
                });
    }

}
