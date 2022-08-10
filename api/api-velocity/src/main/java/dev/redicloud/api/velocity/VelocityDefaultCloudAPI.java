package dev.redicloud.api.velocity;

import com.velocitypowered.api.proxy.ProxyServer;
import dev.redicloud.api.impl.CloudDefaultAPIImpl;
import dev.redicloud.api.impl.service.CloudService;
import dev.redicloud.api.player.ICloudPlayer;
import dev.redicloud.api.player.IPlayerBridge;
import dev.redicloud.api.utils.ApplicationType;
import dev.redicloud.api.velocity.player.VelocityPlayerBridge;
import lombok.Getter;

@Getter
public abstract class VelocityDefaultCloudAPI extends CloudDefaultAPIImpl<CloudService> {

    @Getter
    private static VelocityDefaultCloudAPI instance;

    private final ProxyServer proxyServer;

    public VelocityDefaultCloudAPI(ProxyServer proxyServer) {
        super(ApplicationType.SERVICE_PROXY);
        instance = this;
        this.proxyServer = proxyServer;
    }

    @Override
    public IPlayerBridge createBridge(ICloudPlayer player) {
        return new VelocityPlayerBridge(player);
    }
}
