package dev.redicloud.api.bungeecord;

import dev.redicloud.api.impl.CloudDefaultAPIImpl;
import dev.redicloud.api.impl.service.CloudService;
import dev.redicloud.api.player.ICloudPlayer;
import dev.redicloud.api.player.IPlayerBridge;
import dev.redicloud.api.utils.ApplicationType;
import dev.redicloud.api.bungeecord.player.ProxyPlayerBridge;

public abstract class ProxyDefaultCloudAPI extends CloudDefaultAPIImpl<CloudService> {

    public ProxyDefaultCloudAPI() {
        super(ApplicationType.SERVICE_PROXY);
    }

    @Override
    public IPlayerBridge createBridge(ICloudPlayer player) {
        return new ProxyPlayerBridge(player);
    }
}
