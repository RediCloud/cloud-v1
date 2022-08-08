package net.suqatri.redicloud.plugin.bungeecord.listener;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.event.CloudListener;
import net.suqatri.redicloud.api.player.ICloudPlayer;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.api.service.ServiceEnvironment;
import net.suqatri.redicloud.api.service.event.CloudServiceStartedEvent;
import net.suqatri.redicloud.plugin.bungeecord.BungeeCordCloudAPI;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

                    if(event.getService().isFallback()) {
                        CloudAPI.getInstance().getServiceManager().getServicesAsync()
                                .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to get services!", e))
                                .onSuccess(services -> {

                                    Collection<UUID> fallbackIds = services.parallelStream()
                                            .filter(s -> s.getEnvironment() == ServiceEnvironment.LIMBO
                                                    && s.getName().equals("Fallback")
                                                    && s.isFallback())
                                            .map(ICloudService::getUniqueId)
                                            .collect(Collectors.toList());

                                    if (fallbackIds.isEmpty()) return;

                                    CloudAPI.getInstance().getPlayerManager().getConnectedPlayers()
                                            .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to get connected players!", e))
                                            .thenAcceptAsync(players -> {
                                                for (ICloudPlayer player : players) {
                                                    if (!fallbackIds.contains(player.getLastConnectedServerId())) continue;
                                                    ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(player.getUniqueId());
                                                    if (proxiedPlayer == null) continue;
                                                    ICloudService fallbackServer = CloudAPI.getInstance().getServiceManager().getFallbackService(player);
                                                    if(fallbackServer == null) continue;
                                                    ServerInfo registeredServer = ProxyServer.getInstance().getServerInfo(fallbackServer.getServiceName());
                                                    if(registeredServer == null) continue;
                                                    proxiedPlayer.connect(registeredServer);
                                                }
                                            });
                                });
                    }

                    if(event.isExternal()) {
                        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                            if (!player.hasPermission("redicloud.service.notify")) continue;
                            player.sendMessage(BungeeCordCloudAPI.getInstance().getChatPrefix()
                                    + "§3" + serviceHolder.getServiceName() + "§8(§fExternal§8) » §a§l■");
                        }
                    }else {
                        CloudAPI.getInstance().getNodeManager().getNodeAsync(serviceHolder.getNodeId())
                                .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to get node!", e))
                                .onSuccess(node -> {

                                    for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                                        if (!player.hasPermission("redicloud.service.notify")) continue;
                                        player.sendMessage(BungeeCordCloudAPI.getInstance().getChatPrefix()
                                                + "§3" + serviceHolder.getServiceName() + "§8(§f" + node.getName() + "§8) » §a§l■");
                                    }
                                });
                    }
                });
    }

}
