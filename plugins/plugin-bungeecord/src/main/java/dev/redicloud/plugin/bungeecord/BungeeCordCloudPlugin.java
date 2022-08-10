package dev.redicloud.plugin.bungeecord;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import dev.redicloud.api.service.ServiceState;

public class BungeeCordCloudPlugin extends Plugin {

    private BungeeCordCloudAPI cloudAPI;

    @Override
    public void onLoad() {
        cloudAPI = new BungeeCordCloudAPI(this);
    }

    @Override
    public void onEnable() {
        if (cloudAPI == null) throw new IllegalStateException("CloudAPI is not initialized yet!");
        cloudAPI.registerStartedService();
        cloudAPI.getService().setServiceState(ServiceState.RUNNING_UNDEFINED);
        cloudAPI.getService().setOnlineCount(ProxyServer.getInstance().getOnlineCount());
        cloudAPI.getService().update();
    }

    @Override
    public void onDisable() {
        cloudAPI.shutdown(false);
    }
}
