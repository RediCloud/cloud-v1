package net.suqatri.redicloud.plugin.proxy;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.suqatri.redicloud.api.service.ServiceState;

public class ProxyCloudPlugin extends Plugin {

    private ProxyCloudAPI cloudAPI;

    @Override
    public void onLoad() {
        cloudAPI = new ProxyCloudAPI(this);
    }

    @Override
    public void onEnable() {
        if(cloudAPI == null) throw new IllegalStateException("CloudAPI is not initialized yet!");
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
