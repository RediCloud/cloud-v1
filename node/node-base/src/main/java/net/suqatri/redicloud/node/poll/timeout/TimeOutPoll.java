package net.suqatri.redicloud.node.poll.timeout;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.poll.timeout.ITimeOutPoll;
import net.suqatri.redicloud.api.impl.poll.timeout.TimeOutResult;
import net.suqatri.redicloud.api.impl.redis.bucket.RBucketObject;
import net.suqatri.redicloud.api.network.NetworkComponentType;
import net.suqatri.redicloud.api.node.ICloudNode;
import net.suqatri.redicloud.api.player.ICloudPlayer;
import net.suqatri.redicloud.api.poll.timeout.event.NodeTimeOutedEvent;
import net.suqatri.redicloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.commons.function.future.FutureAction;
import net.suqatri.redicloud.commons.function.future.FutureActionCollection;
import net.suqatri.redicloud.node.NodeLauncher;
import net.suqatri.redicloud.node.node.packet.NodePingPacket;
import net.suqatri.redicloud.node.poll.timeout.packet.TimeOutPollResultPacket;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Data
public class TimeOutPoll extends RBucketObject implements ITimeOutPoll {

    public static final long PACKET_RESPONSE_TIMEOUT = 5000;
    public static final long NODE_TIMEOUT = TimeUnit.MINUTES.toMillis(5);

    private UUID pollId;
    private UUID timeOutTargetId;
    private UUID openerId;
    @JsonIgnore
    private final HashMap<UUID, TimeOutResult> results = new HashMap<>();

