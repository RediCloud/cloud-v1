package net.suqatri.cloud.api.service.version;

import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.commons.function.future.FutureAction;

import java.util.Collection;

public interface ICloudServiceVersionManager {

    IRBucketHolder<ICloudServiceVersion> getServiceVersion(String identifier);
    FutureAction<IRBucketHolder<ICloudServiceVersion>> getServiceVersionAsync(String identifier);

    IRBucketHolder<ICloudServiceVersion> createServiceVersion(ICloudServiceVersion version);
    FutureAction<IRBucketHolder<ICloudServiceVersion>> createServiceVersionAsync(ICloudServiceVersion version);

    Collection<IRBucketHolder<ICloudServiceVersion>> getServiceVersions();
    FutureAction<Collection<IRBucketHolder<ICloudServiceVersion>>> getServiceVersionsAsync();

}
