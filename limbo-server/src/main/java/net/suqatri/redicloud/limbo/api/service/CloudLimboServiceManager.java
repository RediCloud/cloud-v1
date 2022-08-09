package net.suqatri.redicloud.limbo.api.service;

import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.service.CloudServiceManager;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.limbo.api.LimboCloudAPI;

public class CloudLimboServiceManager extends CloudServiceManager {

    @Override
    public boolean executeCommand(ICloudService serviceHolder, String command) {
        if(super.executeCommand(serviceHolder, command)) return true;
        CloudAPI.getInstance().getConsole().trace("Dispatching remote command: " + command);
        //TODO: dispatch remote command
        return true;
    }
}
