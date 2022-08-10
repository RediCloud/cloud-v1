package dev.redicloud.node.file.packet;

import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.utils.ApplicationType;
import dev.redicloud.node.NodeLauncher;

public class FileTransferCompletedPacket extends FileTransferPacket {

    @Override
    public void receive() {
        if (CloudAPI.getInstance().getApplicationType() != ApplicationType.NODE) return;
        NodeLauncher.getInstance().getFileTransferManager().addProcessQueue(this.getTransferId());
    }
}
