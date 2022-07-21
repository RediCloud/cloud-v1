package net.suqatri.redicloud.node.file.packet;

import lombok.Getter;
import lombok.Setter;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.utils.ApplicationType;
import net.suqatri.redicloud.node.NodeLauncher;
import net.suqatri.redicloud.node.file.process.FileTransferReceiveProcess;

@Setter @Getter
public class FileTransferStartPacket extends FileTransferPacket {

    private String originalFilePath;
    private String destinationFilePath;
    private int indexes;
    private String targetFilePathToDelete;

    @Override
    public void receive() {
        if(CloudAPI.getInstance().getApplicationType() != ApplicationType.NODE) return;
        FileTransferReceiveProcess process = new FileTransferReceiveProcess(this.getTransferId());
        process.setDestinationFilePath(this.destinationFilePath);
        process.setOriginalFilePath(this.originalFilePath);
        process.setIndexes(this.indexes);
        process.setTargetFilePathToDelete(this.targetFilePathToDelete);
        process.setSenderNetworkComponentInfo(this.getPacketData().getSender());
        NodeLauncher.getInstance().getFileTransferManager().getWaitingReceiveProcesses().put(this.getTransferId(), process);
    }
}
