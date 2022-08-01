package net.suqatri.redicloud.api.service;

import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.group.ICloudGroup;
import net.suqatri.redicloud.api.network.INetworkComponentInfo;
import net.suqatri.redicloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.redicloud.api.redis.bucket.IRBucketObject;
import net.suqatri.redicloud.api.service.configuration.IServiceStartConfiguration;
import net.suqatri.redicloud.api.service.version.ICloudServiceVersion;
import net.suqatri.redicloud.commons.function.future.FutureAction;

import java.util.Collection;
import java.util.UUID;

public interface ICloudService extends IRBucketObject {

    IServiceStartConfiguration getConfiguration();

    UUID getNodeId();

    default boolean isFallback( {
        return getConfiguration().isFallback();
    }

    boolean isExternal();

    default void setFallback(boolean fallback) {
        getConfiguration().setFallback(fallback);
    }

    INetworkComponentInfo getNetworkComponentInfo();

    default FutureAction<IRBucketHolder<ICloudServiceVersion>> getServiceVersion() {
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

    default FutureAction<IRBucketHolder<ICloudGroup>> getGroup() {
        return CloudAPI.getInstance().getGroupManager().getGroupAsync(getGroupName());
    }

    int getOnlineCount();

    String getMotd();

    void setMotd(String motd);

    ServiceState getServiceState();

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
