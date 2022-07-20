package net.suqatri.cloud.api.service;

import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.service.configuration.IServiceStartConfiguration;
import net.suqatri.cloud.api.service.factory.ICloudServiceFactory;
import net.suqatri.cloud.commons.function.future.FutureAction;
import org.redisson.api.RMap;

import java.util.Collection;
import java.util.UUID;

public interface ICloudServiceManager {

    RMap<String, UUID> getServiceIdFetcherMap();

    FutureAction<IRBucketHolder<ICloudService>> getServiceAsync(String name);
    FutureAction<IRBucketHolder<ICloudService>> getServiceAsync(UUID uniqueId);

    IRBucketHolder<ICloudService> getService(String name);
    IRBucketHolder<ICloudService> getService(UUID uniqueId);

    FutureAction<Collection<IRBucketHolder<ICloudService>>> getServicesAsync();
    Collection<IRBucketHolder<ICloudService>> getServices();

    FutureAction<Boolean> stopServiceAsync(UUID uniqueId, boolean force);

    FutureAction<IRBucketHolder<ICloudService>> startService(IServiceStartConfiguration configuration);

    ICloudServiceFactory getServiceFactory();

    IRBucketHolder<ICloudService> getFallbackService();

    boolean existsService(String name);
    boolean existsService(UUID uniqueId);
    FutureAction<Boolean> existsServiceAsync(String name);
    FutureAction<Boolean> existsServiceAsync(UUID uniqueId);

}
