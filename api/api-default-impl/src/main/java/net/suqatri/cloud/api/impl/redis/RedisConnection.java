package net.suqatri.cloud.api.impl.redis;

import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.impl.redis.bucket.packet.BucketUpdatePacket;
import net.suqatri.cloud.api.redis.IRedisConnection;
import net.suqatri.cloud.api.redis.RedisCredentials;
import net.suqatri.cloud.api.redis.event.RedisConnectedEvent;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.connection.pool.MasterPubSubConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedisConnection implements IRedisConnection {

    private RedisCredentials redisCredentials;
    private RedissonClient client;

    public RedisConnection(RedisCredentials redisCredentials) {
        this.redisCredentials = redisCredentials;
        this.registerPackets();
    }

    private void registerPackets(){
        CloudAPI.getInstance().getPacketManager().registerPacket(BucketUpdatePacket.class);
    }

    @Override
    public RedisCredentials getCredentials() {
        return this.redisCredentials;
    }

    @Override
    public void connect() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + this.redisCredentials.getHostname() + ":" + this.redisCredentials.getPort())
                .setPassword(this.redisCredentials.getPassword())
                .setDatabase(this.redisCredentials.getDatabaseId());
        this.client = Redisson.create(config);

        RedisConnectedEvent redisConnectedEvent = new RedisConnectedEvent(this);
        CloudAPI.getInstance().getEventManager().postLocal(redisConnectedEvent);
    }

    @Override
    public void disconnect() {
        this.client.shutdown();
    }

    @Override
    public boolean isConnected() {
        if(this.client == null) return false;
        return !this.client.isShutdown();
    }

    @Override
    public void reconnect() {
        this.disconnect();
        this.connect();
    }

    @Override
    public RedissonClient getClient() {
        return this.client;
    }
}
