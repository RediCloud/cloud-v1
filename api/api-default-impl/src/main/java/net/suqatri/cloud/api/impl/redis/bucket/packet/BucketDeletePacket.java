package net.suqatri.cloud.api.impl.redis.bucket.packet;

import net.suqatri.cloud.api.impl.redis.bucket.RedissonBucketManager;
import lombok.Data;
import net.suqatri.cloud.api.impl.packet.CloudPacket;

@Data
public class BucketDeletePacket extends CloudPacket {

    private String identifier;
    private String redisPrefix;

    @Override
    public void receive() {
        RedissonBucketManager manager = RedissonBucketManager.getManager(this.redisPrefix);
        if(manager == null) throw new NullPointerException("RedissonBucketManager is null of prefix " + this.redisPrefix + "!");
        manager.getCachedBucketHolders().remove(this.identifier);
    }
}
