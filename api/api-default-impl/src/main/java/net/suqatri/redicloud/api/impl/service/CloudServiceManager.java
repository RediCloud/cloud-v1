package net.suqatri.redicloud.api.impl.service;

import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.redis.bucket.RedissonBucketManager;
import net.suqatri.redicloud.api.impl.service.configuration.DefaultServiceStartConfiguration;
import net.suqatri.redicloud.api.impl.service.packet.command.CloudServiceConsoleCommandPacket;
import net.suqatri.redicloud.api.impl.service.packet.start.CloudFactoryServiceStartPacket;
import net.suqatri.redicloud.api.impl.service.packet.start.CloudFactoryServiceStartResponseCloud;
import net.suqatri.redicloud.api.impl.service.packet.stop.CloudFactoryServiceStopPacket;
import net.suqatri.redicloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.redicloud.api.redis.event.RedisConnectedEvent;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.api.service.ICloudServiceManager;
import net.suqatri.redicloud.api.service.configuration.IServiceStartConfiguration;
import net.suqatri.redicloud.api.service.factory.ICloudServiceFactory;
import net.suqatri.redicloud.commons.function.future.FutureAction;
import org.redisson.api.RMap;

import java.util.*;
import java.util.stream.Collectors;

public abstract class CloudServiceManager extends RedissonBucketManager<CloudService, ICloudService> implements ICloudServiceManager {

    private RMap<String, String> serviceIdFetcherMap;

    public CloudServiceManager() {
        super("service", ICloudService.class);
        CloudAPI.getInstance().getEventManager().register(RedisConnectedEvent.class, event ->
                this.serviceIdFetcherMap = event.getConnection().getClient().getMap("services@idFetcher", getObjectCodec()));
    }

    @Override
    public FutureAction<Set<String>> readAllFetcherKeysAsync() {
        return new FutureAction<>(this.serviceIdFetcherMap.readAllKeySetAsync());
    }

    @Override
    public void putInFetcher(String serviceName, UUID serviceId) {
        this.serviceIdFetcherMap.putAsync(serviceName.toLowerCase(), serviceId.toString());
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
                    if (!contains) {
                        futureAction.completeExceptionally(new IllegalArgumentException(name + " service not found"));
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
        if (!containsKeyInFetcher(name)) return null;
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
        return this.getBucketHoldersAsync().map(services -> {
            for (IRBucketHolder<ICloudService> service : services) {
                putInFetcher(service.get().getServiceName(), service.get().getUniqueId());
            }
            return services;
        });
    }

    @Override
    public Collection<IRBucketHolder<ICloudService>> getServices() {
        Collection<IRBucketHolder<ICloudService>> services = getBucketHolders();
        for (IRBucketHolder<ICloudService> service : services) {
            putInFetcher(service.get().getServiceName(), service.get().getUniqueId());
        }
        return services;
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
                                CloudAPI.getInstance().getServiceManager().getServiceAsync(((CloudFactoryServiceStartResponseCloud) response).getServiceId())
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
    public final IRBucketHolder<ICloudService> getFallbackService(IRBucketHolder<ICloudService>... blacklisted) {
        IRBucketHolder<ICloudService> fallbackHolder = null;
        List<UUID> blackList = Arrays.asList(blacklisted).parallelStream().map(holder -> holder.get().getUniqueId()).collect(Collectors.toList());
        for (IRBucketHolder<ICloudService> serviceHolder : getServices()) {
            if (blackList.contains(serviceHolder.get().getUniqueId())) continue;
            if (!serviceHolder.get().getConfiguration().isFallback()) continue;
            if (serviceHolder.get().getOnlineCount() >= serviceHolder.get().getMaxPlayers()) continue;
            if (fallbackHolder == null) {
                fallbackHolder = serviceHolder;
                continue;
            }
            if (fallbackHolder.get().getOnlineCount() > serviceHolder.get().getOnlineCount()) {
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

    @Override
    public boolean executeCommand(IRBucketHolder<ICloudService> serviceHolder, String command) {
        if(!serviceHolder.get().getNetworkComponentInfo().equals(CloudAPI.getInstance().getNetworkComponentInfo())) {
            CloudServiceConsoleCommandPacket packet = new CloudServiceConsoleCommandPacket();
            packet.setServiceId(serviceHolder.get().getUniqueId());
            packet.setCommand(command);
            packet.getPacketData().addReceiver(serviceHolder.get().getNetworkComponentInfo());
            packet.publishAsync();
            return true;
        }
        return false;
    }
}
