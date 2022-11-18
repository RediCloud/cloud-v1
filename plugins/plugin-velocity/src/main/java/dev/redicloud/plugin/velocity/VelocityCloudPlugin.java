package dev.redicloud.plugin.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.redicloud.api.service.ServiceState;
import dev.redicloud.api.utils.Files;
import dev.redicloud.dependency.DependencyLoader;
import dev.redicloud.module.ModuleHandler;
import org.slf4j.Logger;

@Plugin(id = "redicloud-plugin", name = "RediCloud-Plugin", version = "1.0-SNAPSHOT", authors = "Suqatri")
public class VelocityCloudPlugin {

    private VelocityCloudAPI cloudAPI;
    private final ProxyServer proxyServer;
    private final Logger logger;
    private final DependencyLoader dependencyLoader;

    @Inject
    public VelocityCloudPlugin(ProxyServer proxyServer, Logger logger) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.dependencyLoader = new DependencyLoader(Files.LIBS_FOLDER.getFile(),
                Files.LIBS_REPO_FOLDER.getFile(),
                Files.LIBS_INFO_FOLDER.getFile(),
                Files.LIBS_BLACKLIST_FOLDER.getFile());
        ModuleHandler.setParentClassLoader(this.getClass().getClassLoader());
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event){
        this.cloudAPI = new VelocityCloudAPI(this.dependencyLoader, this.proxyServer, this);
        this.cloudAPI.getService().setServiceState(ServiceState.RUNNING_UNDEFINED);
        this.cloudAPI.getService().setOnlineCount(this.proxyServer.getPlayerCount());
        this.cloudAPI.getService().update();
        this.cloudAPI.getModuleHandler().enableModules();
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event){
        if(this.cloudAPI == null) return;
        this.cloudAPI.shutdown(false);
    }

}
