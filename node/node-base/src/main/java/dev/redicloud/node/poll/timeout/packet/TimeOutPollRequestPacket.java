package dev.redicloud.node.poll.timeout.packet;

import lombok.Data;
import dev.redicloud.api.impl.packet.CloudPacket;
import dev.redicloud.api.node.poll.timeout.TimeOutResult;
import dev.redicloud.api.packet.PacketChannel;
import dev.redicloud.node.NodeLauncher;

import java.util.UUID;

@Data
public class TimeOutPollRequestPacket extends CloudPacket {

    private UUID pollId;

    @Override
    public void receive() {
        NodeLauncher.getInstance().getTimeOutPollManager().getPoll(this.pollId)
                .onFailure(e -> sendResponse(TimeOutResult.ERROR))
                .onSuccess(pool -> {
                    if (pool.isOpenerId()) return;
                    pool.decide()
                            .onFailure(e -> sendResponse(TimeOutResult.ERROR))
                            .onSuccess(this::sendResponse);
                });
    }

    private void sendResponse(TimeOutResult result) {
        TimeOutPollResultPacket packet = new TimeOutPollResultPacket();
        packet.getPacketData().setChannel(PacketChannel.NODE);
        packet.setPollID(this.pollId);
        packet.setResult(result);
        packet.getPacketData().addReceiver(packet.getPacketData().getSender());
        packet.publishAsync();
    }
}
