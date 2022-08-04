package net.suqatri.redicloud.api.impl.redis;

import lombok.Getter;
import lombok.Setter;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.redis.IRedisConnection;
import net.suqatri.redicloud.api.redis.RedisCredentials;
import net.suqatri.redicloud.api.redis.event.RedisConnectedEvent;
import net.suqatri.redicloud.api.redis.event.RedisDisconnectedEvent;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;

import java.util.stream.Collectors;

@Getter
public class RedisConnection implements IRedisConnection {

    private RedisCredentials redisCredentials;
    private RedissonClient client;

    @Setter
    private int subscriptionConnectionMinimumIdleSize = 1;
    @Setter
    private int subscriptionConnectionPoolSize = 50;
    @Setter
    private int connectionMinimumIdleSize = 24;
    @Setter
    private int connectionPoolSize = 64;

    public RedisConnection(RedisCredentials redisCredentials) {
        this.redisCredentials = redisCredentials;
    }

    @Override
    public RedisCredentials getCredentials() {
        return this.redisCredentials;
    }

    @Override
    public void connect() {
        Config config = new Config();
        switch (this.redisCredentials.getType()){
            case CLUSTER:
                config.useClusterServers()
                        .setNodeAddresses(this.redisCredentials.getNodeAddresses()
                                .keySet()
                                .parallelStream()
                                .map(hostname -> this.redisCredentials.toNodeAddress(hostname))
                                .collect(Collectors.toList()));
                config.useClusterServers()
                        .setSubscriptionConnectionMinimumIdleSize(this.subscriptionConnectionMinimumIdleSize)
                        .setSubscriptionConnectionPoolSize(this.subscriptionConnectionPoolSize);

                if(this.redisCredentials.getPassword() != null){
                    config.useClusterServers().setPassword(this.redisCredentials.getPassword());
                }
                break;
            case SINGLE_SERVICE:
                config.useSingleServer()
                        .setSubscriptionConnectionMinimumIdleSize(this.subscriptionConnectionMinimumIdleSize)
                        .setSubscriptionConnectionPoolSize(this.subscriptionConnectionPoolSize)
                        .setConnectionMinimumIdleSize(this.connectionMinimumIdleSize)
                        .setConnectionPoolSize(this.connectionPoolSize)
                        .setAddress(this.redisCredentials.getAnyNodeAddress())
                        .setDatabase(this.redisCredentials.getDatabaseId());

                if (this.redisCredentials.getPassword() != null) {
                    config.useSingleServer().setPassword(this.redisCredentials.getPassword());
                }
                break;
        }

        this.client = Redisson.create(config);

        RedisConnectedEvent redisConnectedEvent = new RedisConnectedEvent(this);
        CloudAPI.getInstance().getEventManager().postLocal(redisConnectedEvent);
    }

    @Override
    public void disconnect() {
        if(this.client == null) return;
        CloudAPI.getInstance().getEventManager().postLocal(new RedisDisconnectedEvent());
        this.client.shutdown();
    }

    @Override
    public boolean isConnected() {
        if (this.client == null) return false;
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
