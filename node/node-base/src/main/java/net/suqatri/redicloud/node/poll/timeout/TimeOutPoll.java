package net.suqatri.redicloud.node.poll.timeout;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.node.poll.timeout.ITimeOutPoll;
import net.suqatri.redicloud.api.node.poll.timeout.TimeOutResult;
import net.suqatri.redicloud.api.impl.redis.bucket.RBucketObject;
import net.suqatri.redicloud.api.network.NetworkComponentType;
import net.suqatri.redicloud.api.node.ICloudNode;
import net.suqatri.redicloud.api.packet.PacketChannel;
import net.suqatri.redicloud.api.player.ICloudPlayer;
import net.suqatri.redicloud.api.poll.timeout.event.NodeTimeOutedEvent;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.commons.function.future.FutureAction;
import net.suqatri.redicloud.commons.function.future.FutureActionCollection;
import net.suqatri.redicloud.node.NodeLauncher;
import net.suqatri.redicloud.node.node.packet.NodePingPacket;
import net.suqatri.redicloud.node.poll.timeout.packet.TimeOutPollResultPacket;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Data
public class TimeOutPoll extends RBucketObject implements ITimeOutPoll {

    @JsonIgnore
    private final HashMap<UUID, TimeOutResult> results = new HashMap<>();
    private UUID pollId;
    private UUID timeOutTargetId;
    private UUID openerId;

