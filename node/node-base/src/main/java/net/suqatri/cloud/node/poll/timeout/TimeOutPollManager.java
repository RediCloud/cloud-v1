package net.suqatri.cloud.node.poll.timeout;

import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.impl.poll.timeout.ITimeOutPoll;
import net.suqatri.cloud.api.impl.poll.timeout.ITimeOutPollManager;
import net.suqatri.cloud.api.impl.poll.timeout.TimeOutResult;
import net.suqatri.cloud.api.impl.redis.bucket.RedissonBucketManager;
import net.suqatri.cloud.api.network.NetworkComponentType;
import net.suqatri.cloud.api.node.ICloudNode;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.redis.event.RedisConnectedEvent;
import net.suqatri.cloud.commons.function.future.FutureAction;
import net.suqatri.cloud.node.NodeLauncher;
import net.suqatri.cloud.node.node.packet.NodePingPacket;
import net.suqatri.cloud.node.poll.timeout.packet.TimeOutPollRequestPacket;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TimeOutPollManager extends RedissonBucketManager<TimeOutPoll, ITimeOutPoll> implements ITimeOutPollManager {

    public TimeOutPollManager() {
        super("timeouts", ITimeOutPoll.class);
        CloudAPI.getInstance().getEventManager().register(RedisConnectedEvent.class, event -> this.checker());
    }

    private void checker(){
        CloudAPI.getInstance().getScheduler().scheduleTaskAsync(() -> {
            CloudAPI.getInstance().getNodeManager().getNodesAsync()
                .onFailure(e -> CloudAPI.getInstance().getConsole().error("Error while checking timeouts", e))
                .onSuccess(nodes -> {
                    nodes.forEach(node -> {
                        if(!node.get().isConnected()) return;
                        if(node.get().getUniqueId().equals(NodeLauncher.getInstance().getNode().getUniqueId())) return;
                        NodePingPacket packet = new NodePingPacket();
                        packet.getPacketData().addReceiver(node.get().getNetworkComponentInfo());
                        packet.getPacketData().waitForResponse()
                            .orTimeout(TimeOutPoll.PACKET_RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS)
                            .onFailure(e -> {
                                if(e.getCause() instanceof TimeoutException){
                                    TimeOutPoll poll = new TimeOutPoll();
                                    poll.setPollId(UUID.randomUUID());
                                    poll.setOpenerId(NodeLauncher.getInstance().getNode().getUniqueId());
                                    poll.setTimeOutTargetId(node.get().getUniqueId());
                                    this.createPoll(poll)
                                        .onFailure(e1 -> CloudAPI.getInstance().getConsole().error("Error while creating timeout poll", e1))
                                        .onSuccess(pollHolder -> {
                                            for (IRBucketHolder<ICloudNode> nodeHolder : nodes) {
                                                if(!nodeHolder.get().isConnected()) continue;
                                                if(nodeHolder.get().getUniqueId().equals(pollHolder.get().getTimeOutTargetId())) continue;
                                                pollHolder.get().getResults().put(nodeHolder.get().getUniqueId(),
                                                        pollHolder.get().getOpenerId().equals(nodeHolder.get().getUniqueId()) ? TimeOutResult.FAILED : TimeOutResult.UNKNOWN);
                                            }
                                            TimeOutPollRequestPacket requestPacket = new TimeOutPollRequestPacket();
                                            requestPacket.setPollId(pollHolder.get().getPollId());
                                            requestPacket.publishAllAsync(NetworkComponentType.NODE);
                                            CloudAPI.getInstance().getScheduler().runTaskLaterAsync(() -> poll.close(),
                                                    TimeOutPoll.PACKET_RESPONSE_TIMEOUT + 1500, TimeUnit.MILLISECONDS);
                                        });
                                    return;
                                }
                                CloudAPI.getInstance().getConsole().error("Error while checking timeouts", e);
                            })
                            .onSuccess(response -> {
                                if(node.get().isConnected()) return;

                            });
                        packet.publishAsync();
                    });
                });
        }, 15, 15, TimeUnit.SECONDS);
    }

    @Override
    public FutureAction<IRBucketHolder<ITimeOutPoll>> createPoll(ITimeOutPoll timeOutPool) {
        return createBucketAsync(timeOutPool.getPollId().toString(), timeOutPool);
    }

    @Override
    public FutureAction<IRBucketHolder<ITimeOutPoll>> getPoll(UUID pollId) {
        return this.getBucketHolderAsync(pollId.toString());
    }

    @Override
    public FutureAction<Boolean> closePoll(IRBucketHolder<ITimeOutPoll> poolHolder) {
        return this.deleteBucketAsync(poolHolder.getIdentifier());
    }
}
