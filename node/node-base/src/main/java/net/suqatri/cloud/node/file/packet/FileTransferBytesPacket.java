package net.suqatri.cloud.node.file.packet;

import lombok.Data;
import lombok.Setter;

@Data
@Setter
public class FileTransferBytesPacket extends FileTransferPacket {

    private int indexId;
    private byte[] fileData;

    @Override
    public void receive() {

    }
}
