package net.suqatri.redicloud.api.impl.service;

import lombok.Getter;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.redis.bucket.RedissonBucketManager;
import net.suqatri.redicloud.api.impl.service.configuration.DefaultServiceStartConfiguration;
import net.suqatri.redicloud.api.impl.service.packet.command.CloudServiceConsoleCommandPacket;
import net.suqatri.redicloud.api.impl.service.packet.start.CloudFactoryServiceStartPacket;
import net.suqatri.redicloud.api.impl.service.packet.start.CloudFactoryServiceStartResponseCloud;
import net.suqatri.redicloud.api.impl.service.packet.stop.CloudFactoryServiceStopPacket;
import net.suqatri.redicloud.api.player.ICloudPlayer;
import net.suqatri.redicloud.api.redis.event.RedisConnectedEvent;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.api.service.ICloudServiceManager;
import net.suqatri.redicloud.api.service.ServiceEnvironment;
import net.suqatri.redicloud.api.service.configuration.IServiceStartConfiguration;
import net.suqatri.redicloud.api.service.factory.ICloudServiceFactory;
import net.suqatri.redicloud.commons.function.future.FutureAction;
import org.redisson.api.RMap;

import java.util.*;
import java.util.stream.Collectors;

public abstract class CloudServiceManager extends RedissonBucketManager<CloudService, ICloudService> implements ICloudServiceManager {

    @Getter
    private RMap<String, String> serviceIdFetcherMap;

    public CloudServiceManager() {
        super("service", CloudService.class);
        CloudAPI.getInstance().getEventManager().register(RedisConnectedEvent.class, event ->
                this.serviceIdFetcherMap = event.getConnection().getClient().getMap("fetcher:serviceId", getObjectCodec()));
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
    public FutureAction<ICloudService> getServiceAsync(String name) {
        FutureAction<ICloudService> futureAction = new FutureAction<>();

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
    public FutureAction<ICloudService> getServiceAsync(UUID uniqueId) {
        return this.getAsync(uniqueId.toString()).map(holder -> {
            this.putInFetcher(holder.getServiceName(), holder.getUniqueId());
            return holder;
        });
    }

    @Override
    public ICloudService getService(String name) {
        if (!containsKeyInFetcher(name)) return null;
        return getService(getServiceIdFromFetcher(name));
    }

    @Override
    public ICloudService getService(UUID uniqueId) {
        ICloudService holder = get(uniqueId.toString());
        putInFetcher(holder.getServiceName(), holder.getUniqueId());
        return holder;
    }

    @Override
    public FutureAction<Collection<ICloudService>> getServicesAsync() {
        return this.getBucketHoldersAsync().map(services -> {
            for (ICloudService service : services) {
                putInFetcher(service.getServiceName(), service.getUniqueId());
            }
            return services;
        });
    }

    @Override
    public Collection<ICloudService> getServices() {
        Collection<ICloudService> services = getBucketHolders();
        for (ICloudService service : services) {
            putInFetcher(service.getServiceName(), service.getUniqueId());
        }
        return services;
    }

    @Override
    public FutureAction<ICloudService> startService(IServiceStartConfiguration configuration) {
        FutureAction<ICloudService> futureAction = new FutureAction<>();
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
    public final ICloudService getFallbackService(ICloudPlayer cloudPlayer, ICloudService... blacklisted) {
        ICloudService fallbackHolder = null;
        List<UUID> blackList = Arrays.asList(blacklisted).parallelStream().map(ICloudService::getUniqueId).collect(Collectors.toList());
        Collection<ICloudService> services = getServices();
        for (ICloudService service : services) {
            if (blackList.contains(service.getUniqueId())) continue;
            if (service.isMaintenance() && !cloudPlayer.getBridge().hasPermission("redicloud.maintenance.bypass")) continue;
            if (!service.getConfiguration().isFallback()) continue;
            if (service.getEnvironment() == ServiceEnvironment.LIMBO) continue;
            if (service.getOnlineCount() >= service.getMaxPlayers()) continue;
            if (fallbackHolder == null) {
                fallbackHolder = service;
                continue;
            }
            if (fallbackHolder.getOnlineCount() > service.getOnlineCount()) {
                fallbackHolder = service;
            }
        }
        if(fallbackHolder == null){
            for (ICloudService service : services) {
                if (blackList.contains(service.getUniqueId())) continue;
                if (service.getEnvironment() != ServiceEnvironment.LIMBO) continue;
                if (service.isMaintenance() && !cloudPlayer.getBridge().hasPermission("redicloud.maintenance.bypass")) continue;
                if (!service.getServiceName().startsWith("Fallback-")) continue;
                if (!service.getConfiguration().isFallback()) continue;
                fallbackHolder = service;
                return fallbackHolder;
            }
        }
        return fallbackHolder;
    }


    @Override
    public final ICloudService getFallbackService(boolean maintenanceByPass, ICloudService... blacklisted) {
        ICloudService fallbackHolder = null;
        List<UUID> blackList = Arrays.asList(blacklisted).parallelStream().map(ICloudService::getUniqueId).collect(Collectors.toList());
        Collection<ICloudService> services = getServices();
        for (ICloudService service : services) {
            if (blackList.contains(service.getUniqueId())) continue;
            if (service.isMaintenance() && !maintenanceByPass) continue;
            if (!service.getConfiguration().isFallback()) continue;
            if (service.getEnvironment() == ServiceEnvironment.LIMBO) continue;
            if (service.getOnlineCount() >= service.getMaxPlayers()) continue;
            if (fallbackHolder == null) {
                fallbackHolder = service;
                continue;
            }
            if (fallbackHolder.getOnlineCount() > service.getOnlineCount()) {
                fallbackHolder = service;
            }
        }
        if(fallbackHolder == null){
            for (ICloudService service : services) {
                if (blackList.contains(service.getUniqueId())) continue;
                if (service.getEnvironment() != ServiceEnvironment.LIMBO) continue;
                if (!service.getServiceName().startsWith("Fallback-")) continue;
                if (!service.getConfiguration().isFallback()) continue;
                if (service.isMaintenance() && !maintenanceByPass) continue;
                fallbackHolder = service;
                return fallbackHolder;
            }
        }
        return fallbackHolder;
    }

    @Override
    public boolean existsService(String name) {
        return getServices().parallelStream().anyMatch(service -> service.getServiceName().equalsIgnoreCase(name));
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
    public boolean executeCommand(ICloudService serviceHolder, String command) {
        if(!serviceHolder.getNetworkComponentInfo().equals(CloudAPI.getInstance().getNetworkComponentInfo())) {
            CloudServiceConsoleCommandPacket packet = new CloudServiceConsoleCommandPacket();
            packet.setServiceId(serviceHolder.getUniqueId());
            packet.setCommand(command);
            packet.getPacketData().addReceiver(serviceHolder.getNetworkComponentInfo());
            packet.publishAsync();
            return true;
        }
        return false;
    }
}
