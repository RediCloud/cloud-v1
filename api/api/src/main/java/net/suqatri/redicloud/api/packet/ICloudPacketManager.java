package net.suqatri.redicloud.api.packet;

import net.suqatri.redicloud.commons.function.future.FutureAction;

import java.util.Collection;

public interface ICloudPacketManager {

    void registerForResponse(ICloudPacketData packetData);

    void registerPacket(Class<? extends ICloudPacket> packet);
    void unregisterPacket(Class<? extends ICloudPacket>  packet);
    boolean isRegisteredPacket(Class<? extends ICloudPacket>  cloudPacket);
    Collection<Class<? extends ICloudPacket>> getRegisteredPackets();

    ICloudPacketReceiver getReceiver();

    void publish(ICloudPacket packet);
    FutureAction<Long> publishAsync(ICloudPacket packet);

}
