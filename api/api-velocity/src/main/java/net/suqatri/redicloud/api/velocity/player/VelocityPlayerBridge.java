package net.suqatri.redicloud.api.velocity.player;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.player.RequestPlayerBridge;
import net.suqatri.redicloud.api.player.ICloudPlayer;
import net.suqatri.redicloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.api.service.ServiceState;
import net.suqatri.redicloud.api.velocity.VelocityDefaultCloudAPI;
import net.suqatri.redicloud.api.velocity.utils.LegacyMessageUtils;

import java.util.Optional;
import java.util.UUID;

public class VelocityPlayerBridge extends RequestPlayerBridge {

    public VelocityPlayerBridge(IRBucketHolder<ICloudPlayer> playerHolder) {
        super(playerHolder);
    }

    @Override
    public void sendMessage(String message) {
        Player player = VelocityDefaultCloudAPI.getInstance().getProxyServer().getPlayer(this.getPlayerHolder().get().getUniqueId()).orElse(null);
        if(player == null || !player.isActive()) return;
        player.sendMessage(LegacyMessageUtils.component(message));
    }

    @Override
    public void connect(IRBucketHolder<ICloudService> cloudService) {
        if(!this.getPlayerHolder().get().isConnected()) return;
        if(cloudService.get().getServiceState() != ServiceState.RUNNING_DEFINED
                || cloudService.get().getServiceState() != ServiceState.RUNNING_UNDEFINED) return;
        Player player = VelocityDefaultCloudAPI.getInstance().getProxyServer().getPlayer(this.getPlayerHolder().get().getUniqueId()).orElse(null);
        if(player == null || !player.isActive()) return;
        RegisteredServer registeredServer = VelocityDefaultCloudAPI.getInstance().getProxyServer().getServer(cloudService.get().getServiceName()).orElse(null);
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
        if(!this.getPlayerHolder().get().isConnected()) return;
        Player player = VelocityDefaultCloudAPI.getInstance().getProxyServer().getPlayer(this.getPlayerHolder().get().getUniqueId()).orElse(null);
        if(player == null || !player.isActive()) return;
        player.disconnect(LegacyMessageUtils.component(reason));
    }

    @Override
    public void sendTab(String header, String footer) {
        if(!this.getPlayerHolder().get().isConnected()) return;
        Player player = VelocityDefaultCloudAPI.getInstance().getProxyServer().getPlayer(this.getPlayerHolder().get().getUniqueId()).orElse(null);
        if(player == null || !player.isActive()) return;
        player.getTabList().setHeaderAndFooter(LegacyMessageUtils.component(header), LegacyMessageUtils.component(footer));
    }

    @Override
    public boolean hasPermission(String permission) {
        Optional<Player> player = VelocityDefaultCloudAPI.getInstance().getProxyServer().getPlayer(this.getPlayerHolder().get().getUniqueId());
        if(player.isPresent()) return player.get().hasPermission(permission);
        return false;
    }
}
