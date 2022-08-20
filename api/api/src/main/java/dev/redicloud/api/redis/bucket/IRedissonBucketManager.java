package dev.redicloud.api.redis.bucket;

import dev.redicloud.api.redis.IRedissonManager;
import dev.redicloud.commons.function.future.FutureAction;
import org.redisson.api.RBatch;

import java.util.Collection;

public interface IRedissonBucketManager<T extends I, I extends IRBucketObject> extends IRedissonManager {

    Class<T> getImplClass();

    FutureAction<I> getBucketAsync(String identifier);
    I getBucket(String identifier);

    FutureAction<T> getBucketImplAsync(String identifier);
    T getBucketImpl(String identifier);

    boolean isCached(String identifier);

    I getCached(String identifier);
    T getCachedImpl(String identifier);

    void removeCache(String identifier);

    FutureAction<I> createBucketAsync(String identifier, I object);
    I createBucket(String identifier, I object);

    boolean existsBucket(String identifier);
    FutureAction<Boolean> existsBucketAsync(String identifier);

    Collection<I> getBucketHolders();
    FutureAction<Collection<I>> getBucketHoldersAsync();

    boolean deleteBucket(I identifier);
    FutureAction<Boolean> deleteBucketAsync(I identifier);

    T publishChanges(T holder);
    FutureAction<T> publishChangesAsync(T holder);

    RBatch createBatch();
}
