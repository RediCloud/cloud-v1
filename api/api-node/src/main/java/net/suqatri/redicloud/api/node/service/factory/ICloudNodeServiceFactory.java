package net.suqatri.redicloud.api.node.service.factory;

import net.suqatri.redicloud.api.service.factory.ICloudServiceFactory;

public interface ICloudNodeServiceFactory extends ICloudServiceFactory {

    ICloudPortManager getPortManager();

}
