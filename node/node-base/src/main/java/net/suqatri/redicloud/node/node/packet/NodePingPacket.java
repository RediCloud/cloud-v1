package net.suqatri.redicloud.node.node.packet;

import lombok.Getter;
import net.suqatri.redicloud.api.impl.packet.CloudPacket;
import net.suqatri.redicloud.api.impl.packet.response.CloudPacketResponse;

@Getter
public class NodePingPacket extends CloudPacket {

    private long time = System.currentTimeMillis();

    @Override
    public void receive() {
        NodePingPacketResponse response = new NodePingPacketResponse();
        response.setTime(time);
        response.getPacketData().setResponseTargetData(this.getPacketData());
        response.getPacketData().addReceiver(this.getPacketData().getSender());
        response.publishAsync();
    }
}