    @Override
    public FutureAction<TimeOutResult> decide() {
        FutureAction<TimeOutResult> futureAction = new FutureAction<>();

        CloudAPI.getInstance().getNodeManager().getNodeAsync(this.timeOutTargetId)
                .onFailure(futureAction)
                .onSuccess(node -> {
                    if (!node.isConnected()) {
                        TimeOutPollResultPacket resultPacket = new TimeOutPollResultPacket();
                        resultPacket.getPacketData().setChannel(PacketChannel.NODE);
                        resultPacket.setPollID(this.pollId);
                        resultPacket.setResult(TimeOutResult.CONNECTED);
                        resultPacket.getPacketData().addReceiver(resultPacket.getPacketData().getSender());
                        resultPacket.publishAsync();
                        return;
                    }
                    NodePingPacket packet = new NodePingPacket();
                    packet.getPacketData().setChannel(PacketChannel.NODE);
                    packet.getPacketData().waitForResponse()
                            .orTimeout(NodeLauncher.getInstance().getTimeOutPollManager().getConfiguration()
                                    .getPacketResponseTimeout(), TimeUnit.MILLISECONDS)
                            .onFailure(e -> {
                                TimeOutPollResultPacket resultPacket = new TimeOutPollResultPacket();
                                resultPacket.getPacketData().setChannel(PacketChannel.NODE);
                                resultPacket.setPollID(this.pollId);
                                resultPacket.setResult(e instanceof TimeoutException ? TimeOutResult.FAILED : TimeOutResult.ERROR);
                                resultPacket.getPacketData().addReceiver(resultPacket.getPacketData().getSender());
                                resultPacket.publishAsync();
                            })
                            .onSuccess(response -> {
                                if(response.getErrorMessage() != null){
                                    CloudAPI.getInstance().getConsole().error("Error while process ping node packet: " + response.getErrorMessage());
                                    TimeOutPollResultPacket resultPacket = new TimeOutPollResultPacket();
                                    resultPacket.getPacketData().setChannel(PacketChannel.NODE);
                                    resultPacket.setPollID(this.pollId);
                                    resultPacket.setResult(TimeOutResult.ERROR);
                                    resultPacket.getPacketData().addReceiver(resultPacket.getPacketData().getSender());
                                    resultPacket.publishAsync();
                                    return;
                                }
                                TimeOutPollResultPacket resultPacket = new TimeOutPollResultPacket();
                                resultPacket.getPacketData().setChannel(PacketChannel.NODE);
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
    public void close() {
        int passed = (int) this.results.values().stream().filter(result -> result == TimeOutResult.PASSED).count();
        int failed = (int) this.results.values().stream().filter(result -> result == TimeOutResult.FAILED).count();
        int error = (int) this.results.values().stream().filter(result -> result == TimeOutResult.ERROR).count();
        int total = (int) this.results.values().parallelStream().filter(r -> r != TimeOutResult.UNKNOWN).count();
        int connected = (int) this.results.values().parallelStream().filter(r -> r == TimeOutResult.CONNECTED).count();
        int unknown = (int) this.results.values().parallelStream().filter(r -> r == TimeOutResult.UNKNOWN).count();
        int min = this.results.size() / 2;

        CloudAPI.getInstance().getNodeManager().getNodeAsync(this.timeOutTargetId)
                .onFailure(e -> {
                    CloudAPI.getInstance().getConsole().error("Error while getting timeouted node " + this.timeOutTargetId + "!");
                })
                .onSuccess(node -> {
                    if (!node.isConnected()) {
                        NodeLauncher.getInstance().getTimeOutPollManager().closePoll(this);
                        return;
                    }
                    if (failed >= min) {
                        node.setTimeOut(System.currentTimeMillis() + NodeLauncher.getInstance().getTimeOutPollManager().getConfiguration().getNodeTimeOut());
                        node.updateAsync();

                        NodeTimeOutedEvent event = new NodeTimeOutedEvent(this.timeOutTargetId, passed, failed, error, total, connected, unknown, min);
                        CloudAPI.getInstance().getEventManager().postGlobalAsync(event);

                        NodeLauncher.getInstance().getTimeOutPollManager().closePoll(this);

                        node.getStartedServices()
                                .onFailure(e -> CloudAPI.getInstance().getConsole().error("Error while getting started services of node " + this.timeOutTargetId + "!", e))
                                .onSuccess(services -> {
                                    this.movePlayers(services)
                                            .onFailure(e -> CloudAPI.getInstance().getConsole().error("Error while moving players to node " + this.timeOutTargetId + "!", e))
                                            .onSuccess(b1 -> this.stopServices(node, services)
                                                    .onFailure(e -> CloudAPI.getInstance().getConsole().error("Error while stopping services on timeouted node " + this.timeOutTargetId + "!"))
                                                    .onSuccess(b2 -> {

                                                    }));
                                });
                    }
                });
    }

    private FutureAction<Boolean> movePlayers(Collection<ICloudService> services) {
        FutureAction<Boolean> futureAction = new FutureAction<>();

        List<ICloudService> fallBacks = services
                .parallelStream()
                .filter(h -> !h.getNodeId().equals(this.timeOutTargetId))
                .filter(h -> h.isFallback())
                .collect(Collectors.toList());

        CloudAPI.getInstance().getPlayerManager().getConnectedPlayers()
                .onFailure(futureAction)
                .onSuccess(players -> {
                    if (fallBacks.isEmpty()) {
                        for (ICloudPlayer player : players) {
                            player.getBridge().disconnect("You have been disconnected because the node of your connected service has been timeouted!");
                        }
                        futureAction.complete(false);
                        return;
                    }
                    for (ICloudPlayer player : players) {
                        ICloudService fallBack = getFallback(fallBacks);
                        if (fallBack == null) {
                            player.getBridge().disconnect("You have been disconnected because the node of your connected service has been timeouted!");
                            continue;
                        }
                        player.getBridge().connect(fallBack);
                        player.getBridge().sendMessage("You have been connected to a fallback service because the node of your connected service has been timeouted!");
                    }
                    futureAction.complete(true);
                });

        return futureAction;
    }

    private ICloudService getFallback(Collection<ICloudService> collection) {
        ICloudService fallBack = null;

        for (ICloudService serviceHolder : collection) {
            if (serviceHolder.getOnlineCount() < serviceHolder.getMaxPlayers()) continue;
            if (fallBack == null) {
                fallBack = serviceHolder;
                continue;
            }
            if (serviceHolder.getMaxPlayers() < fallBack.getOnlineCount()) {
                fallBack = serviceHolder;
            }
        }

        return fallBack;
    }

    private FutureAction<Boolean> stopServices(ICloudNode node, Collection<ICloudService> services) {
        FutureAction<Boolean> futureAction = new FutureAction<>();
        FutureActionCollection<UUID, Boolean> futureActionCollection = new FutureActionCollection<>();
        for (ICloudService serviceHolder : services) {
            CloudAPI.getInstance().getServiceManager().removeFromFetcher(serviceHolder.getServiceName());
            futureActionCollection.addToProcess(serviceHolder.getUniqueId(),
                    NodeLauncher.getInstance().getServiceManager().deleteBucketAsync(serviceHolder.getIdentifier()));
        }
        futureActionCollection.process()
            .onFailure(futureAction)
            .onSuccess(s -> futureAction.complete(true));
        return futureAction;
    }

    @Override
    public void manageResult(TimeOutResult result, UUID nodeId) {
        this.results.put(nodeId, result);
        if (this.results.values().parallelStream().anyMatch(r -> r == TimeOutResult.UNKNOWN)) return;
        close();
    }

    @Override
    public boolean isOpenerId() {
        return NodeLauncher.getInstance().getNode().getUniqueId().equals(this.openerId);
    }

    @Override
    public String getIdentifier() {
        return this.pollId.toString();
    }
}
