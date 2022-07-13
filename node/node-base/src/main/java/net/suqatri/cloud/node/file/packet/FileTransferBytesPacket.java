package net.suqatri.cloud.node.file.packet;

import lombok.Data;
import lombok.Setter;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.node.NodeLauncher;
import net.suqatri.cloud.node.file.process.FileTransferReceiveProcess;

@Data
@Setter
public class FileTransferBytesPacket extends FileTransferPacket {

    private int indexId;
    private byte[] fileData;

    @Override
    public void receive() {
        FileTransferReceiveProcess process = NodeLauncher.getInstance().getFileTransferManager().getWaitingReceiveProcesses().get(this.getTransferId());
        if (process == null) {
            CloudAPI.getInstance().getConsole().error("File-read-transfer process not found for transferId " + this.getTransferId() + "! Received bytes can´t be processed! ByteIndex#" + this.getIndexId());
            return;
        }
        process.getReceivedFileData().put(this.getIndexId(), this.getFileData());
    }
}