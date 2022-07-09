package net.suqatri.cloud.api.packet;

public interface ICloudPacketReceiver {

    void receive(ICloudPacket packet);

    void connectPacketListener(Class packetClass);
    void disconnectPacketListener(Class packetClass);
    boolean isPacketListenerConnected(Class packetClass);

}
