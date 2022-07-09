package net.suqatri.cloud.api.impl;

import lombok.Getter;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.impl.event.CloudEventHandler;
import net.suqatri.cloud.api.redis.IRedisConnection;
import net.suqatri.cloud.api.utils.ApplicationType;

@Getter
public abstract class CloudDefaultAPIImpl extends CloudAPI {

    private CloudEventHandler eventHandler;

    public CloudDefaultAPIImpl(ApplicationType type) {
        super(type);
        this.eventHandler = new CloudEventHandler();
    }

    public abstract IRedisConnection getRedisConnection();

}
