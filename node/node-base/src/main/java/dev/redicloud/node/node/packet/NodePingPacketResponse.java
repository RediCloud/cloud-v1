package dev.redicloud.node.node.packet;

import lombok.Getter;
import lombok.Setter;
import dev.redicloud.api.impl.packet.response.CloudPacketResponse;

@Setter @Getter
public class NodePingPacketResponse extends CloudPacketResponse {

    private long time;

    @Override
    public void receive() {
        this.time = System.currentTimeMillis() - this.time;
        super.receive();
    }
}
