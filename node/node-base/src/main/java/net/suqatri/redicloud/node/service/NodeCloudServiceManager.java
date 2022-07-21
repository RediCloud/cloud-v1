package net.suqatri.redicloud.node.service;

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
}
