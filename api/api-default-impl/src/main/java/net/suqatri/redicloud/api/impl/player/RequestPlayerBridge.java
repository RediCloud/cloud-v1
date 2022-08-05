package net.suqatri.redicloud.api.impl.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.player.packet.*;
import net.suqatri.redicloud.api.network.NetworkComponentType;
import net.suqatri.redicloud.api.player.ICloudPlayer;
import net.suqatri.redicloud.api.player.IPlayerBridge;
import net.suqatri.redicloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.api.service.ServiceState;

import java.util.UUID;

@AllArgsConstructor
public class RequestPlayerBridge implements IPlayerBridge {

    @Getter
    private final IRBucketHolder<ICloudPlayer> playerHolder;

    @Override
    public void sendMessage(String message) {
        if(!this.playerHolder.get().isConnected()) return;
        CloudBridgeMessagePacket packet = new CloudBridgeMessagePacket();
        packet.setMessage(message);
        packet.setUniqueId(playerHolder.get().getUniqueId());
        packet.getPacketData().addReceiver(CloudAPI.getInstance().getNetworkComponentManager()
                .getComponentInfo(NetworkComponentType.SERVICE, playerHolder.get().getLastConnectedProxyId().toString()));
        packet.publishAsync();
    }

    @Override
    public void sendTitle(String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        if(!this.playerHolder.get().isConnected()) return;
        CloudBridgeTitlePacket packet = new CloudBridgeTitlePacket();
        packet.setTitle(title);
        packet.setSubtitle(subTitle);
        packet.setFadeIn(fadeIn);
        packet.setStay(stay);
        packet.setFadeOut(fadeOut);
        packet.setUniqueId(playerHolder.get().getUniqueId());
        packet.getPacketData().addReceiver(CloudAPI.getInstance().getNetworkComponentManager()
                .getComponentInfo(NetworkComponentType.SERVICE, playerHolder.get().getLastConnectedServerId().toString()));
        packet.publishAsync();
    }

    @Override
    public void sendActionbar(String message) {
        if(!this.playerHolder.get().isConnected()) return;
        if(!this.playerHolder.get().getLastConnectedServerId()
                .equals(UUID.fromString(CloudAPI.getInstance().getNetworkComponentInfo().getIdentifier()))) return;
        CloudBridgeActionbarPacket packet = new CloudBridgeActionbarPacket();
        packet.setBar(message);
        packet.setUniqueId(playerHolder.get().getUniqueId());
        packet.getPacketData().addReceiver(CloudAPI.getInstance().getNetworkComponentManager()
                .getComponentInfo(NetworkComponentType.SERVICE, playerHolder.get().getLastConnectedServerId().toString()));
        packet.publishAsync();
    }

    @Override
    public void sendTab(String header, String footer) {
        if(!this.playerHolder.get().isConnected()) return;
        CloudBridgeTabPacket packet = new CloudBridgeTabPacket();
        packet.setHeader(header);
        packet.setFooter(footer);
        packet.setUniqueId(playerHolder.get().getUniqueId());
        packet.getPacketData().addReceiver(CloudAPI.getInstance().getNetworkComponentManager()
                .getComponentInfo(NetworkComponentType.SERVICE, playerHolder.get().getLastConnectedProxyId().toString()));
        packet.publishAsync();
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
    public void connect(IRBucketHolder<ICloudService> cloudService) {
        if(!this.playerHolder.get().isConnected()) return;
        if(cloudService.get().getServiceState() != ServiceState.RUNNING_DEFINED
            || cloudService.get().getServiceState() != ServiceState.RUNNING_UNDEFINED) return;
        CloudBridgeConnectServicePacket packet = new CloudBridgeConnectServicePacket();
        packet.setServiceId(cloudService.get().getUniqueId());
        packet.setUniqueId(this.playerHolder.get().getUniqueId());
        packet.getPacketData().addReceiver(CloudAPI.getInstance().getNetworkComponentManager()
                .getComponentInfo(NetworkComponentType.SERVICE, playerHolder.get().getLastConnectedProxyId().toString()));
        packet.publishAsync();
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
    public void disconnect(String reason) {
        if(!this.playerHolder.get().isConnected()) return;
        CloudBridgeDisconnectPacket packet = new CloudBridgeDisconnectPacket();
        packet.setReason(reason);
        packet.setUniqueId(this.playerHolder.get().getUniqueId());
        packet.getPacketData().addReceiver(CloudAPI.getInstance().getNetworkComponentManager()
                .getComponentInfo(NetworkComponentType.SERVICE, playerHolder.get().getLastConnectedProxyId().toString()));
        packet.publishAsync();
    }

    @Override
    public boolean hasPermission(String permission) {
        return false;
    }

}
