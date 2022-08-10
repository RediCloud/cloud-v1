package dev.redicloud.api.redis;

import dev.redicloud.api.redis.bucket.IRBucketObject;
import dev.redicloud.commons.function.future.FutureAction;

public interface IRUpdateAble {

    IRUpdateAble update();
    FutureAction<IRBucketObject> updateAsync();

}
