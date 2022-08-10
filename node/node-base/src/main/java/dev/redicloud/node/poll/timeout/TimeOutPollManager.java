package dev.redicloud.node.poll.timeout;

import dev.redicloud.node.poll.timeout.packet.TimeOutPollRequestPacket;
import lombok.Getter;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.node.poll.timeout.ITimeOutPoll;
import dev.redicloud.api.node.poll.timeout.ITimeOutPollManager;
import dev.redicloud.api.impl.configuration.impl.TimeOutPoolConfiguration;
import dev.redicloud.api.node.poll.timeout.TimeOutResult;
import dev.redicloud.api.impl.redis.bucket.RedissonBucketManager;
import dev.redicloud.api.network.NetworkComponentType;
import dev.redicloud.api.node.ICloudNode;
import dev.redicloud.api.packet.PacketChannel;
import dev.redicloud.api.redis.event.RedisConnectedEvent;
import dev.redicloud.api.redis.event.RedisDisconnectedEvent;
import dev.redicloud.api.scheduler.IRepeatScheduler;
import dev.redicloud.commons.function.future.FutureAction;
import dev.redicloud.node.NodeLauncher;
import dev.redicloud.node.node.packet.NodePingPacket;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TimeOutPollManager extends RedissonBucketManager<TimeOutPoll, ITimeOutPoll> implements ITimeOutPollManager {

    private IRepeatScheduler<?> task;
    @Getter
    private TimeOutPoolConfiguration configuration = new TimeOutPoolConfiguration();

    public TimeOutPollManager() {
        super("timeouts", TimeOutPoll.class);
        CloudAPI.getInstance().getEventManager().registerWithoutBlockWarning(RedisConnectedEvent.class, event -> {
            this.configuration = CloudAPI.getInstance().getConfigurationManager().existsConfiguration(this.configuration.getIdentifier())
                    ? CloudAPI.getInstance().getConfigurationManager()
                        .getConfiguration(this.configuration.getIdentifier(), TimeOutPoolConfiguration.class)
                    : CloudAPI.getInstance().getConfigurationManager().createConfiguration(this.configuration);
            if(this.configuration.isEnabled()) this.checker();
        });
    }

    private void checker() {
        CloudAPI.getInstance().getEventManager().register(RedisDisconnectedEvent.class, event -> this.task.cancel());
        this.task = CloudAPI.getInstance().getScheduler().scheduleTaskAsync(() -> {
            if (NodeLauncher.getInstance().isRestarting()
                    || NodeLauncher.getInstance().isShutdownInitialized()
                    || NodeLauncher.getInstance().isInstanceTimeOuted()) {
                this.task.cancel();
                return;
            }
            CloudAPI.getInstance().getNodeManager().getNodesAsync()
                    .onFailure(e -> CloudAPI.getInstance().getConsole().error("Error while checking timeouts", e))
                    .onSuccess(nodes -> {
                        nodes.forEach(node -> {
                            if (!node.isConnected()) return;
                            if (node.getUniqueId().equals(NodeLauncher.getInstance().getNode().getUniqueId()))
                                return;
                            NodePingPacket packet = new NodePingPacket();
                            packet.getPacketData().setChannel(PacketChannel.NODE);
                            packet.getPacketData().addReceiver(node.getNetworkComponentInfo());
                            packet.getPacketData().waitForResponse()
                                    .orTimeout(this.configuration.getPacketResponseTimeout(), TimeUnit.MILLISECONDS)
                                    .onFailure(e -> {
                                        if (e.getCause() instanceof TimeoutException) {
                                            TimeOutPoll poll = new TimeOutPoll();
                                            poll.setPollId(UUID.randomUUID());
                                            poll.setOpenerId(NodeLauncher.getInstance().getNode().getUniqueId());
                                            poll.setTimeOutTargetId(node.getUniqueId());
                                            this.createPoll(poll)
                                                    .onFailure(e1 -> CloudAPI.getInstance().getConsole().error("Error while creating timeout poll", e1))
                                                    .onSuccess(pollHolder -> {
                                                        for (ICloudNode clusterNodes : nodes) {
                                                            if (!clusterNodes.isConnected()) continue;
                                                            if (clusterNodes.getUniqueId().equals(pollHolder.getTimeOutTargetId()))
                                                                continue;
                                                            pollHolder.getResults().put(clusterNodes.getUniqueId(),
                                                                    pollHolder.getOpenerId().equals(clusterNodes.getUniqueId()) ? TimeOutResult.FAILED : TimeOutResult.UNKNOWN);
                                                        }
                                                        TimeOutPollRequestPacket requestPacket = new TimeOutPollRequestPacket();
                                                        requestPacket.getPacketData().setChannel(PacketChannel.NODE);
                                                        requestPacket.setPollId(pollHolder.getPollId());
                                                        requestPacket.publishAllAsync(NetworkComponentType.NODE);
                                                        CloudAPI.getInstance().getScheduler().runTaskLaterAsync(poll::close,
                                                                this.configuration.getPacketResponseTimeout() + 1500, TimeUnit.MILLISECONDS);
                                                    });
                                            return;
                                        }
                                        CloudAPI.getInstance().getConsole().error("Error while checking timeouts", e);
                                    });
                            packet.publishAsync();
                        });
                    });
        }, 15, 15, TimeUnit.SECONDS);
    }

    @Override
    public FutureAction<ITimeOutPoll> createPoll(ITimeOutPoll timeOutPool) {
        return createBucketAsync(timeOutPool.getPollId().toString(), timeOutPool);
    }

    @Override
    public FutureAction<ITimeOutPoll> getPoll(UUID pollId) {
        return this.getAsync(pollId.toString());
    }

    @Override
    public FutureAction<Boolean> closePoll(ITimeOutPoll poolHolder) {
        return this.deleteBucketAsync(poolHolder.getIdentifier());
    }
}
