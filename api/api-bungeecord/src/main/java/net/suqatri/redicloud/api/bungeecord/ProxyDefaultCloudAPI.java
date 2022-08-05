package net.suqatri.redicloud.api.bungeecord;

import net.suqatri.redicloud.api.impl.CloudDefaultAPIImpl;
import net.suqatri.redicloud.api.impl.service.CloudService;
import net.suqatri.redicloud.api.player.ICloudPlayer;
import net.suqatri.redicloud.api.player.IPlayerBridge;
import net.suqatri.redicloud.api.bungeecord.player.ProxyPlayerBridge;
import net.suqatri.redicloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.redicloud.api.utils.ApplicationType;

public abstract class ProxyDefaultCloudAPI extends CloudDefaultAPIImpl<CloudService> {

    public ProxyDefaultCloudAPI() {
        super(ApplicationType.SERVICE_PROXY);
    }

    @Override
    public IPlayerBridge createBridge(IRBucketHolder<ICloudPlayer> playerHolder) {
        return new ProxyPlayerBridge(playerHolder);
    }
}
