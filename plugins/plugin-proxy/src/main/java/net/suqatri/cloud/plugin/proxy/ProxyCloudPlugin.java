package net.suqatri.cloud.plugin.proxy;

import net.md_5.bungee.api.plugin.Plugin;
import net.suqatri.cloud.api.service.ServiceState;

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
