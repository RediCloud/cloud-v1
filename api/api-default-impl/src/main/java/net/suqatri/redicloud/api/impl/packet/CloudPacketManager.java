package net.suqatri.redicloud.api.impl.packet;

import net.suqatri.redicloud.api.impl.event.packet.GlobalEventPacket;
import lombok.Getter;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.CloudDefaultAPIImpl;
import net.suqatri.redicloud.api.network.INetworkComponentInfo;
import net.suqatri.redicloud.api.packet.ICloudPacket;
import net.suqatri.redicloud.api.packet.ICloudPacketData;
import net.suqatri.redicloud.api.packet.ICloudPacketManager;
import net.suqatri.redicloud.api.packet.ICloudPacketReceiver;
import net.suqatri.redicloud.api.redis.event.RedisConnectedEvent;
import net.suqatri.redicloud.commons.function.future.FutureAction;
import org.redisson.api.RTopic;
import org.redisson.codec.JsonJacksonCodec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class CloudPacketManager implements ICloudPacketManager {

    private final Collection<Class<? extends ICloudPacket>> packets;
    private CloudPacketReceiver receiver;
    private RTopic topic;
    private final List<Class<? extends ICloudPacket>> packetWaitingList;
    @Getter
    private final ConcurrentHashMap<UUID, ICloudPacketData> waitingForResponse;

    public CloudPacketManager(){
        this.packets = new ArrayList<>();
        this.packetWaitingList = new ArrayList<>();
        this.waitingForResponse = new ConcurrentHashMap<>();
        CloudAPI.getInstance().getEventManager().register(RedisConnectedEvent.class, event -> {
            CloudAPI.getInstance().getConsole().info("Starting connection to packet topic...");
            this.topic = CloudDefaultAPIImpl.getInstance().getRedisConnection().getClient().getTopic("cloud:packet", new JsonJacksonCodec());
            this.receiver = new CloudPacketReceiver(this, this.topic);
            for (Class<? extends ICloudPacket> aClass : this.packetWaitingList) {
                this.receiver.connectPacketListener(aClass);
            }
            CloudAPI.getInstance().getConsole().info("Connecting to event cluster...");
            registerPacket(GlobalEventPacket.class);
        });
    }

    @Override
    public void registerForResponse(ICloudPacketData packetData) {
        this.waitingForResponse.put(packetData.getPacketId(), packetData);
    }

    @Override
    public void registerPacket(Class<? extends ICloudPacket> packet) {
        CloudAPI.getInstance().getConsole().debug("Registering packet: " + packet.getName());
        this.packets.add(packet);
        if(this.receiver != null){
            this.receiver.connectPacketListener(packet);
        }else {
            this.packetWaitingList.add(packet);
        }
    }

    @Override
    public void unregisterPacket(Class<? extends ICloudPacket> packet) {
        CloudAPI.getInstance().getConsole().debug("Unregistering packet: " + packet.getName());
        this.packets.remove(packet);
        if(this.receiver != null){
            this.receiver.disconnectPacketListener(packet);
        }else{
            this.packetWaitingList.remove(packet);
        }
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
        if(this.topic == null) {
            CloudAPI.getInstance().getConsole().warn("Cannot publish packet " + packet.getClass().getSimpleName() + " because topic is not connected.");
            return;
        }
        CloudAPI.getInstance().getConsole().trace("Publishing packet " + packet.getClass().getName() + " to [" + packet.getPacketData().getReceivers().parallelStream().map(INetworkComponentInfo::getKey).collect(Collectors.joining(", ")) + "]");
        this.topic.publish(packet);
        if(!this.isRegisteredPacket(packet.getClass())){
            CloudAPI.getInstance().getConsole().warn("The published packet " + packet.getClass().getSimpleName() + " is not registered.");
        }
    }

    @Override
    public FutureAction<Long> publishAsync(ICloudPacket packet) {
        if(this.topic == null) return new FutureAction<>(new NullPointerException("Cannot publish packet " + packet.getClass().getSimpleName() + " because topic is not connected."));
        CloudAPI.getInstance().getConsole().trace("Publishing packet " + packet.getClass().getName() + " to [" + packet.getPacketData().getReceivers().parallelStream().map(INetworkComponentInfo::getKey).collect(Collectors.joining(", ")) + "]");
        if(!this.isRegisteredPacket(packet.getClass())){
            CloudAPI.getInstance().getConsole().warn("The published packet " + packet.getClass().getSimpleName() + " is not registered.");
        }
        return new FutureAction<>(this.topic.publishAsync(packet));
    }
}
