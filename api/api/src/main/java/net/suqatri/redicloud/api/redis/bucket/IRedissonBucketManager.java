package net.suqatri.redicloud.api.redis.bucket;

import net.suqatri.redicloud.api.redis.IRedissonManager;
import net.suqatri.redicloud.commons.function.future.FutureAction;

public interface IRedissonBucketManager<T extends IRBucketObject> extends IRedissonManager {

    Class<T> getImplClass();

    FutureAction<IRBucketHolder<T>> getBucketHolderAsync(String identifier);

    IRBucketHolder<T> getBucketHolder(String identifier);

    void unlink(IRBucketHolder<T> bucketHolder);

    boolean isBucketHolderCached(String identifier);

    IRBucketHolder<T> getCachedBucketHolder(String identifier);

    void removeCachedBucketHolder(String identifier);

}
