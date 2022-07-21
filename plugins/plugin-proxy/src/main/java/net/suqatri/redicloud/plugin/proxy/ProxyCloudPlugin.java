package net.suqatri.redicloud.plugin.proxy;

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
        cloudAPI.getService().setServiceState(ServiceState.RUNNING_UNDEFINED);
        cloudAPI.getService().update();
    }

    @Override
    public void onDisable() {
        cloudAPI.shutdown(false);
    }
}
