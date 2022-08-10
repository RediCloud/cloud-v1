package dev.redicloud.node.file.packet;

import lombok.Data;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.node.file.process.IFileTransferReceiveProcess;
import dev.redicloud.api.utils.ApplicationType;
import dev.redicloud.node.NodeLauncher;

@Data
public class FileTransferBytesPacket extends FileTransferPacket {

    private int indexId;
    private byte[] fileData;

    @Override
    public void receive() {
        if (CloudAPI.getInstance().getApplicationType() != ApplicationType.NODE) return;
        IFileTransferReceiveProcess process = NodeLauncher.getInstance().getFileTransferManager().getWaitingReceiveProcesses().get(this.getTransferId());
        if (process == null) {
            CloudAPI.getInstance().getConsole().error("File-read-transfer process not found for transferId " + this.getTransferId() + "! Received bytes can´t be processed! ByteIndex#" + this.getIndexId());
            return;
        }
        process.getLastAction().set(System.currentTimeMillis());
        process.getReceivedFileData().put(this.indexId, this.getFileData());
    }
}
