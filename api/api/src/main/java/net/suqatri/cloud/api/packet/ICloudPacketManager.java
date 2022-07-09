package net.suqatri.cloud.api.packet;

import java.util.Collection;

public interface ICloudPacketManager {

    void registerPacket(ICloudPacket packet);
    void unregisterPacket(ICloudPacket packet);
    boolean isRegisteredPacket(ICloudPacket cloudPacket);
    Collection<ICloudPacket> getRegisteredPackets();
    ICloudPacketReceiver getReceiver();
    void publish(ICloudPacket packet);
    void publishAsync(ICloudPacket packet);

}
