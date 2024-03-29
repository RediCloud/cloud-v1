package dev.redicloud.api.velocity.player;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.impl.player.RequestPlayerBridge;
import dev.redicloud.api.player.ICloudPlayer;
import dev.redicloud.api.service.ICloudService;
import dev.redicloud.api.service.ServiceState;
import dev.redicloud.api.velocity.utils.LegacyMessageUtils;
import dev.redicloud.api.velocity.VelocityDefaultCloudAPI;

import java.util.Optional;
import java.util.UUID;

public class VelocityPlayerBridge extends RequestPlayerBridge {

    public VelocityPlayerBridge(ICloudPlayer player) {
        super(player);
    }

    @Override
    public void sendMessage(String message) {
        Player player = VelocityDefaultCloudAPI.getInstance().getProxyServer().getPlayer(this.getPlayer().getUniqueId()).orElse(null);
        if(player == null || !player.isActive()) return;
        player.sendMessage(LegacyMessageUtils.component(message));
    }

    @Override
    public void connect(ICloudService cloudService) {
        if(!this.getPlayer().isConnected()) return;
        if(cloudService.getServiceState() != ServiceState.RUNNING_DEFINED
                || cloudService.getServiceState() != ServiceState.RUNNING_UNDEFINED) return;
        Player player = VelocityDefaultCloudAPI.getInstance().getProxyServer().getPlayer(this.getPlayer().getUniqueId()).orElse(null);
        if(player == null || !player.isActive()) return;
        RegisteredServer registeredServer = VelocityDefaultCloudAPI.getInstance().getProxyServer().getServer(cloudService.getServiceName()).orElse(null);
        if(registeredServer == null) return;
        player.createConnectionRequest(registeredServer).connect();
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
        Player player = VelocityDefaultCloudAPI.getInstance().getProxyServer().getPlayer(this.getPlayer().getUniqueId()).orElse(null);
        if(player == null || !player.isActive()) return;
        player.disconnect(LegacyMessageUtils.component(reason));
    }

    @Override
    public void sendTab(String header, String footer) {
        if(!this.getPlayer().isConnected()) return;
        Player player = VelocityDefaultCloudAPI.getInstance().getProxyServer().getPlayer(this.getPlayer().getUniqueId()).orElse(null);
        if(player == null || !player.isActive()) return;
        player.getTabList().setHeaderAndFooter(LegacyMessageUtils.component(header), LegacyMessageUtils.component(footer));
    }

    @Override
    public boolean hasPermission(String permission) {
        Optional<Player> player = VelocityDefaultCloudAPI.getInstance().getProxyServer().getPlayer(this.getPlayer().getUniqueId());
        if(player.isPresent()) return player.get().hasPermission(permission);
        return false;
    }
}
