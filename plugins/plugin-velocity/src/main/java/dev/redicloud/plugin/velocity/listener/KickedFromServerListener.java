package dev.redicloud.plugin.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.player.ICloudPlayer;
import dev.redicloud.api.service.ICloudService;
import dev.redicloud.plugin.velocity.VelocityCloudAPI;
import dev.redicloud.api.velocity.utils.LegacyMessageUtils;

import java.util.Optional;

public class KickedFromServerListener {

    @Subscribe
    public void onServerKick(KickedFromServerEvent event) {

        if(event.getServerKickReason().isPresent()){
            if(event.getServerKickReason().get().contains(LegacyMessageUtils.component("You logged in from another location"))){
                event.setResult(KickedFromServerEvent.Notify.create(LegacyMessageUtils.component("Someone else tried to login from another location")));
                return;
            }
        }

        if(!event.getPlayer().isActive()) return;

        if(!VelocityCloudAPI.getInstance().getPlayerManager().isCached(event.getPlayer().getUniqueId().toString())){
            ICloudService verifyService = CloudAPI.getInstance().getPlayerManager().getVerifyService();
            if(verifyService == null){
                event.setResult(KickedFromServerEvent.DisconnectPlayer.create(LegacyMessageUtils.component("Error while connecting to server because verify service is not available")));
                return;
            }
            Optional<RegisteredServer> serverInfo = VelocityCloudAPI.getInstance().getProxyServer().getServer(verifyService.getServiceName());
            if(!serverInfo.isPresent()){
                event.setResult(KickedFromServerEvent.DisconnectPlayer.create(LegacyMessageUtils.component("Error while connecting to server because verify service is not available")));
                CloudAPI.getInstance().getConsole().warn("Service " + verifyService.getServiceName() + " is not registered");
                return;
            }
            event.setResult(KickedFromServerEvent.RedirectPlayer.create(serverInfo.get()));
            return;
        }

        CloudAPI.getInstance().getConsole().trace("Player " + event.getPlayer().getUsername() + " kicked from server " + event.getServer().getServerInfo().getName());
        ICloudPlayer player = CloudAPI.getInstance().getPlayerManager().getPlayer(event.getPlayer().getUniqueId());
        ICloudService kickedFrom = CloudAPI.getInstance().getServiceManager()
                .getService(event.getServer().getServerInfo().getName());
        ICloudService fallbackHolder = CloudAPI.getInstance().getServiceManager().getFallbackService(player, kickedFrom);
        if (fallbackHolder == null) {
            event.setResult(KickedFromServerEvent.DisconnectPlayer.create(LegacyMessageUtils.component("Fallback service is not available.")));
            return;
        }
        if(event.getServerKickReason().isPresent()) event.getPlayer().sendMessage(event.getServerKickReason().get());
        Optional<RegisteredServer> registeredServer = VelocityCloudAPI.getInstance().getProxyServer().getServer(fallbackHolder.getServiceName());
        if(!registeredServer.isPresent()) {
            event.setResult(KickedFromServerEvent.DisconnectPlayer.create(LegacyMessageUtils.component("Fallback service is not available.")));
            return;
        }
        event.setResult(KickedFromServerEvent.RedirectPlayer.create(registeredServer.get()));
    }

}
