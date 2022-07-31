package net.suqatri.redicloud.plugin.proxy.service;

import net.md_5.bungee.api.ProxyServer;
import net.suqatri.redicloud.api.impl.service.CloudServiceManager;
import net.suqatri.redicloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.redicloud.api.service.ICloudService;

public class CloudProxyServiceManager extends CloudServiceManager {

    @Override
    public boolean executeCommand(IRBucketHolder<ICloudService> serviceHolder, String command) {
        if(super.executeCommand(serviceHolder, command)) return true;
        ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), command);
        return true;
    }
}
