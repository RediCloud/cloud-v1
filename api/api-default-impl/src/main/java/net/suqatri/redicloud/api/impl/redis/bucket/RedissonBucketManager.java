package net.suqatri.redicloud.api.impl.redis.bucket;

import lombok.Getter;
import lombok.SneakyThrows;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.node.CloudNode;
import net.suqatri.redicloud.api.impl.redis.RedissonManager;
import net.suqatri.redicloud.api.impl.redis.bucket.packet.BucketDeletePacket;
import net.suqatri.redicloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.redicloud.api.redis.bucket.IRBucketObject;
import net.suqatri.redicloud.api.redis.bucket.IRedissonBucketManager;
import net.suqatri.redicloud.commons.function.Predicates;
import net.suqatri.redicloud.commons.function.future.FutureAction;
import net.suqatri.redicloud.commons.function.future.FutureActionCollection;
import org.redisson.api.RBucket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public abstract class RedissonBucketManager<T extends IRBucketObject, I> extends RedissonManager implements IRedissonBucketManager {

    private static HashMap<String, RedissonBucketManager<?, ?>> managers = new HashMap<>();

    private final ConcurrentHashMap<String, IRBucketHolder<T>> cachedBucketHolders;
    private final Class<I> interfaceClass;
    private final String redisPrefix;

    public RedissonBucketManager(String prefix, Class<I> interfaceClass) {
        managers.put(prefix, this);
        this.cachedBucketHolders = new ConcurrentHashMap<>();
        this.interfaceClass = interfaceClass;
        this.redisPrefix = prefix;
    }

    public static RedissonBucketManager<?, ?> getManager(String prefix) {
        return managers.get(prefix);
    }

    @Override
    public boolean isBucketHolderCached(String identifier) {
        return this.cachedBucketHolders.containsKey(identifier);
    }

    @Override
    public IRBucketHolder getCachedBucketHolder(String identifier) {
        return this.cachedBucketHolders.get(identifier);
    }

    @Override
    public void removeCachedBucketHolder(String identifier) {
        this.cachedBucketHolders.remove(identifier);
    }

    @Override
    public FutureAction<IRBucketHolder<I>> getBucketHolderAsync(String identifier) {
        if (this.isBucketHolderCached(identifier)) return new FutureAction(this.cachedBucketHolders.get(identifier));
        FutureAction<IRBucketHolder<I>> futureAction = new FutureAction<>();

        Predicates.illegalArgument(identifier.contains(":"), "Identifier cannot contain '@'", futureAction);

        existsBucketAsync(identifier)
                .onFailure(futureAction)
                .onSuccess(exists -> {
                    if (!exists) {
                        futureAction.completeExceptionally(new NullPointerException("Bucket[" + identifier + "] doesn't exist"));
                        return;
                    }
                    RBucket<T> bucket = getClient().getBucket(getRedisKey(identifier), getObjectCodec());
                    bucket.getAsync()
                            .whenComplete((object, throwable) -> {
                                if (throwable != null) {
                                    futureAction.completeExceptionally(throwable);
                                    return;
                                }
                                if (object == null) {
                                    futureAction.completeExceptionally(new IllegalArgumentException("Bucket[" + identifier + "] not found! Create first before getting!"));
                                    return;
                                }
                                IRBucketHolder bucketHolder = new RBucketHolder(identifier, this, bucket, (RBucketObject) object);
                                this.cachedBucketHolders.put(identifier, bucketHolder);
                                futureAction.complete(bucketHolder);
                            });
                });
        return futureAction;
    }

    @SneakyThrows
    @Override
    public IRBucketHolder<I> getBucketHolder(String identifier) {
        Predicates.illegalArgument(identifier.contains(":"), "Identifier cannot contain ':'");
        if (this.isBucketHolderCached(identifier)) return (IRBucketHolder<I>) this.cachedBucketHolders.get(identifier);
        RBucket<T> bucket = getClient().getBucket(getRedisKey(identifier), getObjectCodec());
        if (!bucket.isExists())
            throw new NullPointerException("Bucket[" + getRedisKey(identifier) + "] doesn't exist");
        T object = bucket.get();
        IRBucketHolder bucketHolder = new RBucketHolder(identifier, this, bucket, (RBucketObject) object);
        this.cachedBucketHolders.put(identifier, bucketHolder);
        return bucketHolder;
    }

    @Override
    public void unlink(IRBucketHolder bucketHolder) {
        this.cachedBucketHolders.remove(bucketHolder.getIdentifier());
    }

    public IRBucketHolder<I> createBucket(String identifier, I object) {
        Predicates.illegalArgument(identifier.contains(":"), "Identifier cannot contain ':'");
        if (existsBucket(identifier))
            throw new IllegalArgumentException("Bucket[" + getRedisKey(identifier) + "] already exists");
        getClient().getBucket(getRedisKey(identifier), getObjectCodec()).set(object);
        RBucket<CloudNode> bucket = getClient().getBucket(getRedisKey(identifier), getObjectCodec());
        IRBucketHolder bucketHolder = new RBucketHolder(identifier, this, bucket, bucket.get());
        this.cachedBucketHolders.put(identifier, bucketHolder);
        return bucketHolder;
    }

    public FutureAction<IRBucketHolder<I>> createBucketAsync(String identifier, I object) {
        FutureAction<IRBucketHolder<I>> futureAction = new FutureAction<>();
        Predicates.illegalArgument(identifier.contains(":"), "Identifier cannot contain ':'", futureAction);
        existsBucketAsync(identifier)
                .onFailure(futureAction)
                .onSuccess(exists -> {
                    if (exists) {
                        futureAction.completeExceptionally(new IllegalArgumentException("Bucket[" + getRedisKey(identifier) + "] already exists"));
                        return;
                    }
                    getClient().getBucket(getRedisKey(identifier), getObjectCodec()).setAsync(object)
                            .whenComplete((object1, throwable) -> {
                                if (throwable != null) {
                                    futureAction.completeExceptionally(throwable);
                                    return;
                                }
                                RBucket<CloudNode> bucket = getClient().getBucket(getRedisKey(identifier), getObjectCodec());
                                IRBucketHolder bucketHolder = new RBucketHolder(identifier, this, bucket, (RBucketObject) object);
                                this.cachedBucketHolders.put(identifier, bucketHolder);
                                futureAction.complete(bucketHolder);
                            });
                });

        return futureAction;
    }

    public void updateBucket(String identifier, String json) {
        Predicates.illegalArgument(identifier.contains(":"), "Identifier cannot contain ':'");
        if (!this.isBucketHolderCached(identifier)) return;
        CloudAPI.getInstance().getConsole().trace("Updating bucket: " + getRedisKey(identifier));
        IRBucketHolder<T> bucketHolder = this.cachedBucketHolders.get(identifier);
        if(bucketHolder == null) return; //bucket was deleted while updating
        bucketHolder.mergeChanges(json);
    }

    public boolean existsBucket(String identifier) {
        Predicates.illegalArgument(identifier.contains(":"), "Identifier cannot contain ':'");
        return getClient().getBucket(getRedisKey(identifier), getObjectCodec()).isExists();
    }

    public FutureAction<Boolean> existsBucketAsync(String identifier) {
        FutureAction<Boolean> futureAction = new FutureAction<>();
        Predicates.illegalArgument(identifier.contains(":"), "Identifier cannot contain ':'", futureAction);
        getClient().getBucket(getRedisKey(identifier), getObjectCodec()).isExistsAsync()
                .whenComplete((exists, throwable) -> {
                    if (throwable != null) {
                        futureAction.completeExceptionally(throwable);
                        return;
                    }
                    futureAction.complete(exists);
                });
        return futureAction;
    }

    public FutureAction<Collection<IRBucketHolder<I>>> getBucketHoldersAsync() {
        FutureAction<Collection<IRBucketHolder<I>>> futureAction = new FutureAction<>();
        getKeys(this.redisPrefix + "@*")
                .onFailure(futureAction)
                .onSuccess(keys -> {
                    FutureActionCollection<String, IRBucketHolder<I>> futureActionCollection = new FutureActionCollection<>();
                    for (String s : keys) {
                        futureActionCollection.addToProcess(s.split(":")[1], getBucketHolderAsync(s.split(":")[1]));
                    }
                    futureActionCollection.process()
                            .onFailure(futureAction)
                            .onSuccess(holders -> {
                                futureAction.complete(holders.values());
                            });
                });
        return futureAction;
    }

    public Collection<IRBucketHolder<I>> getBucketHolders() {
        Collection<IRBucketHolder<I>> bucketHolders = new ArrayList<>();
        for (String s : getClient().getKeys().getKeysByPattern(this.redisPrefix + "@*")) {
            bucketHolders.add(getBucketHolder(s.split(":")[1]));
        }
        return bucketHolders;
    }

    public boolean deleteBucket(String identifier) {
        Predicates.illegalArgument(identifier.contains(":"), "Identifier cannot contain ':'");
        if (this.isBucketHolderCached(identifier)) {
            this.cachedBucketHolders.remove(identifier);
        }
        CloudAPI.getInstance().getConsole().trace("Deleting bucket: " + getRedisKey(identifier));

        BucketDeletePacket packet = new BucketDeletePacket();
        packet.setRedisPrefix(this.redisPrefix);
        packet.setIdentifier(identifier);
        packet.publishAll();

        getClient().getBucket(getRedisKey(identifier), getObjectCodec()).delete();
        return true;
    }

    public FutureAction<Boolean> deleteBucketAsync(String identifier) {
        Predicates.illegalArgument(identifier.contains(":"), "Identifier cannot contain ':'");
        if (this.isBucketHolderCached(identifier)) {
            this.cachedBucketHolders.remove(identifier);
        }
        CloudAPI.getInstance().getConsole().trace("Deleting bucket: " + getRedisKey(identifier));

        BucketDeletePacket packet = new BucketDeletePacket();
        packet.setRedisPrefix(this.redisPrefix);
        packet.setIdentifier(identifier);
        packet.publishAllAsync();

        return new FutureAction<>(getClient().getBucket(getRedisKey(identifier), getObjectCodec()).deleteAsync());
    }

    @Override
    public String getRedisKey(String identifier) {
        return this.redisPrefix + ":" + identifier;
    }

    @Override
    public Class<I> getImplClass() {
        return this.interfaceClass;
    }

    private FutureAction<Collection<String>> getKeys(String pattern) {
        FutureAction<Collection<String>> futureAction = new FutureAction<>();
        CloudAPI.getInstance().getExecutorService().submit(() -> {
            Collection<String> keys = new ArrayList<>();
            for (String s : getClient().getKeys().getKeysByPattern(pattern)) {
                keys.add(s);
            }
            futureAction.complete(keys);
        });
        return futureAction;
    }

}
