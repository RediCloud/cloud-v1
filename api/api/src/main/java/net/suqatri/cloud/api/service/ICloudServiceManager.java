package net.suqatri.cloud.api.service;

import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.service.configuration.IServiceStartConfiguration;
import net.suqatri.cloud.api.service.factory.ICloudServiceFactory;
import net.suqatri.cloud.commons.function.future.FutureAction;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

public interface ICloudServiceManager {

    FutureAction<IRBucketHolder<ICloudService>> getServiceAsync(String name);
    FutureAction<IRBucketHolder<ICloudService>> getServiceAsync(UUID uniqueId);

    IRBucketHolder<ICloudService> getService(String name);
    IRBucketHolder<ICloudService> getService(UUID uniqueId);

    FutureAction<Collection<IRBucketHolder<ICloudService>>> getServicesAsync();
    Collection<IRBucketHolder<ICloudService>> getServices();

    FutureAction<Boolean> stopServiceAsync(UUID uniqueId, boolean force);
    boolean stopService(UUID uniqueId, boolean force) throws Exception;

    FutureAction<IRBucketHolder<ICloudService>> startServiceAsync(IServiceStartConfiguration configuration);
    IRBucketHolder<ICloudService> startService(IServiceStartConfiguration configuration) throws Exception;

    ICloudServiceFactory getServiceFactory();

}
