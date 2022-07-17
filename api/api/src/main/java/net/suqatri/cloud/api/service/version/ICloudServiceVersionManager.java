package net.suqatri.cloud.api.service.version;

import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.commons.function.future.FutureAction;

import java.io.IOException;
import java.util.Collection;

public interface ICloudServiceVersionManager {

    IRBucketHolder<ICloudServiceVersion> getServiceVersion(String name);
    FutureAction<IRBucketHolder<ICloudServiceVersion>> getServiceVersionAsync(String name);

    IRBucketHolder<ICloudServiceVersion> createServiceVersion(ICloudServiceVersion version) throws IOException, InterruptedException;
    FutureAction<IRBucketHolder<ICloudServiceVersion>> createServiceVersionAsync(ICloudServiceVersion version);

    Collection<IRBucketHolder<ICloudServiceVersion>> getServiceVersions();
    FutureAction<Collection<IRBucketHolder<ICloudServiceVersion>>> getServiceVersionsAsync();

    boolean existsServiceVersion(String name);
    FutureAction<Boolean> existsServiceVersionAsync(String name);

    boolean deleteServiceVersion(String name);
    FutureAction<Boolean> deleteServiceVersionAsync(String name);

    boolean patch(IRBucketHolder<ICloudServiceVersion> holder, boolean force) throws IOException, InterruptedException;
    FutureAction<Boolean> patchAsync(IRBucketHolder<ICloudServiceVersion> holder, boolean force);

    boolean download(IRBucketHolder<ICloudServiceVersion> holder, boolean force) throws IOException, InterruptedException;
    FutureAction<Boolean> downloadAsync(IRBucketHolder<ICloudServiceVersion> holder, boolean force);

}
