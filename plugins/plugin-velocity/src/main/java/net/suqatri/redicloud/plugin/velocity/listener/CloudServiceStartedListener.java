package net.suqatri.redicloud.plugin.velocity.listener;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.event.CloudListener;
import net.suqatri.redicloud.api.player.ICloudPlayer;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.api.service.ServiceEnvironment;
import net.suqatri.redicloud.api.service.event.CloudServiceStartedEvent;
import net.suqatri.redicloud.plugin.velocity.VelocityCloudAPI;
import net.suqatri.redicloud.api.velocity.utils.LegacyMessageUtils;

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
                    if (serviceHolder.getEnvironment() == ServiceEnvironment.VELOCITY) return;

                    ServerInfo serverInfo = new ServerInfo(
                            serviceHolder.getServiceName(),
                            new InetSocketAddress(serviceHolder.getHostName(), serviceHolder.getPort()));

                    VelocityCloudAPI.getInstance().getProxyServer().registerServer(serverInfo);
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
                                            if(!player.getLastConnectedProxyId().equals(VelocityCloudAPI.getInstance().getService().getUniqueId())) continue;
                                            if (!fallbackIds.contains(player.getLastConnectedServerId())) continue;
                                            Optional<Player> proxiedPlayer = VelocityCloudAPI.getInstance().getProxyServer().getPlayer(player.getUniqueId());
                                            if (!proxiedPlayer.isPresent()) continue;
                                            ICloudService fallbackServer = CloudAPI.getInstance().getServiceManager().getFallbackService(player);
                                            if(fallbackServer == null) continue;
                                            Optional<RegisteredServer> registeredServer = VelocityCloudAPI.getInstance().getProxyServer()
                                                    .getServer(fallbackServer.getServiceName());
                                            if(!registeredServer.isPresent()) continue;
                                            proxiedPlayer.get().createConnectionRequest(registeredServer.get()).connect();
                                        }
                                    });
                            });
                    }

                    if(event.isExternal()) {
                        for (Player player : VelocityCloudAPI.getInstance().getProxyServer().getAllPlayers()) {
                            if (!player.hasPermission("redicloud.service.notify")) continue;
                            player.sendMessage(LegacyMessageUtils.component(VelocityCloudAPI.getInstance().getChatPrefix()
                                    + "§3" + serviceHolder.getServiceName() + "§8(§fExternal§8) » §a§l■"));
                        }
                    }else {
                        CloudAPI.getInstance().getNodeManager().getNodeAsync(serviceHolder.getNodeId())
                                .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to get node!", e))
                                .onSuccess(node -> {

                                    for (Player player : VelocityCloudAPI.getInstance().getProxyServer().getAllPlayers()) {
                                        if (!player.hasPermission("redicloud.service.notify")) continue;
                                        player.sendMessage(LegacyMessageUtils.component(VelocityCloudAPI.getInstance().getChatPrefix()
                                                + "§3" + serviceHolder.getServiceName() + "§8(§f"
                                                + node.getName() + "§8) » §a§l■"));
                                    }

                                });
                    }
                });
    }

}
