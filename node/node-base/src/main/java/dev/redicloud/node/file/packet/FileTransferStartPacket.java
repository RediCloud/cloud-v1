package dev.redicloud.node.file.packet;

import lombok.Getter;
import lombok.Setter;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.utils.ApplicationType;
import dev.redicloud.node.NodeLauncher;
import dev.redicloud.node.file.process.FileTransferReceiveProcess;

@Setter
@Getter
public class FileTransferStartPacket extends FileTransferPacket {

    private String originalFilePath;
    private String destinationFilePath;
    private int indexes;
    private String targetFilePathToDelete;

    @Override
    public void receive() {
        if (CloudAPI.getInstance().getApplicationType() != ApplicationType.NODE) return;
        FileTransferReceiveProcess process = new FileTransferReceiveProcess(this.getTransferId());
        process.setDestinationFilePath(this.destinationFilePath);
        process.setOriginalFilePath(this.originalFilePath);
        process.setIndexes(this.indexes);
        process.setTargetFilePathToDelete(this.targetFilePathToDelete);
        process.setSenderNetworkComponentInfo(this.getPacketData().getSender());
        NodeLauncher.getInstance().getFileTransferManager().getWaitingReceiveProcesses().put(this.getTransferId(), process);
    }
}
