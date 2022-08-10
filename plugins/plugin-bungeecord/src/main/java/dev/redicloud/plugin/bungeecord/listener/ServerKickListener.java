package dev.redicloud.plugin.bungeecord.listener;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.service.ICloudService;
import dev.redicloud.plugin.bungeecord.BungeeCordCloudAPI;

public class ServerKickListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerKick(ServerKickEvent event) {
        if(event.getKickReason().equals("You logged in from another location")) {
            event.setCancelled(true);
            return;
        }

        if(!event.getPlayer().isConnected()) return;

        if(!BungeeCordCloudAPI.getInstance().getPlayerManager().isCached(event.getPlayer().getUniqueId().toString())){
            ICloudService verifyService = CloudAPI.getInstance().getPlayerManager().getVerifyService();
            if(verifyService.getServiceName().equals(event.getKickedFrom().getName())){
                event.getPlayer().disconnect("§cYou are not verified yet. Please verify first.");
                return;
            }
            ServerInfo serverInfo = ProxyServer.getInstance().getServerInfo(verifyService.getServiceName());
            if(serverInfo == null){
                event.getPlayer().disconnect("§cVerify service is not available.");
                return;
            }
            event.setCancelServer(serverInfo);
            return;
        }


        ICloudService fallbackHolder = CloudAPI.getInstance().getServiceManager()
                .getFallbackService(CloudAPI.getInstance().getPlayerManager().getPlayer(event.getPlayer().getUniqueId()),
                        CloudAPI.getInstance().getServiceManager().getService(event.getKickedFrom().getName()));
        if (fallbackHolder == null) {
            event.getPlayer().disconnect("Fallback service is not available.");
            return;
        }
        ServerInfo serverInfo = ProxyServer.getInstance().getServerInfo(fallbackHolder.getServiceName());
        if(serverInfo == null){
            event.getPlayer().disconnect("Fallback service is not available.");
            return;
        }
        event.getPlayer().sendMessage(event.getKickReasonComponent());
        event.setCancelled(true);
        event.setCancelServer(serverInfo);
    }

}
