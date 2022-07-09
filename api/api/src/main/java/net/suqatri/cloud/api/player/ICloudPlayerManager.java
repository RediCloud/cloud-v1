package net.suqatri.cloud.api.player;

import java.util.UUID;

public interface ICloudPlayerManager {

    ICloudPlayer getPlayer(String playerName);
    ICloudPlayer getPlayer(UUID uniqueId);

    ICloudOfflinePlayer getOfflinePlayer(String playerName);
    ICloudOfflinePlayer getOfflinePlayer(UUID uniqueId);

    boolean isConnected(String playerName);
    boolean isConnected(UUID uniqueId);

    int getRegisteredCount();
    int getOnlineCount();

}
