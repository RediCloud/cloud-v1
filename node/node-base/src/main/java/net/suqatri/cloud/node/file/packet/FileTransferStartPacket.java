package net.suqatri.cloud.node.file.packet;

import lombok.Getter;
import lombok.Setter;
import net.suqatri.cloud.node.NodeLauncher;
import net.suqatri.cloud.node.file.process.FileTransferReceiveProcess;

@Setter @Getter
public class FileTransferStartPacket extends FileTransferPacket {

    private String originalFilePath;
    private String destinationFilePath;
    private int indexes;

    @Override
    public void receive() {
        FileTransferReceiveProcess process = new FileTransferReceiveProcess(this.getTransferId());
        process.setDestinationFilePath(this.destinationFilePath);
        process.setOriginalFilePath(this.originalFilePath);
        process.setIndexes(this.indexes);
        process.setSenderNetworkComponentInfo(this.getPacketData().getSender());
        NodeLauncher.getInstance().getFileTransferManager().getWaitingReceiveProcesses().put(this.getTransferId(), process);
    }
}
