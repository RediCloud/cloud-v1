package dev.redicloud.api.impl.player.packet;

import lombok.Data;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.impl.packet.CloudPacket;

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
            .onSuccess(player -> {
                player.getBridge().sendTitle(this.title, this.subtitle, this.fadeIn, this.stay, this.fadeOut);
            });
    }
}
