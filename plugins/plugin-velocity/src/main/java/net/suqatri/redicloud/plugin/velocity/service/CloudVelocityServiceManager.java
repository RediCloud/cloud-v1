package net.suqatri.redicloud.plugin.velocity.service;

import com.velocitypowered.api.proxy.ProxyServer;
import lombok.AllArgsConstructor;
import net.suqatri.redicloud.api.impl.service.CloudServiceManager;
import net.suqatri.redicloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.plugin.velocity.VelocityCloudAPI;

@AllArgsConstructor
public class CloudVelocityServiceManager extends CloudServiceManager {

    private final ProxyServer proxyServer;

    @Override
    public boolean executeCommand(IRBucketHolder<ICloudService> serviceHolder, String command) {
        if(super.executeCommand(serviceHolder, command)) return true;
        this.proxyServer.getCommandManager().executeAsync(
                        this.proxyServer.getConsoleCommandSource(),
                        command);
        return true;
    }
}
