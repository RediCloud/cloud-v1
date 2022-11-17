package dev.redicloud.api.bungeecord;

import dev.redicloud.api.impl.CloudDefaultAPIImpl;
import dev.redicloud.api.impl.service.CloudService;
import dev.redicloud.api.player.ICloudPlayer;
import dev.redicloud.api.player.IPlayerBridge;
import dev.redicloud.api.utils.ApplicationType;
import dev.redicloud.api.bungeecord.player.ProxyPlayerBridge;
import dev.redicloud.dependency.DependencyLoader;

public abstract class ProxyDefaultCloudAPI extends CloudDefaultAPIImpl<CloudService> {

    public ProxyDefaultCloudAPI(DependencyLoader dependencyLoader) {
        super(ApplicationType.SERVICE_PROXY, dependencyLoader);
    }

    @Override
    public IPlayerBridge createBridge(ICloudPlayer player) {
        return new ProxyPlayerBridge(player);
    }
}
