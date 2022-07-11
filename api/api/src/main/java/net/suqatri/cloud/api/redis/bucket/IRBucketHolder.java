package net.suqatri.cloud.api.redis.bucket;

import net.suqatri.cloud.commons.function.future.FutureAction;

public interface IRBucketHolder<I> {

    default I get() {
        return this.get(false);
    }
    I get(boolean force);
    default <T extends I> T getImpl(Class<T> clazz){
        return clazz.cast(this.get());
    }

    IRBucketHolder<I> update(I object);
    FutureAction<IRBucketHolder<I>> updateAsync(I object);

    void unlink();

    void mergeChanges(String json);

    String getRedisKey();
    String getRedisPrefix();
    String getIdentifier();

}
