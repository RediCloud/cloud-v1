package net.suqatri.cloud.api.packet;

public interface ICloudPacketReceiver {

    void receive(ICloudPacket packet);

    <T extends ICloudPacket> void connectPacketListener(Class<T> packetClass);
    <T extends ICloudPacket> void disconnectPacketListener(Class<T> packetClass);
    <T extends ICloudPacket> boolean isPacketListenerConnected(Class<T> packetClass);

}
