package net.suqatri.cloud.node.file.packet;

import lombok.Getter;
import lombok.Setter;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.impl.packet.CloudPacket;
import net.suqatri.cloud.api.utils.ApplicationType;
import net.suqatri.cloud.node.NodeLauncher;

import java.io.File;
import java.util.UUID;

@Setter @Getter
public class FilePullPacket extends CloudPacket {

    private String originalFilePath;
    private String destinationFilePath;
    private String targetFilePathToDelete;

    @Override
    public void receive() {
        if(CloudAPI.getInstance().getApplicationType() != ApplicationType.NODE) return;
        CloudAPI.getInstance().getNodeManager().getNodeAsync(UUID.fromString(this.getPacketData().getSender().getIdentifier()))
            .onSuccess(nodeHolder -> NodeLauncher.getInstance().getFileTransferManager()
                    .transferFolderToNode(new File(this.originalFilePath), new File(this.destinationFilePath), this.targetFilePathToDelete, nodeHolder));

    }
}
