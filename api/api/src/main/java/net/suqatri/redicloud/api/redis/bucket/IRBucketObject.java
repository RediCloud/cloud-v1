package net.suqatri.redicloud.api.redis.bucket;

import net.suqatri.redicloud.api.redis.IRUpdateAble;
import net.suqatri.redicloud.commons.function.future.FutureAction;

import java.io.Serializable;

public interface IRBucketObject extends Serializable, IRUpdateAble {

    IRBucketHolder<IRBucketObject> getHolder();

    void update();
    FutureAction<Void> updateAsync();
    void merged();

}
