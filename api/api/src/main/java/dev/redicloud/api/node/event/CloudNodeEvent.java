package dev.redicloud.api.node.event;

import lombok.Data;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.event.CloudGlobalEvent;
import dev.redicloud.api.node.ICloudNode;
import dev.redicloud.commons.function.future.FutureAction;

import java.util.UUID;

@Data
public class CloudNodeEvent extends CloudGlobalEvent {

    private UUID nodeId;

    public ICloudNode getCloudNode() {
        return CloudAPI.getInstance().getNodeManager().getNode(this.nodeId);
    }

    public FutureAction<ICloudNode> getCloudNodeAsync() {
        return CloudAPI.getInstance().getNodeManager().getNodeAsync(this.nodeId);
    }

}
