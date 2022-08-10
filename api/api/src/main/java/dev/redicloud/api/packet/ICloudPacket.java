package dev.redicloud.api.packet;

import dev.redicloud.api.network.NetworkComponentType;

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
