package net.suqatri.cloud.plugin.proxy;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.event.CloudListener;
import net.suqatri.cloud.api.service.event.CloudServiceStoppedEvent;

public class CloudServiceStopListener {

    @CloudListener
    public void onCloudServiceStart(CloudServiceStoppedEvent event) {
        ServerInfo serverInfo = ProxyServer.getInstance().getServers().get(event.getServiceName());
        if(serverInfo != null) {
            ProxyServer.getInstance().getServers().remove(serverInfo.getName());
            CloudAPI.getInstance().getConsole().debug("Unregistered service: " + event.getServiceName());
        }
    }

}
