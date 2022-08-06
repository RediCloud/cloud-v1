package net.suqatri.redicloud.api.velocity;

import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import net.suqatri.redicloud.api.impl.CloudDefaultAPIImpl;
import net.suqatri.redicloud.api.impl.service.CloudService;
import net.suqatri.redicloud.api.player.ICloudPlayer;
import net.suqatri.redicloud.api.player.IPlayerBridge;
import net.suqatri.redicloud.api.utils.ApplicationType;
import net.suqatri.redicloud.api.velocity.player.VelocityPlayerBridge;

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
