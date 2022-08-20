package dev.redicloud.api.impl.service;

import dev.redicloud.api.impl.redis.bucket.RedissonBucketManager;
import dev.redicloud.api.impl.redis.bucket.fetch.RedissonBucketFetchManager;
import dev.redicloud.api.impl.service.configuration.DefaultServiceStartConfiguration;
import dev.redicloud.api.impl.service.packet.start.CloudFactoryServiceStartPacket;
import dev.redicloud.api.impl.service.packet.start.CloudFactoryServiceStartResponseCloud;
import dev.redicloud.api.impl.service.packet.stop.CloudFactoryServiceStopPacket;
import lombok.Getter;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.impl.service.packet.command.CloudServiceConsoleCommandPacket;
import dev.redicloud.api.player.ICloudPlayer;
import dev.redicloud.api.redis.event.RedisConnectedEvent;
import dev.redicloud.api.service.ICloudService;
import dev.redicloud.api.service.ICloudServiceManager;
import dev.redicloud.api.service.ServiceEnvironment;
import dev.redicloud.api.service.ServiceState;
import dev.redicloud.api.service.configuration.IServiceStartConfiguration;
import dev.redicloud.api.service.factory.ICloudServiceFactory;
import dev.redicloud.commons.function.future.FutureAction;
import org.redisson.api.RMap;

import java.util.*;

public abstract class CloudServiceManager extends RedissonBucketFetchManager<CloudService, ICloudService> implements ICloudServiceManager {

    @Getter
    private RMap<String, String> serviceIdFetcherMap;

    public CloudServiceManager() {
        super("service", CloudService.class, "serviceId");
    }

    @Override
    public FutureAction<ICloudService> getServiceAsync(String name) {
        FutureAction<ICloudService> futureAction = new FutureAction<>();

        this.containsKeyInFetcherAsync(name.toLowerCase())
            .onFailure(futureAction)
            .onSuccess(contains -> {
                if (!contains) {
                    futureAction.completeExceptionally(new IllegalArgumentException(name + " service not found"));
                    return;
                }
                getFetcherValueAsync(name.toLowerCase())
                    .onFailure(futureAction)
                    .onSuccess(serviceId -> getServiceAsync(serviceId)
                        .onFailure(futureAction)
                        .onSuccess(futureAction::complete));
            });

        return futureAction;
    }

    @Override
    public FutureAction<ICloudService> getServiceAsync(UUID uniqueId) {
        return this.getAsync(uniqueId.toString());
    }

    @Override
    public ICloudService getService(String name) {
        return getService(getFetcherValue(name.toLowerCase()));
    }

    @Override
    public ICloudService getService(UUID uniqueId) {
        return get(uniqueId.toString());
    }

    @Override
    public FutureAction<Collection<ICloudService>> getServicesAsync() {
        return this.getBucketHoldersAsync();
    }

    @Override
    public Collection<ICloudService> getServices() {
        return getBucketHolders();
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
        List<UUID> blackList = new ArrayList<>();
        for (ICloudService service : blacklisted) {
            if(service == null) continue;
            blackList.add(service.getUniqueId());
        }
        Collection<ICloudService> services = getServices();
        for (ICloudService service : services) {
            if (blackList.contains(service.getUniqueId())) continue;
            if (service.isMaintenance() && !cloudPlayer.getBridge().hasPermission("redicloud.maintenance.bypass")) continue;
            if (!service.getConfiguration().isFallback()) continue;
            if (service.getServiceState() != ServiceState.RUNNING_UNDEFINED) continue;
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
                if (service.getServiceState() != ServiceState.RUNNING_UNDEFINED) continue;
                if (service.getEnvironment() != ServiceEnvironment.LIMBO) continue;
                if (!service.getServiceName().startsWith("Fallback-")) continue;
                if (!service.getConfiguration().isFallback()) continue;
                if (service.isMaintenance() && !cloudPlayer.getBridge().hasPermission("redicloud.maintenance.bypass")) continue;
                fallbackHolder = service;
                return fallbackHolder;
            }
        }
        return fallbackHolder;
    }


    @Override
    public final ICloudService getFallbackService(boolean maintenanceByPass, ICloudService... blacklisted) {
        ICloudService fallbackHolder = null;
        List<UUID> blackList = new ArrayList<>();
        for (ICloudService service : blacklisted) {
            if(service == null) continue;
            blackList.add(service.getUniqueId());
        }
        Collection<ICloudService> services = getServices();
        for (ICloudService service : services) {
            if (blackList.contains(service.getUniqueId())) continue;
            if (service.isMaintenance() && !maintenanceByPass) continue;
            if (!service.getConfiguration().isFallback()) continue;
            if (service.getServiceState() != ServiceState.RUNNING_UNDEFINED) continue;
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
                if (service.getServiceState() != ServiceState.RUNNING_UNDEFINED) continue;
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
        return containsKeyInFetcher(name.toLowerCase());
    }

    @Override
    public boolean existsService(UUID uniqueId) {
        return this.existsBucket(uniqueId.toString());
    }

    @Override
    public FutureAction<Boolean> existsServiceAsync(String name) {
        return containsKeyInFetcherAsync(name.toLowerCase());
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
