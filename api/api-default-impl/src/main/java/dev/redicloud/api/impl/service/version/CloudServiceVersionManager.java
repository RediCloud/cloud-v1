package dev.redicloud.api.impl.service.version;

import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.group.ICloudGroup;
import dev.redicloud.api.impl.redis.bucket.RedissonBucketManager;
import dev.redicloud.api.service.version.ICloudServiceVersion;
import dev.redicloud.api.service.version.ICloudServiceVersionManager;
import dev.redicloud.commons.function.future.FutureAction;

import java.io.IOException;
import java.util.Collection;

public class CloudServiceVersionManager extends RedissonBucketManager<CloudServiceVersion, ICloudServiceVersion> implements ICloudServiceVersionManager {

    public CloudServiceVersionManager() {
        super("serviceVersion", CloudServiceVersion.class);
    }

    @Override
    public ICloudServiceVersion getServiceVersion(String identifier) {
        return this.get(identifier);
    }

    @Override
    public FutureAction<ICloudServiceVersion> getServiceVersionAsync(String identifier) {
        return this.getAsync(identifier);
    }

    @Override
    public ICloudServiceVersion createServiceVersion(ICloudServiceVersion version, boolean fullInstall) throws IOException, InterruptedException {
        return this.createBucket(version.getName().toLowerCase(), version);
    }

    @Override
    public FutureAction<ICloudServiceVersion> createServiceVersionAsync(ICloudServiceVersion version, boolean fullInstall) {
        return this.createBucketAsync(version.getName().toLowerCase(), version);
    }

    @Override
    public Collection<ICloudServiceVersion> getServiceVersions() {
        return this.getBucketHolders();
    }

    @Override
    public FutureAction<Collection<ICloudServiceVersion>> getServiceVersionsAsync() {
        return this.getBucketHoldersAsync();
    }

    @Override
    public boolean existsServiceVersion(String name) {
        return this.existsBucket(name.toLowerCase());
    }

    @Override
    public FutureAction<Boolean> existsServiceVersionAsync(String name) {
        return this.existsBucketAsync(name.toLowerCase());
    }

    @Override
    public boolean deleteServiceVersion(String name) {
        name = name.toLowerCase();
        StringBuilder builder = new StringBuilder();
        for (ICloudGroup holder : CloudAPI.getInstance().getGroupManager().getGroups()) {
            if (holder.getServiceVersionName().equalsIgnoreCase(name)) {
                if (!builder.toString().isEmpty()) builder.append(", ");
                builder.append(holder.getName());
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
                    for (ICloudGroup holder : holders) {
                        if (holder.getServiceVersionName().equalsIgnoreCase(name.toLowerCase())) {
                            if (!builder.toString().isEmpty()) builder.append(", ");
                            builder.append(holder.getName());
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
    public boolean patch(ICloudServiceVersion holder, boolean force) throws IOException, InterruptedException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    //TODO packet to node
    @Override
    public FutureAction<Boolean> patchAsync(ICloudServiceVersion holder, boolean force) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    //TODO packet to node
    @Override
    public boolean download(ICloudServiceVersion holder, boolean force) throws IOException, InterruptedException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    //TODO packet to node
    @Override
    public FutureAction<Boolean> downloadAsync(ICloudServiceVersion holder, boolean force) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
