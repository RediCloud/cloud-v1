package net.suqatri.redicloud.api.impl.redis.bucket;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.SneakyThrows;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.redis.RedissonManager;
import net.suqatri.redicloud.api.impl.redis.bucket.packet.BucketDeletePacket;
import net.suqatri.redicloud.api.impl.redis.bucket.packet.BucketUpdatePacket;
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
public abstract class RedissonBucketManager<T extends I, I extends IRBucketObject> extends RedissonManager implements IRedissonBucketManager<T, I> {

    private static HashMap<String, RedissonBucketManager<?, ?>> managers = new HashMap<>();

    private final ConcurrentHashMap<String, T> cachedBucketObjects;
    private final Class<T> interfaceClass;
    private final String redisPrefix;

    public RedissonBucketManager(String prefix, Class<T> interfaceClass) {
        managers.put(prefix, this);
        this.cachedBucketObjects = new ConcurrentHashMap<>();
        this.interfaceClass = interfaceClass;
        this.redisPrefix = prefix;
    }

    public static RedissonBucketManager<?, ?> getManager(String prefix) {
        return managers.get(prefix);
    }

    @Override
    public FutureAction<T> publishChangesAsync(T object) {
        Predicates.notNull(object, "Object with identifier " + object.getIdentifier() + " is null!");
        CloudAPI.getInstance().getConsole().trace("Updating bucket " + object.getIdentifier() + "!");
        FutureAction<T> futureAction = new FutureAction<>();

        new FutureAction<>(this.getClient().getBucket(object.getRedisKey(), getObjectCodec()).setAsync(object))
            .onFailure(futureAction)
            .onSuccess(a -> {
                try {
                    BucketUpdatePacket packet = new BucketUpdatePacket();
                    packet.setIdentifier(object.getIdentifier());
                    packet.setRedisPrefix(this.getRedisPrefix());
                    packet.setJson(this.getObjectCodec().getObjectMapper().writeValueAsString(object));
                    packet.publishAllAsync();
                } catch (JsonProcessingException e) {
                    futureAction.completeExceptionally(e);
                    return;
                }
                futureAction.complete(object);
            });

        return futureAction;
    }

    @Override
    public T publishChanges(T object) {
        Predicates.notNull(object, "Object with identifier " + object.getIdentifier() + " is null!");
        CloudAPI.getInstance().getConsole().trace("Updating bucket " + object.getRedisKey() + "!");
        this.getClient().getBucket(object.getRedisKey(), getObjectCodec()).set(object);
        try {
            BucketUpdatePacket packet = new BucketUpdatePacket();
            packet.setIdentifier(object.getIdentifier());
            packet.setRedisPrefix(this.getRedisPrefix());
            packet.setJson(getObjectCodec().getObjectMapper().writeValueAsString(object));
            packet.publishAll();
        } catch (JsonProcessingException e) {
            CloudAPI.getInstance().getConsole().error("Error while publishing bucket update packet for " + object.getIdentifier(), e);
        }
        return object;
    }

    @Override
    public boolean isCached(String identifier) {
        return this.cachedBucketObjects.containsKey(identifier);
    }

    @Override
    public I getCached(String identifier) {
        return this.cachedBucketObjects.get(identifier);
    }

    @Override
    public T getCachedImpl(String identifier) {
        return this.cachedBucketObjects.get(identifier);
    }

    @Override
    public void removeCache(String identifier) {
        this.cachedBucketObjects.remove(identifier);
    }

