package net.suqatri.cloud.api.service.factory;

import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.service.ICloudService;
import net.suqatri.cloud.api.service.configuration.IServiceStartConfiguration;
import net.suqatri.cloud.commons.function.future.FutureAction;

import java.io.IOException;
import java.util.UUID;

public interface ICloudServiceFactory {

    FutureAction<IRBucketHolder<ICloudService>> queueService(IServiceStartConfiguration configuration);

    FutureAction<Boolean> destroyServiceAsync(UUID uniqueId, boolean force);

}
