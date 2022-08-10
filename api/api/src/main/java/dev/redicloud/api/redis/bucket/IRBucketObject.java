package dev.redicloud.api.redis.bucket;

import dev.redicloud.api.redis.IRUpdateAble;
import dev.redicloud.commons.function.future.FutureAction;

import java.io.Serializable;

public interface IRBucketObject extends Serializable, IRUpdateAble {

    IRBucketObject update();

    FutureAction<IRBucketObject> updateAsync();

    String getRedisKey();

    String getRedisPrefix();

    String getIdentifier();

    default void merged(){}
    default void init(){}

    IRedissonBucketManager getManager();
    void setManager(IRedissonBucketManager manager);

}
