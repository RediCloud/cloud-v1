package net.suqatri.cloud.api.service;

import net.suqatri.cloud.api.group.ICloudGroup;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.redis.bucket.IRBucketObject;

import java.io.Serializable;
import java.util.UUID;

public interface ICloudService extends IRBucketObject {

    IServiceStartConfiguration getConfiguration();

    default ServiceEnvironment getEnvironment(){
        return getConfiguration().getEnvironment();
    }

    default String getName(){
        return getConfiguration().getName();
    }
    default UUID getUniqueId() { return getConfiguration().getUniqueId(); }
    default int getId(){
        return getConfiguration().getId();
    }

    default IRBucketHolder<ICloudGroup> getGroup(){
        return getConfiguration().getGroup();
    }

    String getMotd();
    void setMotd(String motd);

    int getMaxPlayers();
    void setMaxPlayers(int maxPlayers);

    default boolean isStatic(){
        return getConfiguration().isStatic();
    }



}
