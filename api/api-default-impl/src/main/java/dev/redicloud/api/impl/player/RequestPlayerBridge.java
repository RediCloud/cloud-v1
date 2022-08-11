package dev.redicloud.api.impl.player;

import dev.redicloud.api.impl.player.packet.*;
import dev.redicloud.api.player.IPlayerBridge;
import lombok.AllArgsConstructor;
import lombok.Getter;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.network.NetworkComponentType;
import dev.redicloud.api.player.ICloudPlayer;
import dev.redicloud.api.service.ICloudService;
import dev.redicloud.api.service.ServiceState;

import java.util.UUID;

@AllArgsConstructor
public class RequestPlayerBridge implements IPlayerBridge {

    @Getter
    private final ICloudPlayer player;

    @Override
    public void sendMessage(String message) {
        if(!this.player.isConnected()) return;
        CloudBridgeMessagePacket packet = new CloudBridgeMessagePacket();
        packet.setMessage(message);
        packet.setUniqueId(player.getUniqueId());
        packet.getPacketData().addReceiver(CloudAPI.getInstance().getNetworkComponentManager()
                .getComponentInfo(NetworkComponentType.SERVICE, player.getLastConnectedProxyId()));
        packet.publishAsync();
    }

    @Override
    public void sendTitle(String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        if(!this.player.isConnected()) return;
        CloudBridgeTitlePacket packet = new CloudBridgeTitlePacket();
        packet.setTitle(title);
        packet.setSubtitle(subTitle);
        packet.setFadeIn(fadeIn);
        packet.setStay(stay);
        packet.setFadeOut(fadeOut);
        packet.setUniqueId(player.getUniqueId());
        packet.getPacketData().addReceiver(CloudAPI.getInstance().getNetworkComponentManager()
                .getComponentInfo(NetworkComponentType.SERVICE, player.getLastConnectedServerId()));
        packet.publishAsync();
    }

    @Override
    public void sendActionbar(String message) {
        if(!this.player.isConnected()) return;
        if(!this.player.getLastConnectedServerId()
                .equals(CloudAPI.getInstance().getNetworkComponentInfo().getIdentifier())) return;
        CloudBridgeActionbarPacket packet = new CloudBridgeActionbarPacket();
        packet.setBar(message);
        packet.setUniqueId(player.getUniqueId());
        packet.getPacketData().addReceiver(CloudAPI.getInstance().getNetworkComponentManager()
                .getComponentInfo(NetworkComponentType.SERVICE, player.getLastConnectedServerId()));
        packet.publishAsync();
    }

    @Override
    public void sendTab(String header, String footer) {
        if(!this.player.isConnected()) return;
        CloudBridgeTabPacket packet = new CloudBridgeTabPacket();
        packet.setHeader(header);
        packet.setFooter(footer);
        packet.setUniqueId(player.getUniqueId());
        packet.getPacketData().addReceiver(CloudAPI.getInstance().getNetworkComponentManager()
                .getComponentInfo(NetworkComponentType.SERVICE, player.getLastConnectedProxyId()));
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
    public void connect(ICloudService cloudService) {
        if(!this.player.isConnected()) return;
        if(cloudService.getServiceState() != ServiceState.RUNNING_DEFINED
            || cloudService.getServiceState() != ServiceState.RUNNING_UNDEFINED) return;
        CloudBridgeConnectServicePacket packet = new CloudBridgeConnectServicePacket();
        packet.setServiceId(cloudService.getUniqueId());
        packet.setUniqueId(this.player.getUniqueId());
        packet.getPacketData().addReceiver(CloudAPI.getInstance().getNetworkComponentManager()
                .getComponentInfo(NetworkComponentType.SERVICE, this.player.getLastConnectedProxyId()));
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
        if(!this.player.isConnected()) return;
        CloudBridgeDisconnectPacket packet = new CloudBridgeDisconnectPacket();
        packet.setReason(reason);
        packet.setUniqueId(this.player.getUniqueId());
        packet.getPacketData().addReceiver(CloudAPI.getInstance().getNetworkComponentManager()
                .getComponentInfo(NetworkComponentType.SERVICE, player.getLastConnectedProxyId()));
        packet.publishAsync();
    }

    @Override
    public boolean hasPermission(String permission) {
        return false;
    }

}
