package net.suqatri.cloud.node.file.packet;

import net.suqatri.cloud.node.NodeLauncher;

public class FileTransferCancelPacket extends FileTransferPacket {

    @Override
    public void receive() {
        NodeLauncher.getInstance().getFileTransferManager().cancelTransfer(this.getTransferId(), true);
    }
}
