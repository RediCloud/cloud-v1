package net.suqatri.cloud.api.node;

import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.commons.function.future.FutureAction;

import java.util.Collection;
import java.util.UUID;

public interface ICloudNodeManager<T extends ICloudNode> {


    FutureAction<IRBucketHolder<T>> getNodeAsync(UUID uniqueId);
    IRBucketHolder<T> getNode(UUID uniqueId);

    FutureAction<IRBucketHolder<T>> getNodeAsync(String name);
    IRBucketHolder<T> getNode(String name);

    Collection<IRBucketHolder<T>> getNodes();
    FutureAction<Collection<IRBucketHolder<T>>> getNodesAsync();

    boolean existsNode(UUID uniqueId);
    FutureAction<Boolean> existsNodeAsync(UUID uniqueId);

    boolean existsNode(String name);
    FutureAction<Boolean> existsNodeAsync(String name);

}
