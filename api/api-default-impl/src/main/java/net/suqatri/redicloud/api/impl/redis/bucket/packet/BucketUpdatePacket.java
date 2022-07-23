package net.suqatri.redicloud.api.impl.redis.bucket.packet;

import lombok.Data;
import net.suqatri.redicloud.api.impl.packet.CloudPacket;
import net.suqatri.redicloud.api.impl.redis.bucket.RedissonBucketManager;

//TODO use SetObjectListener to listen to changes
//SetObjectListener dont work...
@Data
public class BucketUpdatePacket extends CloudPacket {

    private String identifier;
    private String redisPrefix;
    private String json;

    @Override
    public void receive() {
        RedissonBucketManager manager = RedissonBucketManager.getManager(this.redisPrefix);
        if (manager == null)
            throw new NullPointerException("RedissonBucketManager is null of prefix " + this.redisPrefix + "!");
        manager.updateBucket(this.identifier, this.json);
    }

}
