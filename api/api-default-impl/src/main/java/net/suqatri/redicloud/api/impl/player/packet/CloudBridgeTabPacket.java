package net.suqatri.redicloud.api.impl.player.packet;

import lombok.Data;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.packet.CloudPacket;

import java.util.UUID;

@Data
public class CloudBridgeTabPacket extends CloudPacket {

    private UUID uniqueId;
    private String header;
    private String footer;

    @Override
    public void receive() {
        CloudAPI.getInstance().getPlayerManager().getPlayerAsync(this.uniqueId)
            .onFailure(t -> CloudAPI.getInstance().getConsole().error("Failed to send title to player " + this.uniqueId, t))
            .onSuccess(playerHolder -> {
                playerHolder.getBridge().sendTab(this.header, this.footer);
            });
    }
}
