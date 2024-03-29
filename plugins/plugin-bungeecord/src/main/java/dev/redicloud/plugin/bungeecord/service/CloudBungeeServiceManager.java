package dev.redicloud.plugin.bungeecord.service;

import net.md_5.bungee.api.ProxyServer;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.impl.service.CloudServiceManager;
import dev.redicloud.api.service.ICloudService;

public class CloudBungeeServiceManager extends CloudServiceManager {

    @Override
    public boolean executeCommand(ICloudService serviceHolder, String command) {
        if(super.executeCommand(serviceHolder, command)) return true;
        CloudAPI.getInstance().getConsole().trace("Dispatching remote command: " + command);
        ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), command);
        return true;
    }
}
