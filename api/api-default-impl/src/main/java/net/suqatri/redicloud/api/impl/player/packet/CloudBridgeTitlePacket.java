package net.suqatri.redicloud.api.impl.player.packet;

import lombok.Data;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.packet.CloudPacket;

import java.util.UUID;

@Data
public class CloudBridgeTitlePacket extends CloudPacket {

    private UUID uniqueId;
    private String title;
    private String subtitle;
    private int fadeIn;
    private int stay;
    private int fadeOut;

    @Override
    public void receive() {
        CloudAPI.getInstance().getPlayerManager().getPlayerAsync(this.uniqueId)
            .onFailure(t -> CloudAPI.getInstance().getConsole().error("Failed to send message to player " + this.uniqueId, t))
            .onSuccess(playerHolder -> {
                playerHolder.getBridge().sendTitle(this.title, this.subtitle, this.fadeIn, this.stay, this.fadeOut);
            });
    }
}
