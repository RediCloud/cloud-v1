package net.suqatri.cloud.api.redis.event;

import lombok.Data;
import net.suqatri.cloud.api.event.CloudEvent;
import net.suqatri.cloud.api.redis.IRedisConnection;
import org.redisson.client.RedisClient;

@Data
public class RedisConnectedEvent extends CloudEvent {

    private final IRedisConnection connection;

}
