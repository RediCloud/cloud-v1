package dev.redicloud.api.impl.player.packet;

import dev.redicloud.api.impl.packet.CloudPacket;
import lombok.Data;
import dev.redicloud.api.CloudAPI;

import java.util.UUID;

@Data
public class CloudBridgeDisconnectPacket extends CloudPacket {

    private UUID uniqueId;
    private String reason;

    @Override
    public void receive() {
        CloudAPI.getInstance().getPlayerManager().getPlayerAsync(this.uniqueId)
            .onFailure(t -> CloudAPI.getInstance().getConsole().error("Failed to send message to player " + this.uniqueId, t))
            .onSuccess(player -> {
                player.getBridge().disconnect(this.reason);
            });
    }
}
