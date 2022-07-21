package net.suqatri.redicloud.api.redis.event;

import lombok.Data;
import net.suqatri.redicloud.api.event.CloudEvent;
import net.suqatri.redicloud.api.redis.IRedisConnection;

@Data
public class RedisConnectedEvent extends CloudEvent {

    private final IRedisConnection connection;

}
