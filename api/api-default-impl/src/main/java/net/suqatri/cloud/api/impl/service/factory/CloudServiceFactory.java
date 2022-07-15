package net.suqatri.cloud.api.impl.service.factory;

import lombok.Data;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.service.ICloudService;
import net.suqatri.cloud.api.service.ICloudServiceManager;
import net.suqatri.cloud.api.service.configuration.IServiceStartConfiguration;
import net.suqatri.cloud.api.service.factory.ICloudServiceFactory;
import net.suqatri.cloud.commons.function.future.FutureAction;

import java.io.IOException;
import java.util.UUID;

@Data
public class CloudServiceFactory implements ICloudServiceFactory {

    private final ICloudServiceManager cloudServiceManager;

    //TODO packets to node
    @Override
    public IRBucketHolder<ICloudService> createService(IServiceStartConfiguration configuration) throws Exception {
        return null;
    }

    //TODO packets to node
    @Override
    public FutureAction<IRBucketHolder<ICloudService>> createServiceAsync(IServiceStartConfiguration configuration) {
        return null;
    }

    //TODO packets to node
    @Override
    public boolean destroyService(UUID uniqueId, boolean force) throws IOException {
        return false;
    }

    //TODO packets to node
    @Override
    public FutureAction<Boolean> destroyServiceAsync(UUID uniqueId, boolean force) {
        return null;
    }
}
