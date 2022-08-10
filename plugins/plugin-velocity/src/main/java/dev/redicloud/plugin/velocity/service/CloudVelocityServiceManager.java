package dev.redicloud.plugin.velocity.service;

import com.velocitypowered.api.proxy.ProxyServer;
import lombok.AllArgsConstructor;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.impl.service.CloudServiceManager;
import dev.redicloud.api.service.ICloudService;

@AllArgsConstructor
public class CloudVelocityServiceManager extends CloudServiceManager {

    private final ProxyServer proxyServer;

    @Override
    public boolean executeCommand(ICloudService serviceHolder, String command) {
        if(super.executeCommand(serviceHolder, command)) return true;
        CloudAPI.getInstance().getConsole().trace("Dispatching remote command: " + command);
        this.proxyServer.getCommandManager().executeAsync(
                        this.proxyServer.getConsoleCommandSource(),
                        command);
        return true;
    }
}
