package net.suqatri.cloud.api.player;

import java.util.UUID;

public interface ICloudOfflinePlayer {

    UUID getUniqueId();
    String getName();
    long getFirstLogin();
    long getLastLogin();
    long getLastLogout();
    String getLastIp();

}
