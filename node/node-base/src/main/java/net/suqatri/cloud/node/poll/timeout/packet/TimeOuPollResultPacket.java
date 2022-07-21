package net.suqatri.cloud.node.poll.timeout.packet;

import lombok.Data;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.impl.packet.CloudPacket;
import net.suqatri.cloud.api.impl.poll.timeout.TimeOutResult;
import net.suqatri.cloud.node.NodeLauncher;

import java.util.UUID;

@Data
public class TimeOuPollResultPacket extends CloudPacket {

    private UUID pollID;
    private TimeOutResult result;

    @Override
    public void receive() {
        NodeLauncher.getInstance().getTimeOutPoolManager().getPoll(this.pollID)
                .onFailure(e ->
                        CloudAPI.getInstance().getConsole().error("Error while getting timeout poll " + this.pollID + "!"))
                .onSuccess(pool ->
                        pool.get().manageResult(this.result, UUID.fromString(this.getPacketData().getSender().getIdentifier())));
    }
}
