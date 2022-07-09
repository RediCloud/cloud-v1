package net.suqatri.cloud.api.service;

public interface ICloudProxy extends ICloudService{

    @Override
    default ServiceEnvironment getEnvironment() {
        return ServiceEnvironment.PROXY;
    }

}
