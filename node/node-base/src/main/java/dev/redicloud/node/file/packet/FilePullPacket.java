package dev.redicloud.node.file.packet;

import lombok.Getter;
import lombok.Setter;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.impl.packet.CloudPacket;
import dev.redicloud.api.utils.ApplicationType;
import dev.redicloud.node.NodeLauncher;

import java.io.File;
import java.util.UUID;

@Setter
@Getter
public class FilePullPacket extends CloudPacket {

    private String originalFilePath;
    private String destinationFilePath;
    private String targetFilePathToDelete;

    @Override
    public void receive() {
        if (CloudAPI.getInstance().getApplicationType() != ApplicationType.NODE) return;
        CloudAPI.getInstance().getNodeManager().getNodeAsync(UUID.fromString(this.getPacketData().getSender().getIdentifier()))
                .onSuccess(node -> NodeLauncher.getInstance().getFileTransferManager()
                        .transferFolderToNode(new File(this.originalFilePath), new File(this.destinationFilePath), this.targetFilePathToDelete, node));
    }
}
