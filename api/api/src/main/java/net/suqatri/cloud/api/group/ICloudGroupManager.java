package net.suqatri.cloud.api.group;

import net.suqatri.cloud.api.node.ICloudNode;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.commons.function.future.FutureAction;

import java.util.Collection;
import java.util.UUID;

public interface ICloudGroupManager {

    FutureAction<IRBucketHolder<ICloudGroup>> getGroupAsync(UUID uniqueId);
    IRBucketHolder<ICloudGroup> getGroup(UUID uniqueId);

    FutureAction<IRBucketHolder<ICloudGroup>> getGroupAsync(String name);
    IRBucketHolder<ICloudGroup> getGroup(String name);

    FutureAction<Boolean> existsGroupAsync(UUID uniqueId);
    boolean existsGroup(UUID uniqueId);

    FutureAction<Boolean> existsGroupAsync(String name);
    boolean existsGroup(String name);

    Collection<IRBucketHolder<ICloudGroup>> getGroups();
    FutureAction<Collection<IRBucketHolder<ICloudGroup>>> getGroupsAsync();

}
