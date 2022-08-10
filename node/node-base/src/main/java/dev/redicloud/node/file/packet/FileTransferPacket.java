package dev.redicloud.node.file.packet;

import lombok.Getter;
import lombok.Setter;
import dev.redicloud.api.impl.packet.CloudPacket;

import java.util.UUID;

@Getter
@Setter
public abstract class FileTransferPacket extends CloudPacket {

    private UUID transferId;

}
