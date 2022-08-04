package net.suqatri.redicloud.node.service;

import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.service.CloudServiceManager;
import net.suqatri.redicloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.api.service.configuration.IServiceStartConfiguration;
import net.suqatri.redicloud.commons.function.future.FutureAction;
import net.suqatri.redicloud.node.NodeLauncher;

import java.util.UUID;

public class NodeCloudServiceManager extends CloudServiceManager {

    @Override
    public FutureAction<Boolean> stopServiceAsync(UUID uniqueId, boolean force) {
        return NodeLauncher.getInstance().getServiceFactory().destroyServiceAsync(uniqueId, force);
    }

    @Override
    public FutureAction<IRBucketHolder<ICloudService>> startService(IServiceStartConfiguration configuration) {
        return NodeLauncher.getInstance().getServiceFactory().queueService(configuration);
    }

    public void checkOldService(UUID nodeIdToCheck){
        if (!getServices().isEmpty()) {
            CloudAPI.getInstance().getConsole().warn("It seems that the node was not correctly shut down last time!");
            int count = 0;
            for (IRBucketHolder<ICloudService> serviceHolder : getServices()) {
                if(serviceHolder.get().isExternal()) continue;
                if (!serviceHolder.get().getNodeId().equals(nodeIdToCheck)) continue;
                count++;
                if(serviceHolder.get().isStatic()) continue;
                CloudAPI.getInstance().getConsole().warn("Service " + serviceHolder.get().getServiceName() + " is still registered in redis!");
                deleteBucketAsync(serviceHolder.getIdentifier());
                removeFromFetcher(serviceHolder.get().getServiceName(), serviceHolder.get().getUniqueId());
                if (getClient().getList("screen-log:" + serviceHolder.get().getUniqueId()).isExists()) {
                    getClient().getList("screen-log:" + serviceHolder.get().getUniqueId()).deleteAsync();
                }
            }
            CloudAPI.getInstance().getConsole().warn("Removed " + count + " services from redis!");
            CloudAPI.getInstance().getConsole().warn("Please check if there are any service processes running in the background!");
        }
        for (String s : readAllFetcherKeysAsync().getBlockOrNull()) {
            if(!existsService(s)) {
                getServiceIdFetcherMap().remove(s);
                continue;
            }
            IRBucketHolder<ICloudService> serviceHolder = getService(s);
            if(serviceHolder.get().isExternal()) continue;
            if(!serviceHolder.get().getNodeId().equals(nodeIdToCheck)) continue;
            removeFromFetcher(serviceHolder.getIdentifier());
            CloudAPI.getInstance().getConsole().warn("Removed " + serviceHolder.get().getServiceName() + " services from fetcher!");
        }
    }

}
