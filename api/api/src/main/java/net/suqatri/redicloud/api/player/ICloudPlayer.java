package net.suqatri.redicloud.api.player;

import net.suqatri.redicloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.redicloud.api.redis.bucket.IRBucketObject;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.commons.function.future.FutureAction;

import java.util.UUID;

public interface ICloudPlayer extends IRBucketObject{


    UUID getUniqueId();
    String getName();
    long getFirstLogin();
    long getLastLogin();
    long getLastLogout();
    String getLastIp();
    UUID getLastConnectedServerId();
    UUID getLastConnectedProxyId();
    boolean isConnected();

    FutureAction<IRBucketHolder<ICloudService>> getServer();
    FutureAction<IRBucketHolder<ICloudService>> getProxy();

    default long getSessionTime(){
        return System.currentTimeMillis() - getLastLogin();
    }

    void sendMessage(String message);
    void sendTitle(String title, String subTitle, int fadeIn, int stay, int fadeOut);
    void sendActionbar(String message);
    void sendTab(String header, String footer);

    void connect(String server);
    void connect(IRBucketHolder<ICloudService> cloudService);
    void disconnect(String reason);

}
