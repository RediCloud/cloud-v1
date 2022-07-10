package net.suqatri.cloud.api.node;

import net.suqatri.cloud.api.impl.CloudDefaultAPIImpl;
import net.suqatri.cloud.api.impl.node.CloudNode;
import net.suqatri.cloud.api.impl.redis.bucket.RBucketHolder;
import net.suqatri.cloud.api.node.ICloudNode;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.utils.ApplicationType;

public abstract class NodeCloudDefaultAPI extends CloudDefaultAPIImpl {

    public NodeCloudDefaultAPI() {
        super(ApplicationType.NODE);
    }

    protected abstract RBucketHolder<CloudNode> getCloudNode();

}
