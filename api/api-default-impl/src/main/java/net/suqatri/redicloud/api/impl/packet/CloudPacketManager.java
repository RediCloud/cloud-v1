package net.suqatri.redicloud.api.impl.packet;

import lombok.Getter;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.CloudDefaultAPIImpl;
import net.suqatri.redicloud.api.impl.event.packet.GlobalEventPacket;
import net.suqatri.redicloud.api.network.INetworkComponentInfo;
import net.suqatri.redicloud.api.packet.*;
import net.suqatri.redicloud.api.redis.event.RedisConnectedEvent;
import net.suqatri.redicloud.api.utils.ApplicationType;
import net.suqatri.redicloud.commons.function.future.FutureAction;
import org.redisson.api.RTopic;
import org.redisson.codec.JsonJacksonCodec;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class CloudPacketManager implements ICloudPacketManager {

    private final HashMap<PacketChannel, List<Class<? extends ICloudPacket>>> packets;
    private final ConcurrentHashMap<PacketChannel, List<Class<? extends ICloudPacket>>> packetWaitingList;
    @Getter
    private final ConcurrentHashMap<UUID, ICloudPacketData> waitingForResponse;
    private HashMap<PacketChannel, CloudPacketReceiver> receivers;
    private RTopic globalTopic;
    private RTopic nodeTopic;

    public CloudPacketManager() {
        this.packets = new HashMap<>();
        this.packetWaitingList = new ConcurrentHashMap<>();
        this.waitingForResponse = new ConcurrentHashMap<>();
        CloudAPI.getInstance().getEventManager().register(RedisConnectedEvent.class, event -> {
            CloudAPI.getInstance().getConsole().info("Starting connection to packet topics...");

            this.globalTopic = CloudDefaultAPIImpl.getInstance().getRedisConnection().getClient()
                    .getTopic("cloud:" + PacketChannel.NODE.getName(), new JsonJacksonCodec());

            this.nodeTopic = CloudDefaultAPIImpl.getInstance().getRedisConnection().getClient()
                    .getTopic("cloud:nodes" + PacketChannel.GLOBAL.getName(), new JsonJacksonCodec());

            this.receivers.put(PacketChannel.GLOBAL,
                    new CloudPacketReceiver(this, this.globalTopic, PacketChannel.GLOBAL));

            if(CloudAPI.getInstance().getApplicationType() == ApplicationType.NODE){
                this.receivers.put(PacketChannel.NODE,
                        new CloudPacketReceiver(this, this.nodeTopic, PacketChannel.NODE));
            }



            CloudAPI.getInstance().getConsole().info("Connecting to event cluster...");
            registerPacket(GlobalEventPacket.class, PacketChannel.GLOBAL);
        });
    }

    @Override
    public void registerForResponse(ICloudPacketData packetData) {
        this.waitingForResponse.put(packetData.getPacketId(), packetData);
    }

    @Override
    public void registerPacket(Class<? extends ICloudPacket> packet, PacketChannel channel) {
        CloudAPI.getInstance().getConsole().debug("Registering packet: " + packet.getName());
        List<Class<? extends ICloudPacket>> packets = this.packets.getOrDefault(channel, new ArrayList<>());
        packets.add(packet);
        this.packets.put(channel, packets);
        if (this.receivers.containsKey(channel)) {
            this.receivers.get(channel).connectPacketListener(packet);
        } else {
            List<Class<? extends ICloudPacket>> list = this.packetWaitingList.getOrDefault(channel, new ArrayList<>());
            list.add(packet);
            this.packetWaitingList.put(channel, list);
        }
    }

    @Override
    public void unregisterPacket(Class<? extends ICloudPacket> packet, PacketChannel channel) {
        CloudAPI.getInstance().getConsole().debug("Unregistering packet: " + packet.getName());
        List<Class<? extends ICloudPacket>> packets = this.packets.getOrDefault(channel, new ArrayList<>());
        packets.remove(packet);
        this.packets.put(channel, packets);
        if (this.receivers.containsKey(channel)) {
            this.receivers.get(channel).disconnectPacketListener(packet);
        } else {
            List<Class<? extends ICloudPacket>> list = this.packetWaitingList.getOrDefault(channel, new ArrayList<>());
            list.remove(packet);
            this.packetWaitingList.put(channel, list);
        }
    }

    @Override
    public boolean isRegisteredPacket(Class<? extends ICloudPacket> cloudPacket, PacketChannel channel) {
        if(!this.packets.containsKey(channel)) return false;
        return this.packets.get(channel).contains(cloudPacket);
    }

    @Override
    public Collection<Class<? extends ICloudPacket>> getRegisteredPackets(PacketChannel channel) {
        return this.packets.getOrDefault(channel, new ArrayList<>());
    }

    @Override
    public ICloudPacketReceiver getReceiver(PacketChannel channel) {
        return this.receivers.getOrDefault(channel, null);
    }

    @Override
    public void publish(ICloudPacket packet) {
        if (this.globalTopic == null) {
            CloudAPI.getInstance().getConsole().warn("Cannot publish packet " + packet.getClass().getSimpleName() + " because topic is not connected.");
            return;
        }
        CloudAPI.getInstance().getConsole().trace("Publishing packet " + packet.getClass().getName() + " to [" + packet.getPacketData().getReceivers().parallelStream().map(INetworkComponentInfo::getKey).collect(Collectors.joining(", ")) + "]");
        if (!this.isRegisteredPacket(packet.getClass(), packet.getPacketData().getChannel())) {
            CloudAPI.getInstance().getConsole().warn("The published packet " + packet.getClass().getSimpleName() + " is not registered.");
        }

        switch (packet.getPacketData().getChannel()){
            case GLOBAL:
                this.globalTopic.publish(packet);
                return;
            case NODE:
                this.nodeTopic.publish(packet);
                return;
        }
    }

    @Override
    public FutureAction<Long> publishAsync(ICloudPacket packet) {
        if (this.globalTopic == null || this.nodeTopic == null)
            return new FutureAction<>(new NullPointerException("Cannot publish packet " + packet.getClass().getSimpleName() + " because topic is not connected."));
        CloudAPI.getInstance().getConsole().trace("Publishing packet " + packet.getClass().getName() + " to [" + packet.getPacketData().getReceivers().parallelStream().map(INetworkComponentInfo::getKey).collect(Collectors.joining(", ")) + "]");
        if (!this.isRegisteredPacket(packet.getClass(), packet.getPacketData().getChannel())) {
            CloudAPI.getInstance().getConsole().warn("The published packet " + packet.getClass().getSimpleName() + " is not registered.");
        }
        switch (packet.getPacketData().getChannel()){
            case GLOBAL:
                return new FutureAction<>(this.globalTopic.publishAsync(packet));
            case NODE:
                return new FutureAction<>(this.nodeTopic.publishAsync(packet));
        }
        return null;
    }
}
