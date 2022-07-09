package net.suqatri.cloud.api.player;

import java.io.Serializable;
import java.util.UUID;

public interface ICloudOfflinePlayer extends Serializable {

    UUID getUniqueId();
    String getName();
    long getFirstLogin();
    long getLastLogin();
    long getLastLogout();
    String getLastIp();

}
