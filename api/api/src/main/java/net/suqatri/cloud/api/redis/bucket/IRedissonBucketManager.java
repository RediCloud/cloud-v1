package net.suqatri.cloud.api.redis.bucket;

import net.suqatri.cloud.api.redis.IRedissonManager;
import net.suqatri.cloud.commons.function.future.FutureAction;

public interface IRedissonBucketManager<T extends IRBucketObject> extends IRedissonManager {

    Class<T> getObjectClass();
    FutureAction<IRBucketHolder<T>> getBucketHolderAsync(String identifier);
    IRBucketHolder<T> getBucketHolder(String identifier);
    void unlink(IRBucketHolder<T> bucketHolder);

}
