package net.suqatri.cloud.api.impl.service;

import lombok.Getter;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.impl.redis.bucket.RedissonBucketManager;
import net.suqatri.cloud.api.impl.service.configuration.DefaultServiceStartConfiguration;
import net.suqatri.cloud.api.impl.service.packet.start.CloudFactoryServiceStartPacket;
import net.suqatri.cloud.api.impl.service.packet.start.CloudFactoryServiceStartResponseCloud;
import net.suqatri.cloud.api.impl.service.packet.stop.CloudFactoryServiceStopPacket;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.redis.event.RedisConnectedEvent;
import net.suqatri.cloud.api.service.ICloudService;
import net.suqatri.cloud.api.service.ICloudServiceManager;
import net.suqatri.cloud.api.service.configuration.IServiceStartConfiguration;
import net.suqatri.cloud.api.service.factory.ICloudServiceFactory;
import net.suqatri.cloud.commons.function.future.FutureAction;
import org.redisson.api.RMap;
import org.redisson.codec.JsonJacksonCodec;

import java.util.*;

public class CloudServiceManager extends RedissonBucketManager<CloudService, ICloudService> implements ICloudServiceManager {

    private RMap<String, String> serviceIdFetcherMap;

    public CloudServiceManager() {
        super("service", ICloudService.class);
        CloudAPI.getInstance().getEventManager().register(RedisConnectedEvent.class, event ->
                this.serviceIdFetcherMap = event.getConnection().getClient().getMap("services@idFetcher", getObjectCodec()));
    }

    @Override
    public FutureAction<Set<String>> readAllFetcherKeysAsync(){
        return new FutureAction<>(this.serviceIdFetcherMap.readAllKeySetAsync());
    }

    @Override
    public void putInFetcher(String serviceName, UUID serviceId) {
        this.serviceIdFetcherMap.putAsync(serviceName.toLowerCase(Locale.ROOT), serviceId.toString());
    }

    @Override
    public void removeFromFetcher(String serviceName) {
        this.serviceIdFetcherMap.removeAsync(serviceName.toLowerCase());
    }

    @Override
    public void removeFromFetcher(String serviceName, UUID serviceId) {
        this.serviceIdFetcherMap.removeAsync(serviceName.toLowerCase(), serviceId.toString());
    }

    @Override
    public boolean containsKeyInFetcher(String serviceName) {
        return this.serviceIdFetcherMap.containsKey(serviceName.toLowerCase());
    }

    @Override
    public FutureAction<Boolean> containsKeyInFetcherAsync(String serviceName) {
        return new FutureAction<>(this.serviceIdFetcherMap.containsKeyAsync(serviceName.toLowerCase()));
    }

    @Override
    public FutureAction<UUID> getServiceIdFromFetcherAsync(String serviceName) {
        return new FutureAction<>(this.serviceIdFetcherMap.getAsync(serviceName.toLowerCase())).map(UUID::fromString);
    }

    @Override
    public UUID getServiceIdFromFetcher(String serviceName) {
        return UUID.fromString(this.serviceIdFetcherMap.get(serviceName.toLowerCase()));
    }

    @Override
    public FutureAction<IRBucketHolder<ICloudService>> getServiceAsync(String name) {
        FutureAction<IRBucketHolder<ICloudService>> futureAction = new FutureAction<>();

        this.containsKeyInFetcherAsync(name)
                .onFailure(futureAction)
                .onSuccess(contains -> {
                    if(!contains) {
                        futureAction.completeExceptionally(new IllegalArgumentException("Service not found"));
                        return;
                    }
                    getServiceIdFromFetcherAsync(name)
                            .onFailure(futureAction)
                            .onSuccess(serviceId -> getServiceAsync(serviceId)
                                    .onFailure(futureAction)
                                    .onSuccess(futureAction::complete));
        });

        return futureAction;
    }

    @Override
    public FutureAction<IRBucketHolder<ICloudService>> getServiceAsync(UUID uniqueId) {
        return this.getBucketHolderAsync(uniqueId.toString()).map(holder -> {
            this.putInFetcher(holder.get().getServiceName(), holder.get().getUniqueId());
            return holder;
        });
    }

    @Override
    public IRBucketHolder<ICloudService> getService(String name) {
        if(!containsKeyInFetcher(name)) return null;
        return getService(getServiceIdFromFetcher(name));
    }

    @Override
    public IRBucketHolder<ICloudService> getService(UUID uniqueId) {
        IRBucketHolder<ICloudService> holder = getBucketHolder(uniqueId.toString());
        putInFetcher(holder.get().getServiceName(), holder.get().getUniqueId());
        return holder;
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
        DefaultServiceStartConfiguration.fromInterface(configuration)
                .onFailure(futureAction)
                .onSuccess(configuration1 -> {
                    CloudFactoryServiceStartPacket packet = new CloudFactoryServiceStartPacket();
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
    public IRBucketHolder<ICloudService> getFallbackService() {
        IRBucketHolder<ICloudService> fallbackHolder = null;
        for (IRBucketHolder<ICloudService> serviceHolder : getServices()) {
            if(!serviceHolder.get().getConfiguration().isFallback()) continue;
            if(serviceHolder.get().getOnlineCount() < serviceHolder.get().getMaxPlayers()) continue;
            if(fallbackHolder == null){
                fallbackHolder = serviceHolder;
                continue;
            }
            if(fallbackHolder.get().getOnlineCount() > serviceHolder.get().getOnlineCount()) {
                fallbackHolder = serviceHolder;
            }
        }
        return fallbackHolder;
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
        return containsKeyInFetcherAsync(name);
    }

    @Override
    public FutureAction<Boolean> existsServiceAsync(UUID uniqueId) {
        return this.existsBucketAsync(uniqueId.toString());
    }
}
