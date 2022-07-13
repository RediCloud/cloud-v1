package net.suqatri.cloud.api.player;

import net.suqatri.cloud.api.redis.bucket.IRBucketObject;

import java.io.Serializable;
import java.util.UUID;

public interface ICloudOfflinePlayer extends IRBucketObject {

    UUID getUniqueId();
    String getName();
    long getFirstLogin();
    long getLastLogin();
    long getLastLogout();
    String getLastIp();

}
