package net.suqatri.cloud.api.node;

import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.commons.function.future.FutureAction;

import java.util.Collection;
import java.util.UUID;

public interface ICloudNodeManager {


    FutureAction<IRBucketHolder<ICloudNode>> getNodeAsync(UUID uniqueId);
    IRBucketHolder<ICloudNode> getNode(UUID uniqueId);

    FutureAction<IRBucketHolder<ICloudNode>> getNodeAsync(String name);
    IRBucketHolder<ICloudNode> getNode(String name);

    Collection<IRBucketHolder<ICloudNode>> getNodes();
    FutureAction<Collection<IRBucketHolder<ICloudNode>>> getNodesAsync();

    boolean existsNode(UUID uniqueId);
    FutureAction<Boolean> existsNodeAsync(UUID uniqueId);

    boolean existsNode(String name);
    FutureAction<Boolean> existsNodeAsync(String name);

    IRBucketHolder<ICloudNode> createNode(ICloudNode node);
    FutureAction<IRBucketHolder<ICloudNode>> createNodeAsync(ICloudNode node);

}
