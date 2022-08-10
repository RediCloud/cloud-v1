package dev.redicloud.api.service;

import dev.redicloud.api.redis.bucket.IRBucketObject;
import dev.redicloud.api.service.configuration.IServiceStartConfiguration;
import dev.redicloud.api.service.version.ICloudServiceVersion;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.group.ICloudGroup;
import dev.redicloud.api.network.INetworkComponentInfo;
import dev.redicloud.commons.function.future.FutureAction;

import java.util.Collection;
import java.util.UUID;

public interface ICloudService extends IRBucketObject {

    IServiceStartConfiguration getConfiguration();

    UUID getNodeId();

    default boolean isFallback() {
        return getConfiguration().isFallback();
    }

    boolean isExternal();

    boolean isMaintenance();

    void setMaintenance(boolean maintenance);

    void executeCommand(String command);

    long getLastPlayerAction();

    default void setFallback(boolean fallback) {
        getConfiguration().setFallback(fallback);
    }

    INetworkComponentInfo getNetworkComponentInfo();

    default FutureAction<ICloudServiceVersion> getServiceVersion() {
        return CloudAPI.getInstance().getServiceVersionManager().getServiceVersionAsync(getConfiguration().getServiceVersionName());
    }

    default ServiceEnvironment getEnvironment() {
        return getConfiguration().getEnvironment();
    }

    default String getServiceName() {
        return getName() + "-" + getId();
    }

    default String getName() {
        return getConfiguration().getName();
    }

    default UUID getUniqueId() {
        return getConfiguration().getUniqueId();
    }

    default int getId() {
        return getConfiguration().getId();
    }

    default String getGroupName() {
        return getConfiguration().isGroupBased() ? getConfiguration().getGroupName() : getConfiguration().getName();
    }

    default boolean isGroupBased() {
        return this.getConfiguration().isGroupBased();
    }

    default FutureAction<ICloudGroup> getGroup() {
        return CloudAPI.getInstance().getGroupManager().getGroupAsync(getGroupName());
    }

    int getOnlineCount();

    String getMotd();

    void setMotd(String motd);

    ServiceState getServiceState();

    default int getPercentToStartNewService(){
        return getConfiguration().getPercentToStartNewService();
    }
    default void setPercentToStartNewService(int percentToStartNewService){
        getConfiguration().setPercentToStartNewService(percentToStartNewService);
    }

    void setServiceState(ServiceState serviceState);

    int getMaxPlayers();

    void setMaxPlayers(int maxPlayers);

    default boolean isStatic() {
        return getConfiguration().isStatic();
    }

    long getMaxRam();

    Collection<UUID> getConsoleNodeListenerIds();

    String getHostName();

    int getPort();

}
