package net.suqatri.redicloud.plugin.proxy.listener;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.redicloud.api.service.ICloudService;

public class ServerConnectListener implements Listener {

    @EventHandler
    public void onServerConnect(ServerConnectEvent event) {
        if (event.isCancelled()) return;

        ServerInfo serverInfo =
                (event.getTarget().getName().equalsIgnoreCase("fallback")
                        || event.getTarget().getName().equalsIgnoreCase("lobby"))
                        ? null
                        : event.getTarget();

        if (serverInfo == null) {
            IRBucketHolder<ICloudService> holder = CloudAPI.getInstance().getServiceManager().getFallbackService(event.getPlayer().hasPermission("redicloud.maintenance.bypass"));
            if (holder == null) {
                event.getPlayer().disconnect("Fallback service is not available.");
                event.setCancelled(true);
                return;
            }
            if(holder.get().isMaintenance() && !event.getPlayer().hasPermission("redicloud.service.bypass.maintenance")) {
                event.getPlayer().sendMessage("This service is currently under maintenance. Please try again later.");
                event.setCancelled(true);
                return;
            }
            serverInfo = ProxyServer.getInstance().getServerInfo(holder.get().getServiceName());
        }

        event.setTarget(serverInfo);
    }

}
