package net.suqatri.redicloud.api.node;

import net.suqatri.redicloud.api.network.INetworkComponentInfo;
import net.suqatri.redicloud.api.redis.bucket.IRBucketObject;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.api.utils.Files;
import net.suqatri.redicloud.commons.function.future.FutureAction;

import java.util.Collection;
import java.util.UUID;

public interface ICloudNode extends IRBucketObject {

    UUID getUniqueId();

    String getName();

    String getHostname();

    boolean isConnected();

    long getTimeOut();

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
