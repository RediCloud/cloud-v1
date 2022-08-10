package dev.redicloud.api.impl.redis;

import dev.redicloud.api.impl.CloudDefaultAPIImpl;
import dev.redicloud.api.redis.IRedissonManager;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;

public abstract class RedissonManager implements IRedissonManager {

    @Override
    public RedissonClient getClient() {
        return CloudDefaultAPIImpl.getInstance().getRedisConnection().getClient();
    }

    @Override
    public JsonJacksonCodec getObjectCodec() {
        return new JsonJacksonCodec();
    }

}
