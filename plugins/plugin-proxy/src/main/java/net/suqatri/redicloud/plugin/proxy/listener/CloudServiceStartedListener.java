package net.suqatri.redicloud.plugin.proxy.listener;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.event.CloudListener;
import net.suqatri.redicloud.api.service.ServiceEnvironment;
import net.suqatri.redicloud.api.service.event.CloudServiceStartedEvent;
import net.suqatri.redicloud.plugin.proxy.ProxyCloudAPI;

import java.net.InetSocketAddress;

public class CloudServiceStartedListener {

    @CloudListener
    public void onCloudServiceStart(CloudServiceStartedEvent event) {
        event.getServiceAsync()
                .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to register service!", e))
                .onSuccess(serviceHolder -> {
                    if (serviceHolder.get().getEnvironment() == ServiceEnvironment.PROXY) return;
                    ServerInfo serverInfo = ProxyServer.getInstance().constructServerInfo(
                            serviceHolder.get().getServiceName(),
                            new InetSocketAddress(serviceHolder.get().getHostName(), serviceHolder.get().getPort()),
                            serviceHolder.get().getMotd(),
                            false);

                    ProxyServer.getInstance().getServers().put(serverInfo.getName(), serverInfo);
                    CloudAPI.getInstance().getConsole().debug("Registered service: " + serviceHolder.get().getServiceName());

                    if(event.isExternal()) return;
                    CloudAPI.getInstance().getNodeManager().getNodeAsync(serviceHolder.get().getNodeId())
                            .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to get node!", e))
                            .onSuccess(nodeHolder -> {

                                for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                                    if (!player.hasPermission("redicloud.service.notify")) continue;
                                    player.sendMessage(ProxyCloudAPI.getInstance().getChatPrefix()
                                            + "§3" + serviceHolder.get().getServiceName() + "§8(§f" + nodeHolder.get().getName() + "§8) » §a§l■");
                                }
                            });
                });
    }

}
