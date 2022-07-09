package net.suqatri.cloud.api.packet;

public interface ICloudPacketReceiver {

    void receive(ICloudPacket packet);

    void connect();
    void disconnect();

}
