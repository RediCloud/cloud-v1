package net.suqatri.cloud.node.file.packet;

import lombok.Data;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.utils.ApplicationType;
import net.suqatri.cloud.node.NodeLauncher;
import net.suqatri.cloud.node.file.process.FileTransferReceiveProcess;

@Data
public class FileTransferBytesPacket extends FileTransferPacket {

    private int indexId;
    private byte[] fileData;

    @Override
    public void receive() {
        if(CloudAPI.getInstance().getApplicationType() != ApplicationType.NODE) return;
        FileTransferReceiveProcess process = NodeLauncher.getInstance().getFileTransferManager().getWaitingReceiveProcesses().get(this.getTransferId());
        if (process == null) {
            CloudAPI.getInstance().getConsole().error("File-read-transfer process not found for transferId " + this.getTransferId() + "! Received bytes can´t be processed! ByteIndex#" + this.getIndexId());
            return;
        }
        process.getLastAction().set(System.currentTimeMillis());
        process.getReceivedFileData().put(this.indexId, this.getFileData());
    }
}
