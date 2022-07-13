package net.suqatri.cloud.api.impl.node;

import lombok.Getter;
import lombok.Setter;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.impl.CloudDefaultAPIImpl;
import net.suqatri.cloud.api.impl.node.packet.CloudNodeShutdownPacket;
import net.suqatri.cloud.api.impl.redis.bucket.RBucketObject;
import net.suqatri.cloud.api.network.INetworkComponentInfo;
import net.suqatri.cloud.api.network.NetworkComponentType;
import net.suqatri.cloud.api.node.ICloudNode;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.service.ICloudService;
import net.suqatri.cloud.api.utils.ApplicationType;
import net.suqatri.cloud.commons.file.Files;
import net.suqatri.cloud.commons.function.future.FutureAction;
import net.suqatri.cloud.commons.function.future.FutureActionCollection;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
public class CloudNode extends RBucketObject implements ICloudNode {

    private UUID uniqueId;
    private String name;
    private String hostname;
    private boolean connected;
    private Collection<UUID> startedServiceUniqueIds = new ArrayList<>();
    private double cpuUsage = 0;
    private int memoryUsage = 0;
    private int maxMemory;
    private int maxParallelStartingServiceCount;
    private int maxServiceCount;
    private long lastConnection = 0L;
    private long lastStart = 0L;
    private long lastDisconnect = 0L;
    private String filePath;

    public long getUpTime(){
        return System.currentTimeMillis() - this.lastStart;
    }

    @Override
    public void shutdown() {
        if(CloudAPI.getInstance().getNetworkComponentInfo().equals(this.getNetworkComponentInfo())){
            CloudAPI.getInstance().shutdown(false);
            return;
        }
        CloudNodeShutdownPacket packet = new CloudNodeShutdownPacket();
        packet.setNodeId(this.uniqueId);
        packet.getPacketData().addReceiver(getNetworkComponentInfo());
        packet.publishAsync();
    }

    @Override
    public INetworkComponentInfo getNetworkComponentInfo() {
        return CloudAPI.getInstance().getNetworkComponentManager().getComponentInfo(this);
    }

    @Override
    public String getFilePath(Files files) {
        return new File(getFilePath(), files.getPath()).getAbsolutePath();
    }

    @Override
    public FutureAction<Collection<ICloudService>> getStartedServices() {
        FutureActionCollection<UUID, IRBucketHolder<ICloudService>> futureActionCollection = new FutureActionCollection<>();
        for (UUID startedServiceUniqueId : this.startedServiceUniqueIds) {
            futureActionCollection.addToProcess(startedServiceUniqueId, CloudAPI.getInstance().getServiceManager().getServiceAsync(startedServiceUniqueId));
        }
        return futureActionCollection.process()
                .map(c -> c.values())
                .map(v -> v.stream()
                        .map(IRBucketHolder::get)
                .collect(Collectors.toList()));
    }

    @Override
    public int getStartedServicesCount() {
        return this.startedServiceUniqueIds.size();
    }

    @Override
    public void merged() {
        if(CloudAPI.getInstance().getApplicationType() != ApplicationType.NODE) return;
        CloudDefaultAPIImpl.getInstance().updateApplicationProperties(this);
    }
}
