package net.suqatri.cloud.api.impl.node;

import net.suqatri.cloud.api.impl.redis.bucket.RedissonBucketManager;
import net.suqatri.cloud.api.node.ICloudNode;
import net.suqatri.cloud.api.node.ICloudNodeManager;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.commons.function.future.FutureAction;

import java.util.UUID;

public class CloudNodeManager extends RedissonBucketManager<CloudNode> implements ICloudNodeManager {

    public CloudNodeManager() {
        super("node", CloudNode.class);
    }

    @Override
    public IRBucketHolder<CloudNode> getNode(UUID uniqueId) {
        return this.getBucketHolder(uniqueId.toString());
    }

    @Override
    public FutureAction<IRBucketHolder<CloudNode>> getNodeAsync(UUID uniqueId){
        return this.getBucketHolderAsync(uniqueId.toString());
    }

    public IRBucketHolder<CloudNode> createNode(CloudNode node) {
        return this.createBucket(node.getUniqueId().toString(), node);
    }

    public FutureAction<IRBucketHolder<CloudNode>> createNodeAsync(CloudNode node) {
        return this.createBucketAsync(node.getUniqueId().toString(), node);
    }

    public boolean existsNode(UUID uniqueId) {
        return this.exists(uniqueId.toString());
    }

    public FutureAction<Boolean> existsNodeAsync(UUID uniqueId) {
        return this.existsAsync(uniqueId.toString());
    }

}
