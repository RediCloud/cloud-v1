package net.suqatri.redicloud.plugin.bungeecord.listener;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.event.CloudListener;
import net.suqatri.redicloud.api.service.ServiceEnvironment;
import net.suqatri.redicloud.api.service.event.CloudServiceStartedEvent;
import net.suqatri.redicloud.plugin.bungeecord.BungeeCordCloudAPI;

import java.net.InetSocketAddress;

public class CloudServiceStartedListener {

    @CloudListener
    public void onCloudServiceStart(CloudServiceStartedEvent event) {
        event.getServiceAsync()
                .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to register service!", e))
                .onSuccess(serviceHolder -> {
                    if (serviceHolder.getEnvironment() == ServiceEnvironment.BUNGEECORD) return;
                    if(serviceHolder.getEnvironment() == ServiceEnvironment.VELOCITY) return;
                    ServerInfo serverInfo = ProxyServer.getInstance().constructServerInfo(
                            serviceHolder.getServiceName(),
                            new InetSocketAddress(serviceHolder.getHostName(), serviceHolder.getPort()),
                            serviceHolder.getMotd(),
                            false);

                    ProxyServer.getInstance().getServers().put(serverInfo.getName(), serverInfo);
                    CloudAPI.getInstance().getConsole().debug("Registered service: " + serviceHolder.getServiceName());

                    if(event.isExternal()) return;
                    CloudAPI.getInstance().getNodeManager().getNodeAsync(serviceHolder.getNodeId())
                            .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to get node!", e))
                            .onSuccess(node -> {

                                for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                                    if (!player.hasPermission("redicloud.service.notify")) continue;
                                    player.sendMessage(BungeeCordCloudAPI.getInstance().getChatPrefix()
                                            + "§3" + serviceHolder.getServiceName() + "§8(§f" + node.getName() + "§8) » §a§l■");
                                }
                            });
                });
    }

}
