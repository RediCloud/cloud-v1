package net.suqatri.cloud.api.impl.redis.bucket.packet;

import net.suqatri.cloud.api.impl.packet.CloudPacket;
import net.suqatri.cloud.api.impl.redis.bucket.RedissonBucketManager;
import net.suqatri.cloud.api.redis.bucket.packet.IBucketUpdatePacket;

public class BucketUpdatePacket extends CloudPacket implements IBucketUpdatePacket {

    private String identifier;
    private String redisPrefix;
    private String json;

    @Override
    public void receive() {
        RedissonBucketManager.getManager(this.redisPrefix + "@" + this.identifier).updateBucket(this.identifier, this.json);
    }

    @Override
    public String getIdentifier() {
        return this.identifier;
    }

    @Override
    public String getRedisPrefix() {
        return this.redisPrefix;
    }

    @Override
    public String getJson() {
        return this.json;
    }

    @Override
    public void setJson(String json) {
        this.json = json;
    }

    @Override
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public void setRedisPrefix(String redisPrefix) {
        this.redisPrefix = redisPrefix;
    }
}
