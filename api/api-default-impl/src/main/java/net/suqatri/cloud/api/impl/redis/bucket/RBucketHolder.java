package net.suqatri.cloud.api.impl.redis.bucket;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.impl.redis.bucket.packet.BucketUpdatePacket;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.redis.bucket.IRedissonBucketManager;
import net.suqatri.cloud.commons.function.future.FutureAction;
import org.redisson.api.RBucket;

public class RBucketHolder<T extends RBucketObject> implements IRBucketHolder<T> {

    private final String identifier;
    private IRedissonBucketManager<T> bucketManager;
    private final RBucket<T> bucket;
    private T publishedObject;

    public RBucketHolder(String identifier, IRedissonBucketManager<T> bucketManager, RBucket<T> bucket, T object) {
        this.bucket = bucket;
        this.identifier = identifier;
        this.bucketManager = bucketManager;
        if(object == null) throw new IllegalArgumentException("Object that the holder holds cannot be null");
        this.publishedObject = object;
        this.publishedObject.setHolder(this);
    }

    public void setPublishedObject(T object) {
        CloudAPI.getInstance().getConsole().debug("Setting published object for bucket " + identifier + " to " + object);
        this.publishedObject = object;
        this.publishedObject.setHolder(this);
    }

    @Override
    public T get(boolean force) {
        if(this.publishedObject != null && !force) return this.publishedObject;
        setPublishedObject(this.bucket.get());
        return this.publishedObject;
    }

    @Override
    public IRBucketHolder<T> update(T object) {
        this.bucket.set(object);
        try {
            BucketUpdatePacket packet = new BucketUpdatePacket();
            packet.setIdentifier(this.identifier);
            packet.setRedisPrefix(this.getRedisPrefix());
            packet.setJson(this.bucketManager.getObjectCodec().getObjectMapper().writeValueAsString(object));
            packet.publishAll();
        } catch (JsonProcessingException e) {
            CloudAPI.getInstance().getConsole().error("Error while publishing bucket update packet for " + getRedisKey(), e);
        }
        return this;
    }

    @Override
    public FutureAction<IRBucketHolder<T>> updateAsync(T object) {
        FutureAction<IRBucketHolder<T>> futureAction = new FutureAction<>();

        new FutureAction<>(this.bucket.setAsync(object))
                .onFailure(futureAction)
                .onSuccess(a -> {
                    try {
                        BucketUpdatePacket packet = new BucketUpdatePacket();
                        packet.setIdentifier(this.identifier);
                        packet.setRedisPrefix(this.getRedisPrefix());
                        packet.setJson(this.bucketManager.getObjectCodec().getObjectMapper().writeValueAsString(object));
                        packet.publishAllAsync();
                    } catch (JsonProcessingException e) {
                        futureAction.completeExceptionally(e);
                        return;
                    }
                    futureAction.complete(this);
                });

        return futureAction;
    }

    @Override
    public void unlink() {
        this.bucketManager.unlink(this);
    }

    @Override
    public void mergeChanges(String json) {
        try {
            if(this.publishedObject != null) {
                CloudAPI.getInstance().getConsole().debug("Merging changes for bucket " + this.identifier);
                this.bucketManager.getObjectCodec().getObjectMapper().readerForUpdating(this.publishedObject).readValue(json);
                this.publishedObject.setHolder(this);
            }else{
                this.setPublishedObject(this.bucketManager.getObjectCodec().getObjectMapper().readValue(json, this.bucketManager.getObjectClass()));
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
