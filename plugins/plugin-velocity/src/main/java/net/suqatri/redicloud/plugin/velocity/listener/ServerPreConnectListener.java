package net.suqatri.redicloud.plugin.velocity.listener;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.player.ICloudPlayer;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.plugin.velocity.VelocityCloudAPI;
import net.suqatri.redicloud.api.velocity.utils.LegacyMessageUtils;

import java.util.Optional;

public class ServerPreConnectListener {

    @Subscribe(order = PostOrder.LAST)
    public void onServerConnect(ServerPreConnectEvent event) {
        RegisteredServer target = event.getOriginalServer();

        if(!VelocityCloudAPI.getInstance().getPlayerManager().isCached(event.getPlayer().getUniqueId().toString())){
            if(event.getPlayer().isOnlineMode()){
                event.setResult(ServerPreConnectEvent.ServerResult.denied());
                event.getPlayer().disconnect(LegacyMessageUtils.component("Error while connecting to server because cloud player is not cached"));
                return;
            }
            if(!event.getOriginalServer().getServerInfo().getName().startsWith("Verify-")){
                ICloudService cloudService = CloudAPI.getInstance().getPlayerManager().getVerifyService();
                if(cloudService == null){
                    event.setResult(ServerPreConnectEvent.ServerResult.denied());
                    event.getPlayer().disconnect(LegacyMessageUtils.component("Error while connecting to server because verify service is not available"));
                    return;
                }
                Optional<RegisteredServer> serverInfo = VelocityCloudAPI.getInstance().getProxyServer().getServer(cloudService.getServiceName());
                if(!serverInfo.isPresent()){
                    event.setResult(ServerPreConnectEvent.ServerResult.denied());
                    event.getPlayer().disconnect(LegacyMessageUtils.component("Error while connecting to server because verify service is not available"));
                    CloudAPI.getInstance().getConsole().warn("Service " + cloudService.getServiceName() + " is not registered");
                    return;
                }
                if(event.getOriginalServer().equals(serverInfo.get())) return;
                event.setResult(ServerPreConnectEvent.ServerResult.denied());
                event.getPlayer().createConnectionRequest(serverInfo.get()).connect();
                return;
            }
        }

        ICloudPlayer cloudPlayer = CloudAPI.getInstance().getPlayerManager().getPlayer(event.getPlayer().getUniqueId());
        if(!cloudPlayer.isLoggedIn()){
            ICloudService service = CloudAPI.getInstance().getPlayerManager().getVerifyService();
            if(service == null){
                event.setResult(ServerPreConnectEvent.ServerResult.denied());
                event.getPlayer().disconnect(LegacyMessageUtils.component("Error while connecting to server because verify service is not available"));
                return;
            }
            Optional<RegisteredServer> serverInfo = VelocityCloudAPI.getInstance().getProxyServer().getServer(service.getServiceName());
            if(!serverInfo.isPresent()){
                event.setResult(ServerPreConnectEvent.ServerResult.denied());
                event.getPlayer().disconnect(LegacyMessageUtils.component("Error while connecting to server because verify service is not available"));
                CloudAPI.getInstance().getConsole().warn("Service " + service.getServiceName() + " is not registered");
                return;
            }
            if(event.getOriginalServer().equals(serverInfo.get())) return;
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
            event.getPlayer().createConnectionRequest(serverInfo.get()).connect();
            return;
        }

        RegisteredServer serverInfo =
                (target.getServerInfo().getName().equalsIgnoreCase("fallback")
                        || target.getServerInfo().getName().equalsIgnoreCase("lobby")
                        || target.getServerInfo().getName().startsWith("Verify-"))
                        ? null
                        : target;

        if (serverInfo == null) {
            ICloudService holder = CloudAPI.getInstance().getServiceManager().getFallbackService(event.getPlayer().hasPermission("redicloud.maintenance.bypass"));
            if (holder == null) {
                event.setResult(ServerPreConnectEvent.ServerResult.denied());
                event.getPlayer().disconnect(LegacyMessageUtils.component("Fallback service is not available."));
                return;
            }
            serverInfo = VelocityCloudAPI.getInstance().getProxyServer().getServer(holder.getServiceName()).orElse(null);
            if(serverInfo == null){
                event.setResult(ServerPreConnectEvent.ServerResult.denied());
                event.getPlayer().disconnect(LegacyMessageUtils.component("Fallback service is not available."));
                CloudAPI.getInstance().getConsole().error("Fallback service is not registered: " + holder.getServiceName());
                return;
            }
        }else {
            ICloudService targetServiceHolder = CloudAPI.getInstance().getServiceManager()
                    .getService(serverInfo.getServerInfo().getName());
            if (targetServiceHolder.isMaintenance() && !event.getPlayer().hasPermission("redicloud.maintenance.bypass")) {
                event.setResult(ServerPreConnectEvent.ServerResult.denied());
                if(!event.getPlayer().getCurrentServer().isPresent()){
                    event.getPlayer().disconnect(LegacyMessageUtils.component("Service is in maintenance mode."));
                }else {
                    event.getPlayer().sendMessage(LegacyMessageUtils.component("Service is in maintenance mode."));
                }
                return;
            }
        }

        event.setResult(ServerPreConnectEvent.ServerResult.allowed(serverInfo));
    }

}
