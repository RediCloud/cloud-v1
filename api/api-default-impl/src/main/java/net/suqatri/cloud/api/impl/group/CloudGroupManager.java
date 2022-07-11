package net.suqatri.cloud.api.impl.group;

import net.suqatri.cloud.api.group.ICloudGroup;
import net.suqatri.cloud.api.group.ICloudGroupManager;
import net.suqatri.cloud.api.impl.redis.bucket.RedissonBucketManager;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.commons.function.future.FutureAction;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Future;

public class CloudGroupManager extends RedissonBucketManager<CloudGroup, ICloudGroup> implements ICloudGroupManager {

    public CloudGroupManager() {
        super("group", ICloudGroup.class);
    }

    @Override
    public FutureAction<IRBucketHolder<ICloudGroup>> getGroupAsync(UUID uniqueId) {
        return this.getBucketHolderAsync(uniqueId.toString());
    }

    @Override
    public IRBucketHolder<ICloudGroup> getGroup(UUID uniqueId) {
        return this.getBucketHolder(uniqueId.toString());
    }

    @Override
    public FutureAction<IRBucketHolder<ICloudGroup>> getGroupAsync(String name) {
        FutureAction<IRBucketHolder<ICloudGroup>> futureAction = new FutureAction<>();

        getGroupsAsync()
                .onFailure(futureAction)
                .onSuccess(groups -> {
                    Optional<IRBucketHolder<ICloudGroup>> optional = groups
                            .parallelStream()
                            .filter(group -> group.get().getName().equalsIgnoreCase(name))
                            .findFirst();
                    if(optional.isPresent()) {
                        futureAction.complete(optional.get());
                    } else {
                        futureAction.completeExceptionally(new IllegalArgumentException("Group not found"));
                    }
                });

        return futureAction;
    }

    @Override
    public IRBucketHolder<ICloudGroup> getGroup(String name) {
        return getGroups().parallelStream().filter(group -> group.get().getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    @Override
    public FutureAction<Boolean> existsGroupAsync(UUID uniqueId) {
        return this.existsBucketAsync(uniqueId.toString());
    }

    @Override
    public boolean existsGroup(UUID uniqueId) {
        return this.existsBucket(uniqueId.toString());
    }

    @Override
    public FutureAction<Boolean> existsGroupAsync(String name) {
        FutureAction<Boolean> futureAction = new FutureAction<>();

        getGroupAsync(name)
                .onFailure(e -> {
                    if(e instanceof NullPointerException) {
                        futureAction.complete(false);
                    } else {
                        futureAction.completeExceptionally(e);
                    }
                })
                .onSuccess(group -> futureAction.complete(true));

        return futureAction;
    }

    @Override
    public boolean existsGroup(String name) {
        return getGroup(name) != null;
    }

    @Override
    public Collection<IRBucketHolder<ICloudGroup>> getGroups() {
        return this.getBucketHolders();
    }

    @Override
    public FutureAction<Collection<IRBucketHolder<ICloudGroup>>> getGroupsAsync() {
        return this.getBucketHoldersAsync();
    }
}
