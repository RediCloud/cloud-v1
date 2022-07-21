package net.suqatri.cloud.node.file.packet;

import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.utils.ApplicationType;
import net.suqatri.cloud.node.NodeLauncher;

public class FileTransferCompletedPacket extends FileTransferPacket {

    @Override
    public void receive() {
        if(CloudAPI.getInstance().getApplicationType() != ApplicationType.NODE) return;
        NodeLauncher.getInstance().getFileTransferManager().addProcessQueue(this.getTransferId());
    }
}
