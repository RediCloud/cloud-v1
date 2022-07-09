package net.suqatri.cloud.api.service;

import net.suqatri.cloud.commons.function.future.FutureAction;

import java.util.Collection;
import java.util.UUID;

public interface ICloudServiceManager {

    FutureAction<ICloudService> getServiceAsync(String name);
    FutureAction<ICloudService> getServiceAsync(UUID uniqueId);

    ICloudService getService(String name);
    ICloudService getService(UUID uniqueId);

    FutureAction<Collection<ICloudService>> getServicesAsync();
    Collection<ICloudService> getServices();

}
