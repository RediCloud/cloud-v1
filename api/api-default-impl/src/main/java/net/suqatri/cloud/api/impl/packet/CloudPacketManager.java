package net.suqatri.cloud.api.impl.packet;

import net.suqatri.cloud.api.impl.CloudDefaultAPIImpl;
import net.suqatri.cloud.api.packet.ICloudPacket;
import net.suqatri.cloud.api.packet.ICloudPacketManager;
import net.suqatri.cloud.api.packet.ICloudPacketReceiver;
import net.suqatri.cloud.commons.function.future.FutureAction;
import org.redisson.api.RTopic;
import org.redisson.codec.JsonJacksonCodec;

import java.util.ArrayList;
import java.util.Collection;

public class CloudPacketManager implements ICloudPacketManager {

    private final Collection<Class<? extends ICloudPacket>> packets;
    private final CloudPacketReceiver receiver;
    private final RTopic topic;

    public CloudPacketManager(){
        this.packets = new ArrayList<>();
        this.topic = CloudDefaultAPIImpl.getInstance().getRedisConnection().getClient().getTopic("cloud:packet", new JsonJacksonCodec());
        this.receiver = new CloudPacketReceiver(this.topic);
    }

    @Override
    public void registerPacket(Class<? extends ICloudPacket> packet) {
        this.packets.add(packet);
        this.receiver.connectPacketListener(packet);
    }

    @Override
    public void unregisterPacket(Class<? extends ICloudPacket> packet) {
        this.packets.remove(packet);
        this.receiver.disconnectPacketListener(packet);
    }

    @Override
    public boolean isRegisteredPacket(Class<? extends ICloudPacket> cloudPacket) {
        return this.packets.contains(cloudPacket);
    }

    @Override
    public Collection<Class<? extends ICloudPacket>> getRegisteredPackets() {
        return this.packets;
    }

    @Override
    public ICloudPacketReceiver getReceiver() {
        return this.receiver;
    }

    @Override
    public void publish(ICloudPacket packet) {
        this.topic.publish(packet);
    }

    @Override
    public FutureAction<Long> publishAsync(ICloudPacket packet) {
        return new FutureAction<>(this.topic.publishAsync(packet));
    }
}
