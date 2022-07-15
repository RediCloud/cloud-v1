package net.suqatri.cloud.api.node.service.factory;

import net.suqatri.cloud.api.service.factory.ICloudServiceFactory;

public interface ICloudNodeServiceFactory extends ICloudServiceFactory {

    ICloudPortManager getPortManager();

}
