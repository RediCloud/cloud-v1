package net.suqatri.cloud.api.player;

import net.suqatri.cloud.api.service.ICloudService;

public interface ICloudPlayer extends ICloudOfflinePlayer{

    ICloudService getServer();
    ICloudService getProxy();

    default long getSessionTime(){
        return System.currentTimeMillis() - getLastLogin();
    }

    void sendMessage(String message);
    void sendTitle(String title, String subTitle, int fadeIn, int stay, int fadeOut);
    void sendActionbar(String message);
    void sendTab(String header, String footer);

    void connect(String server);
    void connect(ICloudService cloudService);
    void disconnect(String reason);

}
