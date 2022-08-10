package dev.redicloud.node.poll.timeout.packet;

import lombok.Data;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.impl.packet.CloudPacket;
import dev.redicloud.api.node.poll.timeout.TimeOutResult;
import dev.redicloud.node.NodeLauncher;

import java.util.UUID;

@Data
public class TimeOutPollResultPacket extends CloudPacket {

    private UUID pollID;
    private TimeOutResult result;

    @Override
    public void receive() {
        NodeLauncher.getInstance().getTimeOutPollManager().getPoll(this.pollID)
                .onFailure(e ->
                        CloudAPI.getInstance().getConsole().error("Error while getting timeout poll " + this.pollID + "!"))
                .onSuccess(pool ->
                        pool.manageResult(this.result, UUID.fromString(this.getPacketData().getSender().getIdentifier())));
    }
}
