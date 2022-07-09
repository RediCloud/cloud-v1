package net.suqatri.cloud.api.node;

import net.suqatri.cloud.api.impl.CloudDefaultAPIImpl;
import net.suqatri.cloud.api.node.ICloudNode;
import net.suqatri.cloud.api.utils.ApplicationType;

public abstract class NodeCloudAPI extends CloudDefaultAPIImpl {

    public NodeCloudAPI() {
        super(ApplicationType.NODE);
    }

    public abstract void shutdown();
    public abstract ICloudNode getCloudNode();

}
