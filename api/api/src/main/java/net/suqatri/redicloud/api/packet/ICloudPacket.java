package net.suqatri.redicloud.api.packet;

import net.suqatri.redicloud.api.network.NetworkComponentType;

public interface ICloudPacket {

    void receive();
    void publish();
    void publishAsync();
    void publishAllAsync();
    void publishAll();
    void publishAll(NetworkComponentType type);
    void publishAllAsync(NetworkComponentType type);
    ICloudPacketData getPacketData();

}
