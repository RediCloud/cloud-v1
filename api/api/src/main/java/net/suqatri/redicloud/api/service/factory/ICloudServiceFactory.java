package net.suqatri.redicloud.api.service.factory;

import net.suqatri.redicloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.api.service.configuration.IServiceStartConfiguration;
import net.suqatri.redicloud.commons.function.future.FutureAction;

import java.util.UUID;

public interface ICloudServiceFactory {

    FutureAction<IRBucketHolder<ICloudService>> queueService(IServiceStartConfiguration configuration);

    FutureAction<Boolean> destroyServiceAsync(UUID uniqueId, boolean force);

}
