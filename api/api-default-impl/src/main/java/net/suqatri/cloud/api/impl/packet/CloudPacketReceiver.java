package net.suqatri.cloud.api.impl.packet;

import lombok.Getter;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.impl.CloudDefaultAPIImpl;
import net.suqatri.cloud.api.packet.ICloudPacket;
import net.suqatri.cloud.api.packet.ICloudPacketData;
import net.suqatri.cloud.api.packet.ICloudPacketReceiver;
import net.suqatri.cloud.commons.function.future.FutureAction;
import org.redisson.api.RFuture;
import org.redisson.api.RTopic;
import org.redisson.api.listener.MessageListener;
import org.redisson.codec.JsonJacksonCodec;

import java.util.HashMap;

public class CloudPacketReceiver implements ICloudPacketReceiver {

    @Getter
    private final RTopic topic;
    private final HashMap<Class, Integer> listeners;
    private final CloudPacketManager packetManager;

    public CloudPacketReceiver(CloudPacketManager packetManager, RTopic topic) {
        this.listeners = new HashMap<>();
        this.topic = topic;
        this.packetManager = packetManager;
    }

    @Override
    public void receive(ICloudPacket packet) {
        if(!CloudAPI.getInstance().getPacketManager().isRegisteredPacket(packet.getClass())){
            CloudAPI.getInstance().getConsole().warn("Received packet: " + packet.getClass().getSimpleName() + " but it is not registered!");
            return;
        }
        if(!packet.getPacketData().getReceivers().contains(CloudAPI.getInstance().getNetworkComponentInfo()) && !packet.getPacketData().getReceivers().isEmpty()) return;
        if(packet.getPacketData().getSender().equals(CloudAPI.getInstance().getNetworkComponentInfo()) && !packet.getPacketData().isSenderAsReceiverAllowed()) return;
        if(packet.getPacketData().getResponseTargetData() != null){
            if(this.packetManager.getWaitingForResponse().containsKey(packet.getPacketData().getResponseTargetData().getPacketId())){
                CloudAPI.getInstance().getConsole().debug("Received response packet: " + packet.getClass().getSimpleName() + " for packet: " + packet.getPacketData().getResponseTargetData().getPacketId());
                packet.receive();
                FutureAction<ICloudPacket> futureAction = this.packetManager.getWaitingForResponse().get(packet.getPacketData().getResponseTargetData().getPacketId()).getResponseAction();
                if(!futureAction.isCompletedExceptionally() && !futureAction.isDone() && !futureAction.isCancelled()){
                    futureAction.complete(packet);
                }
                this.packetManager.getWaitingForResponse().remove(packet.getPacketData().getResponseTargetData().getPacketId());
            }else{
                CloudAPI.getInstance().getConsole().warn("Received response packet for " + packet.getPacketData().getResponseTargetData().getPacketId() + " but no request is waiting for it!");
            }
        }else{
            CloudAPI.getInstance().getConsole().debug("Received packet: " + packet.getClass().getSimpleName());
        }

    }

    @Override
    public <T extends ICloudPacket> void connectPacketListener(Class<T> packetClass) {
        RFuture<Integer> future = this.topic.addListenerAsync(packetClass, (charSequence, packet) -> this.receive(packet));
        future.whenComplete((v, e) -> {
            if (e != null) {
                CloudAPI.getInstance().getConsole().error("Failed to connect packet listener for " + packetClass.getName(), e);
                return;
            }
            CloudAPI.getInstance().getConsole().debug("Connected packet listener for " + packetClass.getName());
            this.listeners.put(packetClass, v);
        });
    }

    @Override
    public <T extends ICloudPacket> void disconnectPacketListener(Class<T> packetClass) {
        if(!this.listeners.containsKey(packetClass)) return;
        int listenerId = this.listeners.get(packetClass);
        this.topic.removeListenerAsync(listenerId);
        this.listeners.remove(packetClass);
    }

    @Override
    public <T extends ICloudPacket> boolean isPacketListenerConnected(Class<T> packetClass) {
        return this.listeners.containsKey(packetClass);
    }

}
