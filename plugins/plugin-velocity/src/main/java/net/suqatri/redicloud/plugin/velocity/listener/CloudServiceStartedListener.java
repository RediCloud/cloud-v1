package net.suqatri.redicloud.plugin.velocity.listener;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerInfo;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.event.CloudListener;
import net.suqatri.redicloud.api.service.ServiceEnvironment;
import net.suqatri.redicloud.api.service.event.CloudServiceStartedEvent;
import net.suqatri.redicloud.plugin.velocity.VelocityCloudAPI;
import net.suqatri.redicloud.api.velocity.utils.LegacyMessageUtils;

import java.net.InetSocketAddress;

public class CloudServiceStartedListener {

    @CloudListener
    public void onCloudServiceStart(CloudServiceStartedEvent event) {
        event.getServiceAsync()
                .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to register service!", e))
                .onSuccess(serviceHolder -> {
                    if (serviceHolder.get().getEnvironment() == ServiceEnvironment.BUNGEECORD) return;
                    if (serviceHolder.get().getEnvironment() == ServiceEnvironment.VELOCITY) return;

                    ServerInfo serverInfo = new ServerInfo(
                            serviceHolder.get().getServiceName(),
                            new InetSocketAddress(serviceHolder.get().getHostName(), serviceHolder.get().getPort()));

                    VelocityCloudAPI.getInstance().getProxyServer().registerServer(serverInfo);
                    CloudAPI.getInstance().getConsole().debug("Registered service: " + serviceHolder.get().getServiceName());

                    if(event.isExternal()) return;
                    CloudAPI.getInstance().getNodeManager().getNodeAsync(serviceHolder.get().getNodeId())
                            .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to get node!", e))
                            .onSuccess(nodeHolder -> {

                                for (Player player : VelocityCloudAPI.getInstance().getProxyServer().getAllPlayers()) {
                                    if (!player.hasPermission("redicloud.service.notify")) continue;
                                    player.sendMessage(LegacyMessageUtils.component(VelocityCloudAPI.getInstance().getChatPrefix()
                                            + "§3" + serviceHolder.get().getServiceName() + "§8(§f"
                                            + nodeHolder.get().getName() + "§8) » §a§l■"));
                                }

                            });
                });
    }

}
