package net.suqatri.api.node;

import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.impl.CloudAPIImpl;
import net.suqatri.cloud.api.node.ICloudNode;
import net.suqatri.cloud.api.utils.ApplicationType;

public abstract class NodeCloudAPI extends CloudAPIImpl {

    public NodeCloudAPI() {
        super(ApplicationType.NODE);
    }

    public abstract void shutdown();

    protected abstract ICloudNode getCloudNode();

}
