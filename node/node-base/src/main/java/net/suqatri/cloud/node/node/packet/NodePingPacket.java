package net.suqatri.cloud.node.node.packet;

import lombok.Getter;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.impl.packet.CloudPacket;
import net.suqatri.cloud.api.impl.packet.response.CloudPacketResponse;

@Getter
public class NodePingPacket extends CloudPacketResponse {

    private final long time = System.currentTimeMillis();

}
