package net.suqatri.cloud.api.node.event;

import lombok.Data;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.event.CloudGlobalEvent;
import net.suqatri.cloud.api.node.ICloudNode;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.commons.function.future.FutureAction;

import java.util.UUID;

@Data
public class CloudNodeEvent extends CloudGlobalEvent {

    private UUID cloudNodeId;

    public IRBucketHolder<ICloudNode> getCloudNode(){
        return CloudAPI.getInstance().getNodeManager().getNode(this.cloudNodeId);
    }

    public FutureAction<IRBucketHolder<ICloudNode>> getCloudNodeAsync(){
        return CloudAPI.getInstance().getNodeManager().getNodeAsync(this.cloudNodeId);
    }

}
