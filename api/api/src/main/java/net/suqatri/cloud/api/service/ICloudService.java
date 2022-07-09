package net.suqatri.cloud.api.service;

import net.suqatri.cloud.api.group.ICloudGroup;

import java.util.UUID;

public interface ICloudService {

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

    ICloudGroup getGroup();

    String getMotd();
    void setMotd(String motd);

    int getMaxPlayers();
    void setMaxPlayers(int maxPlayers);

    default boolean isStatic(){
        return getConfiguration().isStatic();
    }



}
