package net.suqatri.cloud.node.poll.timeout;

import lombok.Data;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.impl.poll.timeout.ITimeOutPoll;
import net.suqatri.cloud.api.impl.poll.timeout.TimeOutResult;
import net.suqatri.cloud.api.impl.redis.bucket.RBucketObject;
import net.suqatri.cloud.api.network.NetworkComponentType;
import net.suqatri.cloud.commons.function.future.FutureAction;
import net.suqatri.cloud.node.NodeLauncher;
import net.suqatri.cloud.node.node.packet.NodePingPacket;
import net.suqatri.cloud.node.poll.timeout.packet.TimeOuPollResultPacket;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Data
public class TimeOutPoll extends RBucketObject implements ITimeOutPoll {

    private static final long PACKET_RESPONSE_TIMEOUT = 5000;

    private UUID pollId;
    private UUID timeOutTargetId;
    private UUID openerId;

    @Override
    public FutureAction<TimeOutResult> decide() {
        FutureAction<TimeOutResult> futureAction = new FutureAction<>();

        CloudAPI.getInstance().getNodeManager().getNodeAsync(this.timeOutTargetId)
            .onFailure(futureAction)
            .onSuccess(nodeHolder -> {
                if(!nodeHolder.get().isConnected()){
                    TimeOuPollResultPacket resultPacket = new TimeOuPollResultPacket();
                    resultPacket.setPollID(this.pollId);
                    resultPacket.setResult(TimeOutResult.CONNECTED);
                    resultPacket.getPacketData().addReceiver(resultPacket.getPacketData().getSender());
                    resultPacket.publishAsync();
                    return;
                }
                NodePingPacket packet = new NodePingPacket();
                packet.getPacketData().waitForResponse()
                        .orTimeout(PACKET_RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS)
                        .onFailure(e -> {
                            TimeOuPollResultPacket resultPacket = new TimeOuPollResultPacket();
                            resultPacket.setPollID(this.pollId);
                            resultPacket.setResult(e instanceof TimeoutException ? TimeOutResult.FAILED : TimeOutResult.ERROR);
                            resultPacket.getPacketData().addReceiver(resultPacket.getPacketData().getSender());
                            resultPacket.publishAsync();
                        })
                        .onSuccess(response -> {
                            TimeOuPollResultPacket resultPacket = new TimeOuPollResultPacket();
                            resultPacket.setPollID(this.pollId);
                            resultPacket.setResult(TimeOutResult.PASSED);
                            resultPacket.getPacketData().addReceiver(resultPacket.getPacketData().getSender());
                            resultPacket.publishAsync();
                        });
                packet.getPacketData().addReceiver(CloudAPI.getInstance().getNetworkComponentManager()
                        .getComponentInfo(NetworkComponentType.NODE, this.timeOutTargetId.toString()));
                packet.publishAsync();
            });

        return futureAction;
    }

    @Override
    public void manageResult(TimeOutResult result, UUID nodeId) {

    }

    @Override
    public boolean isOpenerId(UUID uniqueId) {
        return NodeLauncher.getInstance().getNode().getUniqueId().equals(uniqueId);
    }

}
