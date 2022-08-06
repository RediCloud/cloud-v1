package net.suqatri.redicloud.api.impl.node.packet;

import lombok.Data;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.network.NetworkComponentType;
import net.suqatri.redicloud.api.node.ICloudNode;

import java.util.UUID;

@Data
public class CloudNodeShutdownPacket extends CloudNodePacket {

    private UUID nodeId;

    @Override
    public void receive() {
        if (CloudAPI.getInstance().getNetworkComponentInfo().getType() != NetworkComponentType.NODE) {
            CloudAPI.getInstance().getConsole().error("The " + this.getClass().getSimpleName() + " can only be received by the node!");
            return;
        }
        CloudAPI.getInstance().getNodeManager().getNodeAsync(this.nodeId)
                .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to get node " + this.nodeId, e))
                .onSuccess(ICloudNode::shutdown);
    }
}
