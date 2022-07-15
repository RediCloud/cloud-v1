package net.suqatri.cloud.api.impl.service.factory;

import lombok.Data;
import net.suqatri.cloud.api.service.ICloudService;
import net.suqatri.cloud.api.service.ICloudServiceManager;
import net.suqatri.cloud.api.service.configuration.IServiceStartConfiguration;
import net.suqatri.cloud.api.service.factory.ICloudServiceFactory;
import net.suqatri.cloud.commons.function.future.FutureAction;

@Data
public class CloudServiceFactory implements ICloudServiceFactory {

    private final ICloudServiceManager cloudServiceManager;

    @Override
    public ICloudService createService(IServiceStartConfiguration configuration) {
        return null;
    }

    @Override
    public FutureAction<ICloudService> createServiceAsync(IServiceStartConfiguration configuration) {
        return null;
    }
}
