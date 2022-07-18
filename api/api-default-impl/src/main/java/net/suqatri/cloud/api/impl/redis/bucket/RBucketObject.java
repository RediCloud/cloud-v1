package net.suqatri.cloud.api.impl.redis.bucket;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Setter;
import net.suqatri.cloud.api.redis.bucket.IRBucketObject;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.commons.function.future.FutureAction;

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
    public void merged(){

    }

}
