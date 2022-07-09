package net.suqatri.api.node;

import net.suqatri.cloud.api.impl.CloudDefaultAPIImpl;
import net.suqatri.cloud.api.node.ICloudNode;
import net.suqatri.cloud.api.utils.ApplicationType;

public abstract class NodeCloudDefaultAPI extends CloudDefaultAPIImpl {

    public NodeCloudDefaultAPI() {
        super(ApplicationType.NODE);
    }

    public abstract void shutdown();

    protected abstract ICloudNode getCloudNode();

}
