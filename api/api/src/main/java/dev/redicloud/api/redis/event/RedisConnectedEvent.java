package dev.redicloud.api.redis.event;

import lombok.Data;
import dev.redicloud.api.event.CloudEvent;
import dev.redicloud.api.redis.IRedisConnection;

@Data
public class RedisConnectedEvent extends CloudEvent {

    private final IRedisConnection connection;

}
