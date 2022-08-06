package net.suqatri.redicloud.plugin.bungeecord.service;

import net.md_5.bungee.api.ProxyServer;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.service.CloudServiceManager;
import net.suqatri.redicloud.api.service.ICloudService;

public class CloudProxyServiceManager extends CloudServiceManager {

    @Override
    public boolean executeCommand(ICloudService serviceHolder, String command) {
        if(super.executeCommand(serviceHolder, command)) return true;
        CloudAPI.getInstance().getConsole().trace("Dispatching remote command: " + command);
        ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), command);
        return true;
    }
}
