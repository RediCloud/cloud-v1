package net.suqatri.redicloud.api.impl.group;

import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.group.ICloudGroup;
import net.suqatri.redicloud.api.group.ICloudGroupManager;
import net.suqatri.redicloud.api.impl.redis.bucket.RedissonBucketManager;
import net.suqatri.redicloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.commons.function.future.FutureAction;
import net.suqatri.redicloud.commons.function.future.FutureActionCollection;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CloudGroupManager extends RedissonBucketManager<CloudGroup, ICloudGroup> implements ICloudGroupManager {

    public CloudGroupManager() {
        super("servicegroup", ICloudGroup.class);
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
                    if (optional.isPresent()) {
                        futureAction.complete(optional.get());
                    } else {
                        futureAction.completeExceptionally(new NullPointerException("Group not found"));
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
                    if (e instanceof NullPointerException) {
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

    @Override
    public FutureAction<IRBucketHolder<ICloudGroup>> createGroupAsync(ICloudGroup group) {
        return this.createBucketAsync(group.getUniqueId().toString(), group);
    }

    @Override
    public FutureAction<Boolean> deleteGroupAsync(UUID uniqueId) {
        FutureAction<Boolean> futureAction = new FutureAction<>();
        getGroupAsync(uniqueId)
                .onFailure(futureAction)
                .onSuccess(groupHolder -> {
                    groupHolder.get().getOnlineServices()
                            .onFailure(futureAction)
                            .onSuccess(serviceHolders -> {
                                FutureActionCollection<UUID, Boolean> futureActionFutureAction = new FutureActionCollection<>();
                                for (IRBucketHolder<ICloudService> serviceHolder : serviceHolders) {
                                    futureActionFutureAction.addToProcess(serviceHolder.get().getUniqueId(), CloudAPI.getInstance().getServiceManager().stopServiceAsync(serviceHolder.get().getUniqueId(), true));
                                }
                                futureActionFutureAction.process()
                                        .onFailure(futureAction)
                                        .onSuccess(s1 -> {
                                            this.deleteBucketAsync(uniqueId.toString())
                                                    .onFailure(futureAction)
                                                    .onSuccess(s2 -> futureAction.complete(true));
                                        });
                            });
                });

        return futureAction;
    }

    @Override
    public boolean deleteGroup(UUID uniqueId) throws Exception {
        IRBucketHolder<ICloudGroup> holder = getGroup(uniqueId);
        for (IRBucketHolder<ICloudService> service : CloudAPI.getInstance().getServiceManager().getServices()) {
            if (service.get().getGroup() == null) continue;
            if (service.get().getGroupName().equalsIgnoreCase(holder.get().getName())) {
                CloudAPI.getInstance().getServiceManager().stopServiceAsync(service.get().getUniqueId(), true).get(5, TimeUnit.SECONDS);
            }
        }
        this.deleteBucket(uniqueId.toString());
        return true;
    }

    @Override
    public IRBucketHolder<ICloudGroup> createGroup(ICloudGroup group) {
        return this.createBucket(group.getUniqueId().toString(), group);
    }
}
