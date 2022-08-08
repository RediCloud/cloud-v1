package net.suqatri.redicloud.api.impl.packet;

import lombok.Getter;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.packet.ICloudPacket;
import net.suqatri.redicloud.api.packet.ICloudPacketReceiver;
import net.suqatri.redicloud.api.packet.ICloudPacketResponse;
import net.suqatri.redicloud.api.packet.PacketChannel;
import net.suqatri.redicloud.commons.function.future.FutureAction;
import org.redisson.api.RFuture;
import org.redisson.api.RTopic;

import java.util.HashMap;

public class CloudPacketReceiver implements ICloudPacketReceiver {

    @Getter
    private final RTopic topic;
    private final HashMap<Class, Integer> listeners;
    private final CloudPacketManager packetManager;
    private final PacketChannel channel;

    public CloudPacketReceiver(CloudPacketManager packetManager, RTopic topic, PacketChannel channel) {
        this.listeners = new HashMap<>();
        this.topic = topic;
        this.packetManager = packetManager;
        this.channel = channel;
    }

    @Override
    public void receive(ICloudPacket packet) {
        if (!CloudAPI.getInstance().getPacketManager().isRegisteredPacket(packet.getClass(), this.channel)) {
            CloudAPI.getInstance().getConsole().warn("Received packet: " + packet.getClass().getSimpleName() + " but it is not registered!");
            return;
        }

        if (!packet.getPacketData().getReceivers().contains(CloudAPI.getInstance().getNetworkComponentInfo())
                && !packet.getPacketData().getReceivers().isEmpty()) return;

        if (packet.getPacketData().getSender().equals(CloudAPI.getInstance().getNetworkComponentInfo())
                && !packet.getPacketData().isSenderAsReceiverAllowed()) return;

        if (packet.getPacketData().getResponseTargetData() != null) {
            if (this.packetManager.getWaitingForResponse().containsKey(packet.getPacketData().getResponseTargetData().getPacketId())) {

                if (!(packet instanceof ICloudPacketResponse)) {
                    CloudAPI.getInstance().getConsole().error("Received packet: " + packet.getClass().getSimpleName() + " but it is not a response packet but was sent as a response packet!");
                    return;
                }

                CloudAPI.getInstance().getConsole().trace("Received response packet: " + packet.getClass().getSimpleName() + " for packet: " + packet.getPacketData().getResponseTargetData().getPacketId());

                packet.receive();

                FutureAction<ICloudPacketResponse> futureAction = this.packetManager.getWaitingForResponse()
                        .get(packet.getPacketData().getResponseTargetData().getPacketId()).getResponseAction();
                if (!futureAction.isCompletedExceptionally() && !futureAction.isDone() && !futureAction.isCancelled()) {
                    futureAction.complete((ICloudPacketResponse) packet);
                }
                this.packetManager.getWaitingForResponse().remove(packet.getPacketData().getResponseTargetData().getPacketId());

            } else {
                CloudAPI.getInstance().getConsole().warn("Received response packet for " + packet.getPacketData().getResponseTargetData().getPacketId() + " but no request is waiting for it!");
            }
        } else {
            CloudAPI.getInstance().getConsole().trace("Received packet: " + packet.getClass().getSimpleName());
            packet.receive();
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
        if (!this.listeners.containsKey(packetClass)) return;
        int listenerId = this.listeners.get(packetClass);
        this.topic.removeListenerAsync(listenerId);
        this.listeners.remove(packetClass);
    }

    @Override
    public <T extends ICloudPacket> boolean isPacketListenerConnected(Class<T> packetClass) {
        return this.listeners.containsKey(packetClass);
    }

}
