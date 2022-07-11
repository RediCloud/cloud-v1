package net.suqatri.cloud.api.impl.service;

import net.suqatri.cloud.api.impl.redis.bucket.RedissonBucketManager;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.service.ICloudService;
import net.suqatri.cloud.api.service.ICloudServiceManager;
import net.suqatri.cloud.commons.function.future.FutureAction;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public class CloudServiceManager extends RedissonBucketManager<CloudService, ICloudService> implements ICloudServiceManager {

    public CloudServiceManager() {
        super("service", ICloudService.class);
    }

    @Override
    public FutureAction<IRBucketHolder<ICloudService>> getServiceAsync(String name) {
        FutureAction<IRBucketHolder<ICloudService>> futureAction = new FutureAction<>();

        getServicesAsync()
                .onFailure(futureAction)
                .onSuccess(services -> {
                    Optional<IRBucketHolder<ICloudService>> optional = services
                            .parallelStream()
                            .filter(service -> service.get().getName().equalsIgnoreCase(name))
                            .findFirst();
                    if(optional.isPresent()) {
                        futureAction.complete(optional.get());
                    } else {
                        futureAction.completeExceptionally(new IllegalArgumentException("Service not found"));
                    }
                });

        return futureAction;
    }

    @Override
    public FutureAction<IRBucketHolder<ICloudService>> getServiceAsync(UUID uniqueId) {
        return this.getBucketHolderAsync(uniqueId.toString());
    }

    @Override
    public IRBucketHolder<ICloudService> getService(String name) {
        return getServices().parallelStream().filter(service -> service.get().getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    @Override
    public IRBucketHolder<ICloudService> getService(UUID uniqueId) {
        return this.getBucketHolder(uniqueId.toString());
    }

    @Override
    public FutureAction<Collection<IRBucketHolder<ICloudService>>> getServicesAsync() {
        return this.getBucketHoldersAsync();
    }

    @Override
    public Collection<IRBucketHolder<ICloudService>> getServices() {
        return this.getBucketHolders();
    }

}
