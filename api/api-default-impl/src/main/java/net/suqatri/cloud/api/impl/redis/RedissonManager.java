package net.suqatri.cloud.api.impl.redis;

import net.suqatri.cloud.api.impl.CloudDefaultAPIImpl;
import net.suqatri.cloud.api.redis.IRedissonManager;
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
