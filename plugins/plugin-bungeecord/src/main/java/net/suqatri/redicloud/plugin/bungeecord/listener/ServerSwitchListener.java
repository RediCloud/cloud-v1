package net.suqatri.redicloud.plugin.bungeecord.listener;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.player.CloudPlayer;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.plugin.bungeecord.BungeeCordCloudAPI;

public class ServerSwitchListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerSwitch(ServerSwitchEvent event) {
        ProxiedPlayer proxiedPlayer = event.getPlayer();
        ServerInfo serverInfo = proxiedPlayer.getServer().getInfo();

        CloudAPI.getInstance().getConsole().trace("Player " + proxiedPlayer.getName() + " switched from " + (event.getFrom() == null ? "Unknown" : event.getFrom().getName()) + " to server " + serverInfo.getName());

        if(!BungeeCordCloudAPI.getInstance().getPlayerManager().isCached(event.getPlayer().getUniqueId().toString())) {
            if(event.getPlayer().getPendingConnection().isOnlineMode()){
                event.getPlayer().disconnect("Error while connecting to server because cloud player is not cached");
                return;
            }
            if(serverInfo.getName().startsWith("Verify-")) return;
            ICloudService cloudService = CloudAPI.getInstance().getPlayerManager().getVerifyService();
            if(cloudService == null){
                event.getPlayer().disconnect("Error while connecting to server because verify service is not available");
                return;
            }
            ServerInfo verifyServer = ProxyServer.getInstance().getServerInfo(cloudService.getServiceName());
            if(verifyServer == null){
                event.getPlayer().disconnect("Error while connecting to server because verify service is not available");
                return;
            }
            event.getPlayer().connect(verifyServer);
            return;
        }

        CloudPlayer cloudPlayer = (CloudPlayer) CloudAPI.getInstance().getPlayerManager().getPlayer(event.getPlayer().getUniqueId());
        if(!cloudPlayer.isLoggedIn()){
            if(serverInfo.getName().startsWith("Verify-")) return;
            ICloudService cloudService = CloudAPI.getInstance().getPlayerManager().getVerifyService();
            if(cloudService == null){
                event.getPlayer().disconnect("Error while connecting to server because verify service is not available");
                return;
            }
            ServerInfo verifyServer = ProxyServer.getInstance().getServerInfo(cloudService.getServiceName());
            if(verifyServer == null){
                event.getPlayer().disconnect("Error while connecting to server because verify service is not available");
                return;
            }
            event.getPlayer().connect(verifyServer);
            return;
        }
        ICloudService cloudService = CloudAPI.getInstance().getServiceManager().getService(serverInfo.getName());
        if (!event.getPlayer().getServer().getInfo().getName().equals(serverInfo.getName())) return;
        cloudPlayer.setLastConnectedServerId(cloudService.getUniqueId());
        cloudPlayer.updateAsync();
    }

}
