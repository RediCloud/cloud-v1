package net.suqatri.cloud.api.impl.node;

import lombok.Getter;
import lombok.Setter;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.impl.redis.bucket.RBucketObject;
import net.suqatri.cloud.api.node.ICloudNode;
import net.suqatri.cloud.api.service.ICloudService;
import net.suqatri.cloud.commons.function.future.FutureAction;
import net.suqatri.cloud.commons.function.future.FutureActionCollection;

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

    @Override
    public FutureAction<Collection<ICloudService>> getStartedServices() {
        FutureActionCollection<String, ICloudService> futureActionCollection = new FutureActionCollection<>();
        for (FutureAction<ICloudService> iCloudServiceFutureAction : this.startedServiceUniqueIds.parallelStream().map(UUID::toString).map(name -> CloudAPI.getInstance().getServiceManager().getServiceAsync(name)).collect(Collectors.toList())) {
            futureActionCollection.addToProcess(iCloudServiceFutureAction);
        }
        return futureActionCollection.process().map(map -> map.values());
    }

    @Override
    public int getStartedServicesCount() {
        return this.startedServiceUniqueIds.size();
    }

}
