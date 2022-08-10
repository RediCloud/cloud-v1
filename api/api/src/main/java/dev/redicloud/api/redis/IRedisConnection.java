package dev.redicloud.api.redis;

import org.redisson.api.RedissonClient;

public interface IRedisConnection {

    RedisCredentials getCredentials();

    void connect();

    void disconnect();

    boolean isConnected();

    void reconnect();

    RedissonClient getClient();

}
