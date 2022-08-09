package net.suqatri.redicloud.plugin.bungeecord.listener;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.plugin.bungeecord.BungeeCordCloudAPI;

public class ServerConnectListener implements Listener {

    @EventHandler
    public void onServerConnect(ServerConnectEvent event) {
        if (event.isCancelled()) return;

        if(!BungeeCordCloudAPI.getInstance().getPlayerManager().isCached(event.getPlayer().getUniqueId().toString())){
            if(!event.getTarget().getName().startsWith("Verify")){
                event.setCancelled(true);
                event.getPlayer().sendMessage("§cYou are not verified yet. Please verify first.");
                return;
            }
        }

        ServerInfo serverInfo =
                (event.getTarget().getName().equalsIgnoreCase("fallback")
                        || event.getTarget().getName().equalsIgnoreCase("lobby"))
                        ? null
                        : event.getTarget();

        if (serverInfo == null) {
            ICloudService holder = CloudAPI.getInstance().getServiceManager().getFallbackService(event.getPlayer().hasPermission("redicloud.maintenance.bypass"));
            if (holder == null) {
                event.getPlayer().disconnect("Fallback service is not available.");
                event.setCancelled(true);
                return;
            }
            if(holder.isMaintenance() && !event.getPlayer().hasPermission("redicloud.service.bypass.maintenance")) {
                event.getPlayer().sendMessage("This service is currently under maintenance. Please try again later.");
                event.setCancelled(true);
                return;
            }
            serverInfo = ProxyServer.getInstance().getServerInfo(holder.getServiceName());
        }

        event.setTarget(serverInfo);
    }

}
