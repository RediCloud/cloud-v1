package net.suqatri.cloud.api.redis.bucket;

import net.suqatri.cloud.api.redis.IRUpdateAble;
import net.suqatri.cloud.commons.function.future.FutureAction;

import java.io.Serializable;

public interface IRBucketObject extends Serializable, IRUpdateAble {

    IRBucketHolder<IRBucketObject> getHolder();

    void update();
    FutureAction<Void> updateAsync();
    void merged();

}
