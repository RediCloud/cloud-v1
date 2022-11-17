package dev.redicloud.plugin.bungeecord;

import dev.redicloud.api.utils.Files;
import dev.redicloud.dependency.DependencyLoader;
import dev.redicloud.module.ModuleHandler;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import dev.redicloud.api.service.ServiceState;

import java.io.File;

public class BungeeCordCloudPlugin extends Plugin {

    private BungeeCordCloudAPI cloudAPI;

    @Override
    public void onLoad() {
        DependencyLoader dependencyLoader = new DependencyLoader(Files.LIBS_FOLDER.getFile(),
                Files.LIBS_REPO_FOLDER.getFile(),
                Files.LIBS_INFO_FOLDER.getFile(),
                Files.LIBS_BLACKLIST_FOLDER.getFile());
        this.cloudAPI = new BungeeCordCloudAPI(dependencyLoader, this);
        ModuleHandler.setParentClassLoader(this.getClass().getClassLoader());
        this.cloudAPI.getModuleHandler().loadModules();
    }

    @Override
    public void onEnable() {
        if (this.cloudAPI == null) throw new IllegalStateException("CloudAPI is not initialized yet!");
        this.cloudAPI.registerStartedService();

        this.cloudAPI.getService().setServiceState(ServiceState.RUNNING_UNDEFINED);
        this.cloudAPI.getService().setOnlineCount(ProxyServer.getInstance().getOnlineCount());
        this.cloudAPI.getService().update();

        this.cloudAPI.getModuleHandler().enableModules();
    }

    @Override
    public void onDisable() {
        if(this.cloudAPI == null) return;
        this.cloudAPI.shutdown(false);
    }
}
