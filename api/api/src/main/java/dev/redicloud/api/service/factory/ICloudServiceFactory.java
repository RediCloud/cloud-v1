package dev.redicloud.api.service.factory;

import dev.redicloud.api.service.ICloudService;
import dev.redicloud.api.service.configuration.IServiceStartConfiguration;
import dev.redicloud.commons.function.future.FutureAction;

import java.util.UUID;

public interface ICloudServiceFactory {

    FutureAction<ICloudService> queueService(IServiceStartConfiguration configuration);

    FutureAction<Boolean> destroyServiceAsync(UUID uniqueId, boolean force);

}
