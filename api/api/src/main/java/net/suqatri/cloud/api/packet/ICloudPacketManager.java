package net.suqatri.cloud.api.packet;

public interface ICloudPacketManager {

    void registerPacket(CloudPacket packet);
    void unregisterPacket(CloudPacket packet);
    boolean isRegisteredPacket(CloudPacket cloudPacket);

}
