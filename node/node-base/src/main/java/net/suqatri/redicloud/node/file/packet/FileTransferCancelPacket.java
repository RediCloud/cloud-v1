package net.suqatri.redicloud.node.file.packet;

import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.utils.ApplicationType;
import net.suqatri.redicloud.node.NodeLauncher;

public class FileTransferCancelPacket extends FileTransferPacket {

    @Override
    public void receive() {
        if (CloudAPI.getInstance().getApplicationType() != ApplicationType.NODE) return;
        NodeLauncher.getInstance().getFileTransferManager().cancelTransfer(this.getTransferId(), true);
    }
}
