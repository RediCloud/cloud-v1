package net.suqatri.cloud.node.poll.timeout.packet;

import lombok.Data;
import net.suqatri.cloud.api.impl.packet.CloudPacket;
import net.suqatri.cloud.api.impl.poll.timeout.TimeOutResult;
import net.suqatri.cloud.node.NodeLauncher;

import java.util.UUID;

@Data
public class TimeOutPollRequestPacket extends CloudPacket {

    private UUID pollId;

    @Override
    public void receive() {
        NodeLauncher.getInstance().getTimeOutPoolManager().getPoll(this.pollId)
            .onFailure(e -> sendResponse(TimeOutResult.ERROR))
            .onSuccess(pool -> {
                pool.get().decide()
                    .onFailure(e -> sendResponse(TimeOutResult.ERROR))
                    .onSuccess(this::sendResponse);
            });
    }

    private void sendResponse(TimeOutResult result){
        TimeOuPollResultPacket packet = new TimeOuPollResultPacket();
        packet.setPollID(this.pollId);
        packet.setResult(result);
        packet.getPacketData().addReceiver(packet.getPacketData().getSender());
        packet.publishAsync();
    }
}
