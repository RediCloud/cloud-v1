package net.suqatri.cloud.api.impl.service.version;

import net.suqatri.cloud.api.impl.redis.bucket.RedissonBucketManager;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.service.version.ICloudServiceVersion;
import net.suqatri.cloud.api.service.version.ICloudServiceVersionManager;
import net.suqatri.cloud.commons.function.future.FutureAction;

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
    public IRBucketHolder<ICloudServiceVersion> createServiceVersion(ICloudServiceVersion version) {
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
}
