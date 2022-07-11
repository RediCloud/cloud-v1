package net.suqatri.cloud.api.impl.redis.bucket;

import lombok.Getter;
import lombok.SneakyThrows;
import net.suqatri.cloud.api.impl.node.CloudNode;
import net.suqatri.cloud.api.impl.redis.RedissonManager;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.redis.bucket.IRBucketObject;
import net.suqatri.cloud.api.redis.bucket.IRedissonBucketManager;
import net.suqatri.cloud.commons.function.Predicates;
import net.suqatri.cloud.commons.function.future.FutureAction;
import org.redisson.api.RBucket;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public abstract class RedissonBucketManager<T extends IRBucketObject> extends RedissonManager implements IRedissonBucketManager {

    @Getter
    private static HashMap<String, RedissonBucketManager<?>> managers = new HashMap<>();

    @Getter
    private final ConcurrentHashMap<String, IRBucketHolder<T>> bucketHolders;
    private final Class<T> objectClass;
    private final String redisPrefix;

    public RedissonBucketManager(String prefix, Class<T> objectClass) {
        managers.put(prefix, this);
        this.bucketHolders = new ConcurrentHashMap<>();
        this.objectClass = objectClass;
        this.redisPrefix = prefix;
    }

    @Override
    public FutureAction<IRBucketHolder<T>> getBucketHolderAsync(String identifier) {
        if(this.bucketHolders.containsKey(identifier)) return new FutureAction<>(this.bucketHolders.get(identifier));
        FutureAction<IRBucketHolder<T>> futureAction = new FutureAction<>();

        Predicates.illegalArgument(identifier.contains("@"), "Identifier cannot contain '@'", futureAction);

        existsAsync(identifier)
                .onFailure(futureAction)
                .onSuccess(exists -> {
                    if(!exists) {
                        futureAction.completeExceptionally(new IllegalArgumentException("Bucket[" + identifier + "] does not exist"));
                        return;
                    }
                    RBucket<T> bucket = getClient().getBucket(getRedisKey(identifier), getObjectCodec());
                    bucket.getAsync()
                            .whenComplete((object, throwable) -> {
                                if(throwable != null) {
                                    futureAction.completeExceptionally(throwable);
                                    return;
                                }
                                if(object == null){
                                    futureAction.completeExceptionally(new IllegalArgumentException("Bucket[" + identifier + "] not found! Create first before getting!"));
                                    return;
                                }
                                IRBucketHolder bucketHolder = new RBucketHolder(identifier, this, bucket, (RBucketObject) object);
                                this.bucketHolders.put(identifier, bucketHolder);
                                futureAction.complete(bucketHolder);
                            });
        });
        return futureAction;
    }

    @SneakyThrows
    @Override
    public IRBucketHolder getBucketHolder(String identifier) {
        Predicates.illegalArgument(identifier.contains("@"), "Identifier cannot contain '@'");
        if(this.bucketHolders.containsKey(identifier)) return this.bucketHolders.get(identifier);
        RBucket<T> bucket = getClient().getBucket(getRedisKey(identifier), getObjectCodec());
        T object = bucket.get();
        if(object == null) throw new IllegalArgumentException("Bucket[" + identifier + "] not found! Create first before getting!");
        IRBucketHolder bucketHolder = new RBucketHolder(identifier, this, bucket, (RBucketObject) object);
        this.bucketHolders.put(identifier, bucketHolder);
        return bucketHolder;
    }

    @Override
    public void unlink(IRBucketHolder bucketHolder) {
        this.bucketHolders.remove(bucketHolder.getIdentifier());
    }

    public IRBucketHolder createBucket(String identifier, T object) {
        Predicates.illegalArgument(identifier.contains("@"), "Identifier cannot contain '@'");
        getClient().getBucket(getRedisKey(identifier), getObjectCodec()).set(object);
        RBucket<CloudNode> bucket = getClient().getBucket(getRedisKey(identifier), getObjectCodec());
        IRBucketHolder bucketHolder = new RBucketHolder(identifier, this, bucket, bucket.get());
        this.bucketHolders.put(identifier, bucketHolder);
        return bucketHolder;
    }

    public FutureAction<IRBucketHolder<T>> createBucketAsync(String identifier, T object) {
        FutureAction<IRBucketHolder<T>> futureAction = new FutureAction<>();
        Predicates.illegalArgument(identifier.contains("@"), "Identifier cannot contain '@'", futureAction);
        getClient().getBucket(getRedisKey(identifier), getObjectCodec()).setAsync(object)
                .whenComplete((object1, throwable) -> {
                    if(throwable != null) {
                        futureAction.completeExceptionally(throwable);
                        return;
                    }
                    RBucket<CloudNode> bucket = getClient().getBucket(getRedisKey(identifier), getObjectCodec());
                    IRBucketHolder bucketHolder = new RBucketHolder(identifier, this, bucket, (RBucketObject) object);
                    this.bucketHolders.put(identifier, bucketHolder);
                    futureAction.complete(bucketHolder);
                });
        return futureAction;
    }

    public void updateBucket(String identifier, String json){
        Predicates.illegalArgument(identifier.contains("@"), "Identifier cannot contain '@'");
        if(!identifier.startsWith(getRedisPrefix())) return;
        if(!this.bucketHolders.containsKey(identifier)) return;
        IRBucketHolder bucketHolder = this.bucketHolders.get(identifier);
        bucketHolder.mergeChanges(json);
    }

    public boolean exists(String identifier) {
        Predicates.illegalArgument(identifier.contains("@"), "Identifier cannot contain '@'");
        return getClient().getBucket(getRedisKey(identifier), getObjectCodec()).isExists();
    }

    public FutureAction<Boolean> existsAsync(String identifier) {
        FutureAction<Boolean> futureAction = new FutureAction<>();
        Predicates.illegalArgument(identifier.contains("@"), "Identifier cannot contain '@'", futureAction);
        getClient().getBucket(getRedisKey(identifier), getObjectCodec()).isExistsAsync()
                .whenComplete((exists, throwable) -> {
                    if(throwable != null) {
                        futureAction.completeExceptionally(throwable);
                        return;
                    }
                    futureAction.complete(exists);
                });
        return futureAction;
    }

    @Override
    public String getRedisKey(String identifier) {
        return this.redisPrefix + "@" + identifier;
    }

    @Override
    public Class<T> getObjectClass() {
        return this.objectClass;
    }

    public static RedissonBucketManager<?> getManager(String prefix) {
        return managers.get(prefix);
    }

}
