package net.suqatri.cloud.api.service.factory;

import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.service.ICloudService;
import net.suqatri.cloud.api.service.configuration.IServiceStartConfiguration;
import net.suqatri.cloud.commons.function.future.FutureAction;

import java.io.IOException;
import java.util.UUID;

public interface ICloudServiceFactory {

    IRBucketHolder<ICloudService> createService(IServiceStartConfiguration configuration) throws Exception;
    FutureAction<IRBucketHolder<ICloudService>> createServiceAsync(IServiceStartConfiguration configuration);

    boolean destroyService(UUID uniqueId, boolean force) throws IOException;
    FutureAction<Boolean> destroyServiceAsync(UUID uniqueId, boolean force);

}
