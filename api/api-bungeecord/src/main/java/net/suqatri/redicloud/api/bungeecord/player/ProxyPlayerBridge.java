package net.suqatri.redicloud.api.bungeecord.player;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.player.RequestPlayerBridge;
import net.suqatri.redicloud.api.player.ICloudPlayer;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.api.service.ServiceState;

import java.util.UUID;

public class ProxyPlayerBridge extends RequestPlayerBridge {

    public ProxyPlayerBridge(ICloudPlayer player) {
        super(player);
    }

    @Override
    public void sendMessage(String message) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(this.getPlayer().getUniqueId());
        if(player == null || !player.isConnected()) return;
        player.sendMessage(message);
    }

    @Override
    public void connect(ICloudService cloudService) {
        if(!this.getPlayer().isConnected()) return;
        if(cloudService.getServiceState() != ServiceState.RUNNING_DEFINED
                || cloudService.getServiceState() != ServiceState.RUNNING_UNDEFINED) return;
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(this.getPlayer().getUniqueId());
        if(player == null || !player.isConnected()) return;
        ServerInfo serverInfo = ProxyServer.getInstance().getServerInfo(cloudService.getServiceName());
        if(serverInfo == null) return;
        player.connect(serverInfo);
    }

    @Override
    public void connect(UUID serviceId) {
        CloudAPI.getInstance().getServiceManager().existsServiceAsync(serviceId)
                .onFailure(t -> CloudAPI.getInstance().getConsole().error("Failed to connect player to server " + serviceId, t))
                .onSuccess(exists -> {
                    if(!exists) return;
                    CloudAPI.getInstance().getServiceManager().getServiceAsync(serviceId)
                            .onFailure(t -> CloudAPI.getInstance().getConsole().error("Failed to connect player to server " + serviceId, t))
                            .onSuccess(this::connect);
                });
    }

    @Override
    public void connect(String serverName) {
        CloudAPI.getInstance().getServiceManager().existsServiceAsync(serverName)
                .onFailure(t -> CloudAPI.getInstance().getConsole().error("Failed to connect player to server " + serverName, t))
                .onSuccess(exists -> {
                    if(!exists) return;
                    CloudAPI.getInstance().getServiceManager().getServiceAsync(serverName)
                            .onFailure(t -> CloudAPI.getInstance().getConsole().error("Failed to connect player to server " + serverName, t))
                            .onSuccess(this::connect);
                });
    }

    @Override
    public void disconnect(String reason) {
        if(!this.getPlayer().isConnected()) return;
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(this.getPlayer().getUniqueId());
        if(player == null || !player.isConnected()) return;
        player.disconnect(reason);
    }

    @Override
    public void sendTab(String header, String footer) {
        if(!this.getPlayer().isConnected()) return;
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(this.getPlayer().getUniqueId());
        if(player == null || !player.isConnected()) return;
        player.setTabHeader(TextComponent.fromLegacyText(header), TextComponent.fromLegacyText(footer));
    }

    @Override
    public boolean hasPermission(String permission) {
        ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(this.getPlayer().getUniqueId());
        if(proxiedPlayer == null || !proxiedPlayer.isConnected()) return false;
        return proxiedPlayer.hasPermission(permission);
    }
}
