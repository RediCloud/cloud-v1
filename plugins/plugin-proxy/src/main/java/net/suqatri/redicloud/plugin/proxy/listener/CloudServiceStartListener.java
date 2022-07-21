package net.suqatri.redicloud.plugin.proxy.listener;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.event.CloudListener;
import net.suqatri.redicloud.api.service.ServiceEnvironment;
import net.suqatri.redicloud.api.service.event.CloudServiceStartedEvent;

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
