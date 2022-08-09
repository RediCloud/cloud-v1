package net.suqatri.redicloud.plugin.bungeecord.listener;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.player.ICloudPlayer;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.plugin.bungeecord.BungeeCordCloudAPI;

public class ServerConnectListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerConnect(ServerConnectEvent event) {
        if (event.isCancelled()) return;
        CloudAPI.getInstance().getConsole().trace("ServerConnectEvent: " + event.getTarget().getName());

        if(!BungeeCordCloudAPI.getInstance().getPlayerManager().isCached(event.getPlayer().getUniqueId().toString())){
            if(event.getPlayer().getPendingConnection().isOnlineMode()){
                event.setCancelled(true);
                event.getPlayer().disconnect("Error while connecting to server because cloud player is not cached");
                return;
            }
            if(!event.getTarget().getName().startsWith("Verify-")){
                ICloudService cloudService = CloudAPI.getInstance().getPlayerManager().getVerifyService();
                if(cloudService == null){
                    event.setCancelled(true);
                    event.getPlayer().disconnect("Error while connecting to server because verify service is not available");
                    return;
                }
                ServerInfo serverInfo = ProxyServer.getInstance().getServerInfo(cloudService.getServiceName());
                if(serverInfo == null){
                    event.setCancelled(true);
                    event.getPlayer().disconnect("Error while connecting to server because verify service is not available");
                    return;
                }
                if(event.getTarget().equals(serverInfo)) return;
                event.setTarget(serverInfo);
                return;
            }
        }

        ICloudPlayer cloudPlayer = CloudAPI.getInstance().getPlayerManager().getPlayer(event.getPlayer().getUniqueId());
        if(!cloudPlayer.isLoggedIn()){
            ICloudService cloudService = CloudAPI.getInstance().getPlayerManager().getVerifyService();
            if(cloudService == null){
                event.setCancelled(true);
                event.getPlayer().disconnect("Error while connecting to server because verify service is not available");
                return;
            }
            ServerInfo serverInfo = ProxyServer.getInstance().getServerInfo(cloudService.getServiceName());
            if(serverInfo == null){
                event.setCancelled(true);
                event.getPlayer().disconnect("Error while connecting to server because verify service is not available");
                return;
            }
            event.setTarget(serverInfo);
            return;
        }

        ServerInfo serverInfo =
                (event.getTarget().getName().equalsIgnoreCase("fallback")
                        || event.getTarget().getName().equalsIgnoreCase("lobby")
                        || event.getTarget().getName().startsWith("Verify-"))
                        ? null
                        : event.getTarget();

        if (serverInfo == null) {
            ICloudService service = CloudAPI.getInstance().getServiceManager().getFallbackService(event.getPlayer().hasPermission("redicloud.maintenance.bypass"));
            if (service == null) {
                event.getPlayer().disconnect("Fallback service is not available.");
                event.setCancelled(true);
                return;
            }
            if(service.isMaintenance() && !event.getPlayer().hasPermission("redicloud.service.bypass.maintenance")) {
                event.getPlayer().sendMessage("This service is currently under maintenance. Please try again later.");
                event.setCancelled(true);
                return;
            }
            serverInfo = ProxyServer.getInstance().getServerInfo(service.getServiceName());
        }else{
            ICloudService service = CloudAPI.getInstance().getServiceManager().getService(serverInfo.getName());
            if(service.isMaintenance() && !event.getPlayer().hasPermission("redicloud.maintenance.bypass")){
                if(event.getReason() == ServerConnectEvent.Reason.JOIN_PROXY) {
                    event.getPlayer().disconnect("This service is currently under maintenance. Please try again later.");
                }else{
                    event.getPlayer().sendMessage("This service is currently under maintenance. Please try again later.");
                }
                event.setCancelled(true);
                return;
            }
        }

        event.setTarget(serverInfo);
    }

}
