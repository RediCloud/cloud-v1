package net.suqatri.cloud.api.impl;

import lombok.Getter;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.event.ICloudEventManager;
import net.suqatri.cloud.api.impl.event.CloudEventManager;
import net.suqatri.cloud.api.impl.packet.CloudPacketManager;
import net.suqatri.cloud.api.packet.ICloudPacketManager;
import net.suqatri.cloud.api.redis.IRedisConnection;
import net.suqatri.cloud.api.utils.ApplicationType;

@Getter
public abstract class CloudDefaultAPIImpl extends CloudAPI {

    @Getter
    private static CloudDefaultAPIImpl instance;

    private ICloudEventManager eventManager;
    private ICloudPacketManager packetManager;

    public CloudDefaultAPIImpl(ApplicationType type) {
        super(type);
        instance = this;
        this.packetManager = new CloudPacketManager();
        this.eventManager = new CloudEventManager();
    }

    public abstract IRedisConnection getRedisConnection();

}
