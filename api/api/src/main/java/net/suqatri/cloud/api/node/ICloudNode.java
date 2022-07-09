package net.suqatri.cloud.api.node;

import net.suqatri.cloud.api.player.ICloudPlayer;
import net.suqatri.cloud.api.service.ICloudService;

import java.util.Collection;

public interface ICloudNode {

    String getName();
    String getHostname();
    void setHostName();

    boolean isConnected();

    Collection<ICloudService> getStartedServices();
    int getStartedServicesCount();

    double getCPUUsage();
    int getMemoryUsage();
    int getMaxMemory();

}
