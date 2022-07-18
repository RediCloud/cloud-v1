package net.suqatri.cloud.api.impl.packet;

import lombok.Getter;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.impl.CloudDefaultAPIImpl;
import net.suqatri.cloud.api.impl.event.packet.GlobalEventPacket;
import net.suqatri.cloud.api.network.INetworkComponentInfo;
import net.suqatri.cloud.api.packet.ICloudPacket;
import net.suqatri.cloud.api.packet.ICloudPacketData;
import net.suqatri.cloud.api.packet.ICloudPacketManager;
import net.suqatri.cloud.api.packet.ICloudPacketReceiver;
import net.suqatri.cloud.api.redis.event.RedisConnectedEvent;
import net.suqatri.cloud.commons.function.future.FutureAction;
import org.redisson.api.RTopic;
import org.redisson.codec.JsonJacksonCodec;

import java.util.*;
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
        CloudAPI.getInstance().getConsole().debug("Publishing packet " + packet.getClass().getName() + " to [" + packet.getPacketData().getReceivers().parallelStream().map(INetworkComponentInfo::getKey).collect(Collectors.joining(", ")) + "]");
        this.topic.publish(packet);
        if(!this.isRegisteredPacket(packet.getClass())){
            CloudAPI.getInstance().getConsole().warn("The published packet " + packet.getClass().getSimpleName() + " is not registered.");
        }
    }

    @Override
    public FutureAction<Long> publishAsync(ICloudPacket packet) {
        if(this.topic == null) return new FutureAction<>(new NullPointerException("Cannot publish packet " + packet.getClass().getSimpleName() + " because topic is not connected."));
        CloudAPI.getInstance().getConsole().debug("Publishing packet " + packet.getClass().getName() + " to [" + packet.getPacketData().getReceivers().parallelStream().map(INetworkComponentInfo::getKey).collect(Collectors.joining(", ")) + "]");
        if(!this.isRegisteredPacket(packet.getClass())){
            CloudAPI.getInstance().getConsole().warn("The published packet " + packet.getClass().getSimpleName() + " is not registered.");
        }
        return new FutureAction<>(this.topic.publishAsync(packet));
    }
}
