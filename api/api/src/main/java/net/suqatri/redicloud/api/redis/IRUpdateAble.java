package net.suqatri.redicloud.api.redis;

import net.suqatri.redicloud.api.redis.bucket.IRBucketObject;
import net.suqatri.redicloud.commons.function.future.FutureAction;

public interface IRUpdateAble {

    IRUpdateAble update();
    FutureAction<IRBucketObject> updateAsync();

}
