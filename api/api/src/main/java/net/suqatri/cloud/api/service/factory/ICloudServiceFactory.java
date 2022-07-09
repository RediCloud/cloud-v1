package net.suqatri.cloud.api.service.factory;

import net.suqatri.cloud.api.service.ICloudService;
import net.suqatri.cloud.api.service.IServiceStartConfiguration;

public interface ICloudServiceFactory {

    ICloudService createCloudService(IServiceStartConfiguration configuration);

}
