package net.suqatri.cloud.node.file.packet;

import net.suqatri.cloud.node.NodeLauncher;

public class FileTransferCompletedPacket extends FileTransferPacket {

    @Override
    public void receive() {
        NodeLauncher.getInstance().getFileTransferManager().addProcessQueue(this.getTransferId());
    }
}
