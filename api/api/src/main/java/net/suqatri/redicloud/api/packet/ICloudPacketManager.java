package net.suqatri.redicloud.api.packet;

import net.suqatri.redicloud.commons.function.future.FutureAction;

import java.util.Collection;

public interface ICloudPacketManager {

    void registerForResponse(ICloudPacketData packetData);

    void registerPacket(Class<? extends ICloudPacket> packet, PacketChannel channel);

    void unregisterPacket(Class<? extends ICloudPacket> packet, PacketChannel channel);

    boolean isRegisteredPacket(Class<? extends ICloudPacket> cloudPacket, PacketChannel channel);

    Collection<Class<? extends ICloudPacket>> getRegisteredPackets(PacketChannel channel);

    ICloudPacketReceiver getReceiver(PacketChannel channel);

    void publish(ICloudPacket packet);

    FutureAction<Long> publishAsync(ICloudPacket packet);

}
