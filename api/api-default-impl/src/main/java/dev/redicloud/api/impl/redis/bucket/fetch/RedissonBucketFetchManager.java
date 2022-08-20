package dev.redicloud.api.impl.redis.bucket.fetch;

import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.impl.redis.bucket.RedissonBucketManager;
import dev.redicloud.api.impl.redis.bucket.packet.BucketDeletePacket;
import dev.redicloud.api.redis.bucket.fetch.IRBucketFetchAble;
import dev.redicloud.api.redis.bucket.fetch.IRedissonBucketFetchManager;
import dev.redicloud.api.redis.event.RedisConnectedEvent;
import dev.redicloud.commons.function.Predicates;
import dev.redicloud.commons.function.future.FutureAction;
import lombok.Getter;
import org.redisson.api.RMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

@Getter
public abstract class RedissonBucketFetchManager<T extends I, I extends IRBucketFetchAble> extends RedissonBucketManager<T, I> implements IRedissonBucketFetchManager<T, I> {

    private RMap<String, String> fetcherMap;
    private final String fetcherName;

    public RedissonBucketFetchManager(String prefix, Class<T> interfaceClass, String fetcherName) {
        super(prefix, interfaceClass);
        this.fetcherName = fetcherName;
        CloudAPI.getInstance().getEventManager().register(RedisConnectedEvent.class, event ->
                this.fetcherMap = event.getConnection().getClient().getMap("fetcher:" + fetcherName, getObjectCodec()));
    }

    @Override
    public FutureAction<Set<String>> readAllFetcherKeysAsync() {
        return new FutureAction<>(this.fetcherMap.readAllKeySetAsync());
    }

    @Override
    public void putInFetcher(String fetcherKey, String fetcherValue) {
        this.fetcherMap.putAsync(fetcherKey.toLowerCase(), fetcherValue);
    }

    @Override
    public void removeFromFetcher(String fetcherKey) {
        this.fetcherMap.removeAsync(fetcherKey.toLowerCase());
    }

    @Override
    public void removeFromFetcher(String fetcherKey, String fetcherValue) {
        this.fetcherMap.removeAsync(fetcherKey.toLowerCase(), fetcherValue);
    }

    @Override
    public boolean containsKeyInFetcher(String fetcherKey) {
        return this.fetcherMap.containsKey(fetcherKey.toLowerCase());
    }

    @Override
    public FutureAction<Boolean> containsKeyInFetcherAsync(String fetcherKey) {
        return new FutureAction<>(this.fetcherMap.containsKeyAsync(fetcherKey.toLowerCase()));
    }

    @Override
    public FutureAction<String> getFetcherValueAsync(String fetcherKey) {
        return new FutureAction<>(this.fetcherMap.getAsync(fetcherKey.toLowerCase()));
    }

    @Override
    public String getFetcherValue(String fetcherKey) {
        return this.fetcherMap.get(fetcherKey.toLowerCase());
    }

    @Override
    public I getFromFetcher(String fetcherKey){
        String value = getFetcherValue(fetcherKey);
        return this.getBucket(value);
    }

    @Override
    public FutureAction<I> getFromFetcherAsync(String fetcherKey){
        FutureAction<I> futureAction = new FutureAction<>();

        this.getFetcherValueAsync(fetcherKey)
            .onFailure(futureAction)
            .onSuccess(value -> {
                getBucketAsync(value)
                    .onFailure(futureAction)
                    .onSuccess(futureAction::complete);
            });

        return futureAction;
    }

    @Override
    public I createBucket(String identifier, I object) {
        Predicates.illegalArgument(identifier.contains(":"), "Identifier cannot contain ':'");
        if (existsBucket(identifier))
            throw new IllegalArgumentException("Bucket[" + getRedisKey(identifier) + "] already exists");
        getClient().getBucket(getRedisKey(identifier), getObjectCodec()).set(object);

        object.setManager(this);

        this.getCachedBucketObjects().put(identifier, (T) object);
        this.putInFetcher(object.getFetchKey(), object.getFetchValue());

        object.init();
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

                                this.getCachedBucketObjects().put(identifier, (T) object);
                                this.putInFetcher(object.getFetchKey(), object.getFetchValue());

                                object.init();
                                futureAction.complete(object);
                            });
                });

        return futureAction;
    }

    @Override
    public boolean deleteBucket(I object) {
        String identifier = object.getIdentifier();
        Predicates.illegalArgument(identifier.contains(":"), "Identifier cannot contain ':'");
        if (this.isCached(identifier)) {
            this.getCachedBucketObjects().remove(identifier);
        }
        CloudAPI.getInstance().getConsole().trace("Deleting bucket: " + getRedisKey(identifier));

        BucketDeletePacket packet = new BucketDeletePacket();
        packet.setRedisPrefix(this.getRedisPrefix());
        packet.setIdentifier(identifier);
        packet.publishAll();

        getClient().getBucket(getRedisKey(identifier), getObjectCodec()).delete();
        removeFromFetcher(identifier);

        return true;
    }

    @Override
    public FutureAction<Boolean> deleteBucketAsync(I object) {
        String identifier = object.getIdentifier();
        Predicates.illegalArgument(identifier.contains(":"), "Identifier cannot contain ':'");
        CloudAPI.getInstance().getConsole().trace("Deleting bucket: " + getRedisKey(identifier));

        BucketDeletePacket packet = new BucketDeletePacket();
        packet.setRedisPrefix(this.getRedisPrefix());
        packet.setIdentifier(identifier);
        packet.publishAllAsync();

        this.getCachedBucketObjects().remove(identifier);
        removeFromFetcher(object.getFetchKey());

        return new FutureAction<>(getClient().getBucket(getRedisKey(identifier), getObjectCodec()).deleteAsync());
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
