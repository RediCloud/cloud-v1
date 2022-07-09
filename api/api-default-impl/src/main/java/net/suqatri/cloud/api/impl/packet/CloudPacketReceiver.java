package net.suqatri.cloud.api.impl.packet;

import lombok.Getter;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.impl.CloudDefaultAPIImpl;
import net.suqatri.cloud.api.packet.ICloudPacket;
import net.suqatri.cloud.api.packet.ICloudPacketReceiver;
import org.redisson.api.RFuture;
import org.redisson.api.RTopic;
import org.redisson.api.listener.MessageListener;
import org.redisson.codec.JsonJacksonCodec;

import java.util.HashMap;

public class CloudPacketReceiver implements ICloudPacketReceiver {

    @Getter
    private final RTopic topic;
    private final HashMap<Class, Integer> listeners;

    public CloudPacketReceiver(RTopic topic) {
        this.listeners = new HashMap<>();
        this.topic = topic;
    }

    @Override
    public void receive(ICloudPacket packet) {
        packet.receive();
    }

    @Override
    public void connectPacketListener(Class packetClass) {
        RFuture<Integer> future = this.topic.addListenerAsync(packetClass, (MessageListener<ICloudPacket>) (charSequence, packet) -> {
            packet.receive();
        });
        future.whenComplete((v, e) -> {
            if (e != null) {
                CloudAPI.getInstance().getConsole().error("Failed to connect packet listener for " + packetClass.getName(), e);
                return;
            }
            this.listeners.put(packetClass, v);
        });
    }

    @Override
    public void disconnectPacketListener(Class packetClass) {
        if(!this.listeners.containsKey(packetClass)) return;
        int listenerId = this.listeners.get(packetClass);
        this.topic.removeListenerAsync(listenerId);
        this.listeners.remove(packetClass);
    }

    @Override
    public boolean isPacketListenerConnected(Class packetClass) {
        return this.listeners.containsKey(packetClass);
    }

}
