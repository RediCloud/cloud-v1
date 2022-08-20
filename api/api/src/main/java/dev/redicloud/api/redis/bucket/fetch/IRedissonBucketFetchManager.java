package dev.redicloud.api.redis.bucket.fetch;


import dev.redicloud.api.redis.bucket.IRedissonBucketManager;
import dev.redicloud.commons.function.future.FutureAction;
import org.redisson.api.RMap;

import java.util.Set;

public interface IRedissonBucketFetchManager<T extends I, I extends IRBucketFetchAble> extends IRedissonBucketManager<T, I> {

    RMap<String, String> getFetcherMap();

    FutureAction<Set<String>> readAllFetcherKeysAsync();
    void putInFetcher(String fetchKey, String fetchValue);
    void removeFromFetcher(String fetchKey);
    void removeFromFetcher(String fetchKey, String fetchValue);
    boolean containsKeyInFetcher(String fetchKey);
    FutureAction<Boolean> containsKeyInFetcherAsync(String fetchKey);
    FutureAction<String> getFetcherValueAsync(String fetchKey);
    String getFetcherValue(String fetchKey);

    I getFromFetcher(String fetchKey);
    FutureAction<I> getFromFetcherAsync(String fetchKey);



}
