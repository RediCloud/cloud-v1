package dev.redicloud.node.file.packet;

import lombok.Data;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.utils.ApplicationType;

import java.util.UUID;

@Data
public class FileTransferReadFailedPacket extends FileTransferPacket {

    private int indexesReceived;
    private int indexesSent;

    @Override
    public void receive() {
        if (CloudAPI.getInstance().getApplicationType() != ApplicationType.NODE) return;
        CloudAPI.getInstance().getNodeManager().getNodeAsync(UUID.fromString(getPacketData().getSender().getIdentifier()))
                .onSuccess(node -> {
                    CloudAPI.getInstance().getConsole().error("§cNode §f" + node.getName() + " §cfailed to read received file for transfer " + this.getTransferId() + "! (§f" + indexesReceived + "§c/§f" + indexesSent + "§c packets received)");
                });
    }

}