    @Override
    public FutureAction<TimeOutResult> decide() {
        FutureAction<TimeOutResult> futureAction = new FutureAction<>();

        CloudAPI.getInstance().getNodeManager().getNodeAsync(this.timeOutTargetId)
            .onFailure(futureAction)
            .onSuccess(nodeHolder -> {
                if(!nodeHolder.get().isConnected()){
                    TimeOutPollResultPacket resultPacket = new TimeOutPollResultPacket();
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
                            TimeOutPollResultPacket resultPacket = new TimeOutPollResultPacket();
                            resultPacket.setPollID(this.pollId);
                            resultPacket.setResult(e instanceof TimeoutException ? TimeOutResult.FAILED : TimeOutResult.ERROR);
                            resultPacket.getPacketData().addReceiver(resultPacket.getPacketData().getSender());
                            resultPacket.publishAsync();
                        })
                        .onSuccess(response -> {
                            TimeOutPollResultPacket resultPacket = new TimeOutPollResultPacket();
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
            .onSuccess(nodeHolder -> {
               if(!nodeHolder.get().isConnected()) {
                   NodeLauncher.getInstance().getTimeOutPollManager().closePoll(this.getHolder());
                   return;
               }
               if(failed >= min){
                   nodeHolder.get().setTimeOut(System.currentTimeMillis() + NODE_TIMEOUT);
                   nodeHolder.get().updateAsync();

                   NodeTimeOutedEvent event = new NodeTimeOutedEvent(this.timeOutTargetId, passed, failed, error, total, connected, unknown, min);
                   CloudAPI.getInstance().getEventManager().postGlobalAsync(event);

                   NodeLauncher.getInstance().getTimeOutPollManager().closePoll(this.getHolder());

                   nodeHolder.get().getStartedServices()
                       .onFailure(e -> CloudAPI.getInstance().getConsole().error("Error while getting started services of node " + this.timeOutTargetId + "!", e))
                       .onSuccess(serviceHolders -> {
                           this.movePlayers(serviceHolders)
                               .onFailure(e -> CloudAPI.getInstance().getConsole().error("Error while moving players to node " + this.timeOutTargetId + "!", e))
                               .onSuccess(b1 -> this.stopServices(nodeHolder, serviceHolders)
                                   .onFailure(e -> CloudAPI.getInstance().getConsole().error("Error while stopping services on timeouted node " + this.timeOutTargetId + "!"))
                                   .onSuccess(b2 -> {

                                   }));
                       });
               }
            });
    }

    private FutureAction<Boolean> movePlayers(Collection<IRBucketHolder<ICloudService>> serviceHolders){
        FutureAction<Boolean> futureAction = new FutureAction<>();

       List<IRBucketHolder<ICloudService>> fallBacks = serviceHolders
              .parallelStream()
              .filter(h -> !h.get().getNodeId().equals(this.timeOutTargetId))
              .filter(h -> h.get().isFallback())
              .collect(Collectors.toList());

       CloudAPI.getInstance().getPlayerManager().getConnectedPlayers()
           .onFailure(futureAction)
           .onSuccess(playerHolders -> {
                if(fallBacks.isEmpty()){
                    for (IRBucketHolder<ICloudPlayer> playerHolder : playerHolders) {
                        playerHolder.get().disconnect("You have been disconnected because the node of your connected service has been timeouted!");
                    }
                    futureAction.complete(false);
                    return;
                }
               for (IRBucketHolder<ICloudPlayer> playerHolder : playerHolders) {
                   IRBucketHolder<ICloudService> fallBack = getFallback(fallBacks);
                   if(fallBack == null){
                       playerHolder.get().disconnect("You have been disconnected because the node of your connected service has been timeouted!");
                       continue;
                   }
                   playerHolder.get().connect(fallBack);
                   playerHolder.get().sendMessage("You have been connected to a fallback service because the node of your connected service has been timeouted!");
               }
               futureAction.complete(true);
       });

        return futureAction;
    }

    private IRBucketHolder<ICloudService> getFallback(Collection<IRBucketHolder<ICloudService>> collection){
        IRBucketHolder<ICloudService> fallBack = null;

        for (IRBucketHolder<ICloudService> serviceHolder : collection) {
            if(serviceHolder.get().getOnlineCount() < serviceHolder.get().getMaxPlayers()) continue;
            if(fallBack == null){
                fallBack = serviceHolder;
                continue;
            }
            if(serviceHolder.get().getMaxPlayers() < fallBack.get().getOnlineCount()){
                fallBack = serviceHolder;
            }
        }

        return fallBack;
    }

    private FutureAction<Boolean> stopServices(IRBucketHolder<ICloudNode> nodeHolder, Collection<IRBucketHolder<ICloudService>> serviceHolders){
        FutureAction<Boolean> futureAction = new FutureAction<>();
        FutureActionCollection<UUID, Boolean> futureActionCollection = new FutureActionCollection<>();
        for (IRBucketHolder<ICloudService> serviceHolder : serviceHolders) {
            CloudAPI.getInstance().getServiceManager().removeFromFetcher(serviceHolder.get().getServiceName());
            futureActionCollection.addToProcess(serviceHolder.get().getUniqueId(),
                    NodeLauncher.getInstance().getServiceManager().deleteBucketAsync(serviceHolder.getIdentifier()));
        }
        futureActionCollection.process()
            .onFailure(futureAction)
            .onSuccess(s -> {
                CloudAPI.getInstance().getNodeManager().getNodeAsync(this.timeOutTargetId)
                    .onFailure(e ->
                            CloudAPI.getInstance().getConsole().error("Error while getting time outed node "
                                    + this.timeOutTargetId + "!"))
                    .onSuccess(nodeHolder2 -> {

                    });
            });
        return futureAction;
    }

    @Override
    public void manageResult(TimeOutResult result, UUID nodeId) {
        this.results.put(nodeId, result);
        if(this.results.values().parallelStream().anyMatch(r -> r == TimeOutResult.UNKNOWN)) return;
        close();
    }

    @Override
    public boolean isOpenerId() {
        return NodeLauncher.getInstance().getNode().getUniqueId().equals(this.openerId);
    }

}
