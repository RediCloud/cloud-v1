package net.suqatri.cloud.node.service;

import net.suqatri.cloud.api.impl.service.CloudServiceManager;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.service.ICloudService;
import net.suqatri.cloud.api.service.configuration.IServiceStartConfiguration;
import net.suqatri.cloud.commons.function.future.FutureAction;
import net.suqatri.cloud.node.NodeLauncher;

import java.util.UUID;

public class NodeCloudServiceManager extends CloudServiceManager {

    @Override
    public FutureAction<Boolean> stopServiceAsync(UUID uniqueId, boolean force) {
        return NodeLauncher.getInstance().getServiceFactory().destroyServiceAsync(uniqueId, force);
    }

    @Override
    public boolean stopService(UUID uniqueId, boolean force) throws Exception {
        return NodeLauncher.getInstance().getServiceFactory().destroyService(uniqueId, force);
    }

    @Override
    public FutureAction<IRBucketHolder<ICloudService>> startService(IServiceStartConfiguration configuration) {
        return NodeLauncher.getInstance().getServiceFactory().queueService(configuration);
    }
}
