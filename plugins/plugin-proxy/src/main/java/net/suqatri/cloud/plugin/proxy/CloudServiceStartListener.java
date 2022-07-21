package net.suqatri.cloud.plugin.proxy;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.event.CloudListener;
import net.suqatri.cloud.api.service.ServiceEnvironment;
import net.suqatri.cloud.api.service.event.CloudServiceStartedEvent;

import java.net.InetSocketAddress;

public class CloudServiceStartListener {

    @CloudListener
    public void onCloudServiceStart(CloudServiceStartedEvent event) {
        event.getServiceAsync()
                .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to register service!", e))
                .onSuccess(serviceHolder -> {
                    if(serviceHolder.get().getEnvironment() != ServiceEnvironment.MINECRAFT) return;
                    ServerInfo serverInfo = ProxyServer.getInstance().constructServerInfo(
                                    serviceHolder.get().getServiceName(),
                                    InetSocketAddress.createUnresolved(serviceHolder.get().getHostName(), serviceHolder.get().getPort()),
                                    serviceHolder.get().getMotd(),
                                    false);

                    ProxyServer.getInstance().getServers().put(serverInfo.getName(), serverInfo);
                    CloudAPI.getInstance().getConsole().debug("Registered service: " + serviceHolder.get().getServiceName());
                });
    }

}
