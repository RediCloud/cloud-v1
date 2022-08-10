package dev.redicloud.api.service;

import dev.redicloud.api.service.configuration.IServiceStartConfiguration;
import dev.redicloud.api.service.factory.ICloudServiceFactory;
import dev.redicloud.api.player.ICloudPlayer;
import dev.redicloud.commons.function.future.FutureAction;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public interface ICloudServiceManager {

    FutureAction<Set<String>> readAllFetcherKeysAsync();

    void putInFetcher(String serviceName, UUID serviceId);

    void removeFromFetcher(String serviceName);

    void removeFromFetcher(String serviceName, UUID serviceId);

    boolean containsKeyInFetcher(String serviceName);

    FutureAction<Boolean> containsKeyInFetcherAsync(String serviceName);

    FutureAction<UUID> getServiceIdFromFetcherAsync(String serviceName);

    UUID getServiceIdFromFetcher(String serviceName);

    FutureAction<ICloudService> getServiceAsync(String name);

    FutureAction<ICloudService> getServiceAsync(UUID uniqueId);

    ICloudService getService(String name);

    ICloudService getService(UUID uniqueId);

    FutureAction<Collection<ICloudService>> getServicesAsync();

    Collection<ICloudService> getServices();

    FutureAction<Boolean> stopServiceAsync(UUID uniqueId, boolean force);

    FutureAction<ICloudService> startService(IServiceStartConfiguration configuration);

    ICloudServiceFactory getServiceFactory();

    ICloudService getFallbackService(ICloudPlayer cloudPlayer, ICloudService... blacklist);
    ICloudService getFallbackService(boolean maintenance, ICloudService... blacklist);

    boolean existsService(String name);

    boolean existsService(UUID uniqueId);

    FutureAction<Boolean> existsServiceAsync(String name);

    FutureAction<Boolean> existsServiceAsync(UUID uniqueId);

    boolean executeCommand(ICloudService service, String command);

}
