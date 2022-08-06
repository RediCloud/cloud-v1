package net.suqatri.redicloud.api.redis.bucket;

import net.suqatri.redicloud.api.redis.IRUpdateAble;
import net.suqatri.redicloud.commons.function.future.FutureAction;

import java.io.Serializable;

public interface IRBucketObject extends Serializable, IRUpdateAble {

    IRBucketObject update();

    FutureAction<IRBucketObject> updateAsync();

    String getRedisKey();

    String getRedisPrefix();

    String getIdentifier();

    default void merged(){}

    IRedissonBucketManager getManager();
    void setManager(IRedissonBucketManager manager);

}
