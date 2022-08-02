package net.suqatri.redicloud.plugin.proxy;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;

@Plugin(id = "redicloud-proxy", name = "RediCloud Proxy", version = "1.0.0")
public class VelocityCloudPlugin {

    private ProxyCloudAPI cloudAPI;

    @Inject
    public VelocityCloudPlugin(ProxyServer proxyServer) {

    }

}
