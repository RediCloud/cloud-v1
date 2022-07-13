package net.suqatri.cloud.node.service;

import net.suqatri.cloud.api.impl.service.CloudServiceManager;
import net.suqatri.cloud.commons.function.future.FutureAction;

import java.util.UUID;

public class NodeCloudServiceManager extends CloudServiceManager {

    //TODO stop process
    @Override
    public FutureAction<Boolean> stopServiceAsync(UUID uniqueId) {
        return super.stopServiceAsync(uniqueId);
    }

    //TODO stop process
    @Override
    public boolean stopService(UUID uniqueId) {
        return super.stopService(uniqueId);
    }
}
