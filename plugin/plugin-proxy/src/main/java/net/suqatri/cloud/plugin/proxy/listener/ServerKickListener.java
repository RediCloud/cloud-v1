package net.suqatri.cloud.plugin.proxy.listener;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.service.ICloudService;

public class ServerKickListener implements Listener {

    @EventHandler
    public void onServerKick(ServerKickEvent event){
        IRBucketHolder<ICloudService> fallbackHolder = CloudAPI.getInstance().getServiceManager().getFallbackService();
        if(fallbackHolder == null){
            event.getPlayer().disconnect("Fallback service is not available.");
            return;
        }
        event.getPlayer().sendMessage(event.getKickReasonComponent());
        event.setCancelServer(ProxyServer.getInstance().getServerInfo(fallbackHolder.get().getServiceName()));
        event.setCancelled(true);
    }

}
