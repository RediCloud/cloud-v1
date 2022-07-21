package net.suqatri.redicloud.node.file.packet;

import lombok.Getter;
import lombok.Setter;
import net.suqatri.redicloud.api.impl.packet.CloudPacket;

import java.util.UUID;

@Getter @Setter
public abstract class FileTransferPacket extends CloudPacket {

    private UUID transferId;

}
