package net.suqatri.redicloud.node.file.packet;

import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.utils.ApplicationType;
import net.suqatri.redicloud.node.NodeLauncher;

public class FileTransferCompletedPacket extends FileTransferPacket {

    @Override
    public void receive() {
        if(CloudAPI.getInstance().getApplicationType() != ApplicationType.NODE) return;
        NodeLauncher.getInstance().getFileTransferManager().addProcessQueue(this.getTransferId());
    }
}
