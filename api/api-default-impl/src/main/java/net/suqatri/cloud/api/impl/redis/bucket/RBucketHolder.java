package net.suqatri.cloud.api.impl.redis.bucket;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.redis.bucket.IRedissonBucketManager;
import net.suqatri.cloud.commons.function.future.FutureAction;
import org.redisson.api.RBucket;

public class RBucketHolder<T extends RBucketObject> implements IRBucketHolder<T> {

    private final String identifier;
    private IRedissonBucketManager<T> bucketManager;
    private final RBucket<T> bucket;
    private T publishedObject;

    public RBucketHolder(String identifier, IRedissonBucketManager<T> bucketManager, RBucket<T> bucket) {
        this.bucket = bucket;
        this.identifier = identifier;
        this.bucketManager = bucketManager;
    }

    @Override
    public T getObject(boolean force) {
        if(this.publishedObject != null && !force) return this.publishedObject;
        this.publishedObject = this.bucket.get();
        this.publishedObject.setHolder(this);
        return this.publishedObject;
    }

    @Override
    public FutureAction<T> getObjectAsync(boolean force) {
        FutureAction<T> future = new FutureAction<>();
        if(this.publishedObject != null && !force) {
            future.complete(this.publishedObject);
            return future;
        }
        this.bucket.getAsync().whenComplete((object, throwable) -> {
            if(throwable != null) return;
            this.publishedObject = object;
            this.publishedObject.setHolder(this);
            future.complete(object);
        });
        return future;
    }

    @Override
    public IRBucketHolder<T> update(T object) {
        this.bucket.set(object);
        return this;
    }

    @Override
    public FutureAction<IRBucketHolder<T>> updateAsync(T object) {
        return new FutureAction<>(this.bucket.setAsync(object)).map(v -> this);
    }

    @Override
    public void unlink() {
        this.bucketManager.unlink(this);
    }

    @Override
    public void mergeChanges(String json) {
        try {
            if(this.publishedObject != null) {
                this.bucketManager.getObjectCodec().getObjectMapper().readerForUpdating(this.publishedObject).readValue(json);
            }else{
                this.publishedObject = this.bucketManager.getObjectCodec().getObjectMapper().readValue(json, this.bucketManager.getObjectClass());
            }
        } catch (JsonProcessingException e) {
            CloudAPI.getInstance().getConsole().error("Failed to merge changes of bucket " + getRedisKey(), e);
        }
    }

    @Override
    public String getRedisKey() {
        return this.bucketManager.getRedisKey(this.identifier);
    }

    @Override
    public String getRedisPrefix() {
        return this.bucketManager.getRedisPrefix();
    }

    @Override
    public String getIdentifier() {
        return this.identifier;
    }
}
