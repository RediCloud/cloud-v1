package net.suqatri.redicloud.node.node.packet;

import lombok.Getter;
import net.suqatri.redicloud.api.impl.packet.response.CloudPacketResponse;

@Getter
public class NodePingPacket extends CloudPacketResponse {

    private final long time = System.currentTimeMillis();

}
