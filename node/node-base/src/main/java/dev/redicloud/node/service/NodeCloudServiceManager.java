package dev.redicloud.node.service;

import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.impl.service.CloudServiceManager;
import dev.redicloud.api.service.ICloudService;
import dev.redicloud.api.service.configuration.IServiceStartConfiguration;
import dev.redicloud.commons.function.future.FutureAction;
import dev.redicloud.node.NodeLauncher;

import java.util.UUID;

public class NodeCloudServiceManager extends CloudServiceManager {

    @Override
    public FutureAction<Boolean> stopServiceAsync(UUID uniqueId, boolean force) {
        return NodeLauncher.getInstance().getServiceFactory().destroyServiceAsync(uniqueId, force);
    }

    @Override
    public FutureAction<ICloudService> startService(IServiceStartConfiguration configuration) {
        return NodeLauncher.getInstance().getServiceFactory().queueService(configuration);
    }

    public void checkOldService(UUID nodeIdToCheck){
        if (!getServices().isEmpty()) {
            CloudAPI.getInstance().getConsole().warn("It seems that the node was not correctly shut down last time!");
            int count = 0;
            for (ICloudService service : getServices()) {
                if(service.isExternal()) continue;
                if (!service.getNodeId().equals(nodeIdToCheck)) continue;
                count++;
                if(service.isStatic()) continue;
                CloudAPI.getInstance().getConsole().warn("Service " + service.getServiceName() + " is still registered in redis!");
                deleteBucketAsync(service);
                removeFromFetcher(service.getServiceName().toLowerCase(), service.getUniqueId().toString());
                if (getClient().getList("screen-log:" + service.getUniqueId()).isExists()) {
                    getClient().getList("screen-log:" + service.getUniqueId()).deleteAsync();
                }
            }
            CloudAPI.getInstance().getConsole().warn("Removed " + count + " services from redis!");
            CloudAPI.getInstance().getConsole().warn("Please check if there are any service processes running in the background!");
        }
        for (String s : readAllFetcherKeysAsync().getBlockOrNull()) {
            if(!existsService(s)) {
                removeFromFetcher(s);
                continue;
            }
            ICloudService serviceHolder = null;
            try {
                serviceHolder = getService(s);
            }catch (NullPointerException e){
                removeFromFetcher(s);
                CloudAPI.getInstance().getConsole().warn("Removed " + s + " service from fetcher!");
                continue;
            }
            if(serviceHolder.isExternal()) continue;
            if(!serviceHolder.getNodeId().equals(nodeIdToCheck)) continue;
            removeFromFetcher(serviceHolder.getIdentifier());
            CloudAPI.getInstance().getConsole().warn("Removed " + serviceHolder.getServiceName() + " service from fetcher!");
        }
    }

}
