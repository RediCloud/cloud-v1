package net.suqatri.redicloud.plugin.bungeecord.listener;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.service.ICloudService;

public class ServerKickListener implements Listener {

    @EventHandler
    public void onServerKick(ServerKickEvent event) {
        ICloudService fallbackHolder = CloudAPI.getInstance().getServiceManager()
                .getFallbackService(CloudAPI.getInstance().getPlayerManager().getPlayer(event.getPlayer().getUniqueId()),
                        CloudAPI.getInstance().getServiceManager().getService(event.getKickedFrom().getName()));
        if (fallbackHolder == null) {
            event.getPlayer().disconnect("Fallback service is not available.");
            return;
        }
        event.getPlayer().sendMessage(event.getKickReasonComponent());
        event.setCancelled(true);
        event.setCancelServer(ProxyServer.getInstance().getServerInfo(fallbackHolder.getServiceName()));
    }

}
