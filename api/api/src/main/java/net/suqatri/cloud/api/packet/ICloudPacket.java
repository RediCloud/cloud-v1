package net.suqatri.cloud.api.packet;

public interface ICloudPacket {

    void receive();
    void publish();
    void publishAsync();
    void publishAllAsync();
    void publishAll();
    ICloudPacketData getPacketData();

}
