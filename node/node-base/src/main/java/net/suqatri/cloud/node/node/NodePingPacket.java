package net.suqatri.cloud.node.node;

import lombok.Getter;
import net.suqatri.cloud.api.impl.packet.CloudPacket;

@Getter
public class NodePingPacket extends CloudPacket {

    private final long time = System.currentTimeMillis();

    @Override
    public void receive() {
        NodePingResponsePacket responsePacket = new NodePingResponsePacket();
        responsePacket.getPacketData().setResponseTargetData(getPacketData());
        responsePacket.publishAsync();
    }
}
