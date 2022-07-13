package net.suqatri.cloud.api.node;

import net.suqatri.cloud.api.network.INetworkComponentInfo;
import net.suqatri.cloud.api.redis.bucket.IRBucketObject;
import net.suqatri.cloud.api.service.ICloudService;
import net.suqatri.cloud.commons.function.future.FutureAction;
import org.omg.CORBA.IRObject;

import java.io.Serializable;
import java.util.Collection;
import java.util.UUID;

public interface ICloudNode extends IRBucketObject {

    UUID getUniqueId();
    String getName();
    String getHostname();
    boolean isConnected();

    FutureAction<Collection<ICloudService>> getStartedServices();
    int getStartedServicesCount();
    int getMaxServiceCount();
    void setMaxServiceCount(int maxServiceCount);
    int getMaxParallelStartingServiceCount();
    void setMaxParallelStartingServiceCount(int maxStartingServiceCount);

    double getCpuUsage();
    int getMemoryUsage();
    int getMaxMemory();
    void setMaxMemory(int maxMemory);
    long getLastConnection();
    long getUpTime();
    void shutdown();
    INetworkComponentInfo getNetworkComponentInfo();

}
