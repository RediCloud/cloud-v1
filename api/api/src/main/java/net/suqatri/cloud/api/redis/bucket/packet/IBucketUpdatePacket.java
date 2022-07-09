package net.suqatri.cloud.api.redis.bucket.packet;

import net.suqatri.cloud.api.packet.ICloudPacket;

public interface IBucketUpdatePacket {

    String getIdentifier();
    String getRedisPrefix();
    String getJson();
    void setJson(String json);
    void setIdentifier(String identifier);
    void setRedisPrefix(String redisPrefix);

}
