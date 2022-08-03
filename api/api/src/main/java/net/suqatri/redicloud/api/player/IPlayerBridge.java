package net.suqatri.redicloud.api.player;

import net.suqatri.redicloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.redicloud.api.service.ICloudService;

import java.util.UUID;

public interface IPlayerBridge {

    void connect(IRBucketHolder<ICloudService> serviceHolder);

    void connect(UUID serviceId);

    void sendMessage(String message);

    void sendTitle(String title, String subTitle, int fadeIn, int stay, int fadeOut);

    void sendActionbar(String message);

    void sendTab(String header, String footer);

    void connect(String server);

    void disconnect(String reason);
}
