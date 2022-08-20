package dev.redicloud.api.player;

import dev.redicloud.api.redis.bucket.IRBucketObject;
import dev.redicloud.api.redis.bucket.fetch.IRBucketFetchAble;
import dev.redicloud.api.service.ICloudService;
import dev.redicloud.commons.function.future.FutureAction;

import java.util.UUID;

public interface ICloudPlayer extends IRBucketFetchAble {


    UUID getUniqueId();

    String getName();

    long getFirstLogin();

    long getLastLogin();

    long getLastLogout();

    boolean isLoggedIn();

    String getLastIp();

    boolean isCracked();

    UUID getLastConnectedServerId();

    UUID getLastConnectedProxyId();

    boolean isConnected();

    FutureAction<ICloudService> getServer();

    FutureAction<ICloudService> getProxy();

    default long getSessionTime() {
        return System.currentTimeMillis() - getLastLogin();
    }

    IPlayerBridge getBridge();

}