    @Override
    public FutureAction<I> getAsync(String identifier) {
        if (this.isCached(identifier)) return new FutureAction<I>(this.cachedBucketObjects.get(identifier));
        FutureAction<I> futureAction = new FutureAction<>();

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
                        object.setManager(this);
                        this.cachedBucketObjects.put(identifier, object);
                        futureAction.complete(object);
                    });
            });
        return futureAction;
    }

    @SneakyThrows
    @Override
    public I get(String identifier) {
        Predicates.illegalArgument(identifier.contains(":"), "Identifier cannot contain ':'");
        if (this.isCached(identifier)) return this.cachedBucketObjects.get(identifier);
        RBucket<T> bucket = getClient().getBucket(getRedisKey(identifier), getObjectCodec());
        if (!bucket.isExists())
            throw new NullPointerException("Bucket[" + getRedisKey(identifier) + "] doesn't exist");
        T object = bucket.get();
        object.setManager(this);
        this.cachedBucketObjects.put(identifier, object);
        return object;
    }

    @Override
    public FutureAction<T> getImplAsync(String identifier) {
        return this.getAsync(identifier).map(object -> (T)object);
    }

    @Override
    public T getImpl(String identifier) {
        return (T) this.get(identifier);
    }

    @Override
    public I createBucket(String identifier, I object) {
        Predicates.illegalArgument(identifier.contains(":"), "Identifier cannot contain ':'");
        if (existsBucket(identifier))
            throw new IllegalArgumentException("Bucket[" + getRedisKey(identifier) + "] already exists");
        getClient().getBucket(getRedisKey(identifier), getObjectCodec()).set(object);
        object.setManager(this);
        this.cachedBucketObjects.put(identifier, (T) object);
        return object;
    }

    @Override
    public FutureAction<I> createBucketAsync(String identifier, I object) {
        FutureAction<I> futureAction = new FutureAction<>();
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
                        object.setManager(this);
                        this.cachedBucketObjects.put(identifier, (T) object);
                        futureAction.complete(object);
                    });
            });

        return futureAction;
    }

    public void mergeChanges(String identifier, String json) {
        Predicates.illegalArgument(identifier.contains(":"), "Identifier cannot contain ':'");
        if (!this.isCached(identifier)) return;
        CloudAPI.getInstance().getConsole().trace("Updating bucket: " + getRedisKey(identifier));
        T bucketHolder = this.cachedBucketObjects.get(identifier);
        if(bucketHolder == null) return; //bucket was deleted while updating
        if (json == null) throw new IllegalArgumentException("Object that the holder holds cannot be null");
        try {
            CloudAPI.getInstance().getConsole().trace("Merging changes for bucket " + getRedisKey(identifier) + " | cached: " + (isCached(identifier)));
            if (this.isCached(identifier)) {
                T object = this.getCachedImpl(identifier);
                getObjectCodec().getObjectMapper().readerForUpdating(object).readValue(json);
                object.setManager(this);
                object.merged();
            } else {
                T object = this.getObjectCodec().getObjectMapper().readValue(json, this.getImplClass());
                object.setManager(this);
                this.cachedBucketObjects.put(identifier, object);
                object.merged();
            }
        } catch (JsonProcessingException e) {
            CloudAPI.getInstance().getConsole().error("Failed to merge changes of bucket " + getRedisKey(identifier), e);
        }
    }

    @Override
    public boolean existsBucket(String identifier) {
        Predicates.illegalArgument(identifier.contains(":"), "Identifier cannot contain ':'");
        return getClient().getBucket(getRedisKey(identifier), getObjectCodec()).isExists();
    }

    @Override
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

    @Override
    public FutureAction<Collection<I>> getBucketHoldersAsync() {
        FutureAction<Collection<I>> futureAction = new FutureAction<>();
        getKeys(this.redisPrefix + ":*")
            .onFailure(futureAction)
            .onSuccess(keys -> {
                FutureActionCollection<String, I> futureActionCollection = new FutureActionCollection<>();
                for (String s : keys) {
                    futureActionCollection.addToProcess(s.split(":")[1], getAsync(s.split(":")[1]));
                }
                futureActionCollection.process()
                        .onFailure(futureAction)
                        .onSuccess(holders -> {
                            futureAction.complete(holders.values());
                        });
            });
        return futureAction;
    }

    @Override
    public Collection<I> getBucketHolders() {
        Collection<I> bucketHolders = new ArrayList<>();
        for (String s : getClient().getKeys().getKeysByPattern(this.redisPrefix + ":*")) {
            bucketHolders.add(get(s.split(":")[1]));
        }
        return bucketHolders;
    }

    @Override
    public boolean deleteBucket(String identifier) {
        Predicates.illegalArgument(identifier.contains(":"), "Identifier cannot contain ':'");
        if (this.isCached(identifier)) {
            this.cachedBucketObjects.remove(identifier);
        }
        CloudAPI.getInstance().getConsole().trace("Deleting bucket: " + getRedisKey(identifier));

        BucketDeletePacket packet = new BucketDeletePacket();
        packet.setRedisPrefix(this.redisPrefix);
        packet.setIdentifier(identifier);
        packet.publishAll();

        getClient().getBucket(getRedisKey(identifier), getObjectCodec()).delete();
        return true;
    }

    @Override
    public FutureAction<Boolean> deleteBucketAsync(String identifier) {
        Predicates.illegalArgument(identifier.contains(":"), "Identifier cannot contain ':'");
        if (this.isCached(identifier)) {
            this.cachedBucketObjects.remove(identifier);
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
    public Class<T> getImplClass() {
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
