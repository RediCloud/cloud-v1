package dev.redicloud.api.redis;

import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;

public interface IRedissonManager {

    RedissonClient getClient();

    JsonJacksonCodec getObjectCodec();

    String getRedisPrefix();

    default String getRedisKey(String identifier) {
        return getRedisPrefix() + ":" + identifier;
    }

}
