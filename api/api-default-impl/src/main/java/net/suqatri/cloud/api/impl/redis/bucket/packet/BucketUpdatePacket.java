package net.suqatri.cloud.api.impl.redis.bucket.packet;

import net.suqatri.cloud.api.impl.packet.CloudPacket;
import net.suqatri.cloud.api.impl.redis.bucket.RedissonBucketManager;
import net.suqatri.cloud.api.redis.bucket.packet.IBucketUpdatePacket;

//TODO use SetObjectListener to listen to changes
public class BucketUpdatePacket extends CloudPacket implements IBucketUpdatePacket {

    private String identifier;
    private String redisPrefix;
    private String json;

    @Override
    public void receive() {
        RedissonBucketManager manager = RedissonBucketManager.getManager(this.redisPrefix);
        if(manager == null) throw new NullPointerException("RedissonBucketManager is null of prefix " + this.redisPrefix + "!");
        manager.updateBucket(this.identifier, this.json);
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
