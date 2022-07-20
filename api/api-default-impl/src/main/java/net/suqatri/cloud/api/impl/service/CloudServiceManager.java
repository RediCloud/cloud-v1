package net.suqatri.cloud.api.impl.service;

import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.impl.redis.bucket.RedissonBucketManager;
import net.suqatri.cloud.api.impl.service.configuration.DefaultServiceStartConfiguration;
import net.suqatri.cloud.api.impl.service.packet.start.CloudFactoryServiceStartPacket;
import net.suqatri.cloud.api.impl.service.packet.start.CloudFactoryServiceStartResponseCloud;
import net.suqatri.cloud.api.impl.service.packet.stop.CloudFactoryServiceStopPacket;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.service.ICloudService;
import net.suqatri.cloud.api.service.ICloudServiceManager;
import net.suqatri.cloud.api.service.configuration.IServiceStartConfiguration;
import net.suqatri.cloud.api.service.factory.ICloudServiceFactory;
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
                            .filter(service -> service.get().getServiceName().equalsIgnoreCase(name))
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
        return getServices().parallelStream().filter(service -> service.get().getServiceName().equalsIgnoreCase(name)).findFirst().orElse(null);
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

    @Override
    public FutureAction<IRBucketHolder<ICloudService>> startService(IServiceStartConfiguration configuration) {
        FutureAction<IRBucketHolder<ICloudService>> futureAction = new FutureAction<>();
        CloudFactoryServiceStartPacket packet = new CloudFactoryServiceStartPacket();
        DefaultServiceStartConfiguration.fromInterface(configuration)
                .onFailure(futureAction)
                .onSuccess(configuration1 -> {
                    packet.setConfiguration(configuration1);
                    packet.setAsync(true);
                    packet.getPacketData().waitForResponse()
                            .onFailure(futureAction)
                            .onSuccess(response -> {
                                CloudAPI.getInstance().getServiceManager().getServiceAsync(((CloudFactoryServiceStartResponseCloud)response).getServiceId())
                                        .onFailure(futureAction)
                                        .onSuccess(futureAction::complete);
                            });
                    packet.publishAsync();
                });
        return futureAction;
    }

    @Override
    public FutureAction<Boolean> stopServiceAsync(UUID uniqueId, boolean force) {
        FutureAction<Boolean> futureAction = new FutureAction<>();

        CloudFactoryServiceStopPacket packet = new CloudFactoryServiceStopPacket();
        packet.setServiceId(uniqueId);
        packet.setForce(force);
        packet.getPacketData().waitForResponse()
                .onFailure(futureAction)
                .onSuccess(response -> {
                    futureAction.complete(true);
                });
        packet.publishAsync();

        return futureAction;
    }


    @Override
    public ICloudServiceFactory getServiceFactory() {
        return CloudAPI.getInstance().getServiceFactory();
    }

    @Override
    public boolean existsService(String name) {
        return getServices().parallelStream().anyMatch(service -> service.get().getServiceName().equalsIgnoreCase(name));
    }

    @Override
    public boolean existsService(UUID uniqueId) {
        return this.existsBucket(uniqueId.toString());
    }

    @Override
    public FutureAction<Boolean> existsServiceAsync(String name) {
        return getServicesAsync().map(services -> services.parallelStream().anyMatch(service -> service.get().getServiceName().equalsIgnoreCase(name)));
    }

    @Override
    public FutureAction<Boolean> existsServiceAsync(UUID uniqueId) {
        return this.existsBucketAsync(uniqueId.toString());
    }
}
