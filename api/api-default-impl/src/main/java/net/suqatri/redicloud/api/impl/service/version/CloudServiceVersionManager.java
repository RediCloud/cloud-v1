package net.suqatri.redicloud.api.impl.service.version;

import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.group.ICloudGroup;
import net.suqatri.redicloud.api.impl.redis.bucket.RedissonBucketManager;
import net.suqatri.redicloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.redicloud.api.service.version.ICloudServiceVersion;
import net.suqatri.redicloud.api.service.version.ICloudServiceVersionManager;
import net.suqatri.redicloud.commons.function.future.FutureAction;

import java.io.IOException;
import java.util.Collection;

public class CloudServiceVersionManager extends RedissonBucketManager<CloudServiceVersion, ICloudServiceVersion> implements ICloudServiceVersionManager {

    public CloudServiceVersionManager() {
        super("serviceVersion", ICloudServiceVersion.class);
    }

    @Override
    public IRBucketHolder<ICloudServiceVersion> getServiceVersion(String identifier) {
        return this.getBucketHolder(identifier);
    }

    @Override
    public FutureAction<IRBucketHolder<ICloudServiceVersion>> getServiceVersionAsync(String identifier) {
        return this.getBucketHolderAsync(identifier);
    }

    @Override
    public IRBucketHolder<ICloudServiceVersion> createServiceVersion(ICloudServiceVersion version) throws IOException, InterruptedException {
        return this.createBucket(version.getName(), version);
    }

    @Override
    public FutureAction<IRBucketHolder<ICloudServiceVersion>> createServiceVersionAsync(ICloudServiceVersion version) {
        return this.createBucketAsync(version.getName(), version);
    }

    @Override
    public Collection<IRBucketHolder<ICloudServiceVersion>> getServiceVersions() {
        return this.getBucketHolders();
    }

    @Override
    public FutureAction<Collection<IRBucketHolder<ICloudServiceVersion>>> getServiceVersionsAsync() {
        return this.getBucketHoldersAsync();
    }

    @Override
    public boolean existsServiceVersion(String name) {
        return this.existsBucket(name);
    }

    @Override
    public FutureAction<Boolean> existsServiceVersionAsync(String name) {
        return this.existsBucketAsync(name);
    }

    @Override
    public boolean deleteServiceVersion(String name) {
        StringBuilder builder = new StringBuilder();
        for (IRBucketHolder<ICloudGroup> holder : CloudAPI.getInstance().getGroupManager().getGroups()) {
            if (holder.get().getServiceVersionName().equalsIgnoreCase(name)) {
                if (!builder.toString().isEmpty()) builder.append(", ");
                builder.append(holder.get().getName());
            }
        }
        if (!builder.toString().isEmpty())
            throw new IllegalStateException("Service version " + name + " is still in use by groups: " + builder.toString());
        return this.deleteBucket(name);
    }

    @Override
    public FutureAction<Boolean> deleteServiceVersionAsync(String name) {
        FutureAction<Boolean> futureAction = new FutureAction<>();
        CloudAPI.getInstance().getGroupManager().getGroupsAsync()
                .onFailure(futureAction)
                .onSuccess(holders -> {
                    StringBuilder builder = new StringBuilder();
                    for (IRBucketHolder<ICloudGroup> holder : holders) {
                        if (holder.get().getServiceVersionName().equalsIgnoreCase(name)) {
                            if (!builder.toString().isEmpty()) builder.append(", ");
                            builder.append(holder.get().getName());
                        }
                    }
                    if (!builder.toString().isEmpty()) {
                        futureAction.completeExceptionally(new IllegalStateException("Service version " + name + " is still in use by groups: " + builder.toString()));
                        return;
                    }
                    this.deleteBucketAsync(name)
                            .onFailure(futureAction)
                            .onSuccess(futureAction::complete);
                });
        return futureAction;
    }

    //TODO packet to node
    @Override
    public boolean patch(IRBucketHolder<ICloudServiceVersion> holder, boolean force) throws IOException, InterruptedException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    //TODO packet to node
    @Override
    public FutureAction<Boolean> patchAsync(IRBucketHolder<ICloudServiceVersion> holder, boolean force) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    //TODO packet to node
    @Override
    public boolean download(IRBucketHolder<ICloudServiceVersion> holder, boolean force) throws IOException, InterruptedException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    //TODO packet to node
    @Override
    public FutureAction<Boolean> downloadAsync(IRBucketHolder<ICloudServiceVersion> holder, boolean force) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
