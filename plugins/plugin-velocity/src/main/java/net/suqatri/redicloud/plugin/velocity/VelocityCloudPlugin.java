package net.suqatri.redicloud.plugin.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import net.suqatri.redicloud.api.service.ServiceState;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

@Plugin(id = "redicloud-plugin", name = "RediCloud-Plugin", version = "1.0-SNAPSHOT", authors = "Suqatri")
public class VelocityCloudPlugin {

    private VelocityCloudAPI cloudAPI;
    private ProxyServer proxyServer;
    private Logger logger;

    @Inject
    public VelocityCloudPlugin(ProxyServer proxyServer, Logger logger) {
        this.proxyServer = proxyServer;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event){
        this.cloudAPI = new VelocityCloudAPI(this.proxyServer, this);
        this.cloudAPI.getService().setServiceState(ServiceState.RUNNING_UNDEFINED);
        this.cloudAPI.getService().setOnlineCount(this.proxyServer.getPlayerCount());
        this.cloudAPI.getService().update();
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event){
        if(this.cloudAPI == null) return;
        this.cloudAPI.shutdown(false);
    }

}
