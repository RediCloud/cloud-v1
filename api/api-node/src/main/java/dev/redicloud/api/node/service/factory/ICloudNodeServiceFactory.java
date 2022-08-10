package dev.redicloud.api.node.service.factory;

import dev.redicloud.api.service.factory.ICloudServiceFactory;

public interface ICloudNodeServiceFactory extends ICloudServiceFactory {

    ICloudPortManager getPortManager();

}
