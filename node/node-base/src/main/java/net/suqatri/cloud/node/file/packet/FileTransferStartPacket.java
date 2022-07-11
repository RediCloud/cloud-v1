package net.suqatri.cloud.node.file.packet;

import lombok.Getter;
import lombok.Setter;
import net.suqatri.cloud.api.impl.packet.CloudPacket;

@Setter @Getter
public class FileTransferStartPacket extends FileTransferPacket {

    private String originalFilePath;
    private String destinationFilePath;

    @Override
    public void receive() {

    }
}
