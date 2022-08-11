package dev.redicloud.api.node;

import dev.redicloud.api.network.INetworkComponentInfo;
import dev.redicloud.api.redis.bucket.IRBucketObject;
import dev.redicloud.api.service.ICloudService;
import dev.redicloud.api.utils.Files;
import dev.redicloud.commons.function.future.FutureAction;

import java.util.Collection;
import java.util.UUID;

public interface ICloudNode extends IRBucketObject {

    UUID getUniqueId();

    String getName();

    String getHostname();

    boolean isConnected();

    long getTimeOut();

    String getVersion();

    void setTimeOut(long time);

    FutureAction<Collection<ICloudService>> getStartedServices();

    int getStartedServicesCount();

    int getMaxServiceCount();

    void setMaxServiceCount(int maxServiceCount);

    int getMaxParallelStartingServiceCount();

    void setMaxParallelStartingServiceCount(int maxStartingServiceCount);

    double getCpuUsage();

    long getMemoryUsage();

    long getFreeMemory();

    long getMaxMemory();

    void setMaxMemory(long maxMemory);

    long getLastConnection();

    long getUpTime();

    void shutdown();

    INetworkComponentInfo getNetworkComponentInfo();

    String getFilePath();

    String getFilePath(Files files);

    long getLastDisconnect();

    boolean isFileNode();

    void setFileNode(boolean fileNode);
}
