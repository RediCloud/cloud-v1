package net.suqatri.cloud.api.group;

import net.suqatri.cloud.api.node.ICloudNode;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.commons.function.future.FutureAction;

import java.util.Collection;
import java.util.UUID;

public interface ICloudGroupManager<T extends ICloudGroup> {

    FutureAction<IRBucketHolder<T>> getGroupAsync(UUID uniqueId);
    IRBucketHolder<T> getGroup(UUID uniqueId);

    FutureAction<IRBucketHolder<T>> getGroupAsync(String name);
    IRBucketHolder<T> getGroup(String name);

    FutureAction<Boolean> existsGroupAsync(UUID uniqueId);
    boolean existsGroup(UUID uniqueId);

    FutureAction<Boolean> existsGroupAsync(String name);
    boolean existsGroup(String name);

    Collection<IRBucketHolder<T>> getGroups();
    FutureAction<Collection<IRBucketHolder<T>>> getGroupsAsync();

}
