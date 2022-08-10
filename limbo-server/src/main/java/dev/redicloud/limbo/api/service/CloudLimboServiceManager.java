package dev.redicloud.limbo.api.service;

import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.impl.service.CloudServiceManager;
import dev.redicloud.api.service.ICloudService;

public class CloudLimboServiceManager extends CloudServiceManager {

    @Override
    public boolean executeCommand(ICloudService serviceHolder, String command) {
        if(super.executeCommand(serviceHolder, command)) return true;
        CloudAPI.getInstance().getConsole().trace("Dispatching remote command: " + command);
        //TODO: dispatch remote command
        return true;
    }
}
