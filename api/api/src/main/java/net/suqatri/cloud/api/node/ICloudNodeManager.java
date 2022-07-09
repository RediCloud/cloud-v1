package net.suqatri.cloud.api.node;

import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.commons.function.future.FutureAction;

import java.util.UUID;

public interface ICloudNodeManager<T extends ICloudNode> {

    FutureAction<IRBucketHolder<T>> getNodeAsync(UUID uniqueId);
    IRBucketHolder<T> getNode(UUID uniqueId);

}
