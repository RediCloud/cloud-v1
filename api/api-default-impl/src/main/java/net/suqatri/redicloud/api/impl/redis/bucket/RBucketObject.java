package net.suqatri.redicloud.api.impl.redis.bucket;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Setter;
import net.suqatri.redicloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.redicloud.api.redis.bucket.IRBucketObject;
import net.suqatri.redicloud.commons.function.future.FutureAction;

public abstract class RBucketObject implements IRBucketObject {

    @JsonIgnore
    @Setter
    private RBucketHolder holder;

    @Override
    public IRBucketHolder getHolder() {
        return this.holder;
    }

    @Override
    public void update() {
        getHolder().update(this);
    }

    @Override
    public FutureAction<Void> updateAsync() {
        return getHolder().updateAsync(this).map(v -> null);
    }

    @Override
    public void merged() {

    }

}
