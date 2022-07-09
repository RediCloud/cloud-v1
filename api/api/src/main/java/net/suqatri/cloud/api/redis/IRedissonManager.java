package net.suqatri.cloud.api.redis;

import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.codec.JsonJacksonCodec;

public interface IRedissonManager {

    RedissonClient getClient();
    JsonJacksonCodec getObjectCodec();
    String getRedisPrefix();
    default String getRedisKey(String identifier){
        return getRedisPrefix() + "@" + identifier;
    }

}
