package net.suqatri.cloud.api.service;

public interface ICloudServer extends ICloudService{

    @Override
    default ServiceEnvironment getEnvironment() {
        return ServiceEnvironment.MINECRAFT;
    }
}
