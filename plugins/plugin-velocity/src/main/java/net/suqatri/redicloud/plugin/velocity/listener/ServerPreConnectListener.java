package net.suqatri.redicloud.plugin.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.plugin.velocity.VelocityCloudAPI;
import net.suqatri.redicloud.api.velocity.utils.LegacyMessageUtils;

import java.util.Optional;

public class ServerPreConnectListener {

    @Subscribe
    public void onServerConnect(ServerPreConnectEvent event) {

        if(!event.getPlayer().getCurrentServer().isPresent()) {
            IRBucketHolder<ICloudService> holder = CloudAPI.getInstance().getServiceManager().getFallbackService();
            if (holder == null) {
                event.setResult(ServerPreConnectEvent.ServerResult.denied());
                event.getPlayer().disconnect(LegacyMessageUtils.component("Fallback service is not available."));
                return;
            }
            Optional<RegisteredServer> registeredServer = VelocityCloudAPI.getInstance().getProxyServer()
                    .getServer(holder.get().getServiceName());
            if(!registeredServer.isPresent()){
                event.setResult(ServerPreConnectEvent.ServerResult.denied());
                event.getPlayer().disconnect(LegacyMessageUtils.component("Fallback service is not available."));
                return;
            }
            event.setResult(ServerPreConnectEvent.ServerResult.allowed(registeredServer.get()));
            return;
        }

        RegisteredServer target = event.getOriginalServer();

        RegisteredServer serverInfo =
                (target.getServerInfo().getName().equalsIgnoreCase("fallback")
                        || target.getServerInfo().getName().equalsIgnoreCase("lobby"))
                        ? null
                        : target;

        if (serverInfo == null) {
            IRBucketHolder<ICloudService> holder = CloudAPI.getInstance().getServiceManager().getFallbackService();
            if (holder == null) {
                event.setResult(ServerPreConnectEvent.ServerResult.denied());
                event.getPlayer().disconnect(LegacyMessageUtils.component("Fallback service is not available."));
                return;
            }
            serverInfo = VelocityCloudAPI.getInstance().getProxyServer().getServer(holder.get().getServiceName()).orElse(null);
            if(serverInfo == null){
                event.setResult(ServerPreConnectEvent.ServerResult.denied());
                event.getPlayer().disconnect(LegacyMessageUtils.component("Fallback service is not available."));
                return;
            }
        }

        event.setResult(ServerPreConnectEvent.ServerResult.allowed(serverInfo));
    }

}
