package net.suqatri.cloud.api.service;

import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.commons.function.future.FutureAction;

import java.util.Collection;
import java.util.UUID;

public interface ICloudServiceManager {

    FutureAction<IRBucketHolder<ICloudService>> getServiceAsync(String name);
    FutureAction<IRBucketHolder<ICloudService>> getServiceAsync(UUID uniqueId);

    IRBucketHolder<ICloudService> getService(String name);
    IRBucketHolder<ICloudService> getService(UUID uniqueId);

    FutureAction<Collection<IRBucketHolder<ICloudService>>> getServicesAsync();
    Collection<IRBucketHolder<ICloudService>> getServices();

}
