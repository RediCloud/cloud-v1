package net.suqatri.redicloud.plugin.bungeecord.listener;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.player.CloudPlayer;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.plugin.bungeecord.BungeeCordCloudAPI;

public class ServerSwitchListener implements Listener {

    @EventHandler
    public void onServerSwitch(ServerSwitchEvent event) {
        ProxiedPlayer proxiedPlayer = event.getPlayer();
        ServerInfo serverInfo = proxiedPlayer.getServer().getInfo();
        CloudAPI.getInstance().getConsole().trace("Player " + proxiedPlayer.getName() + " switched to server " + serverInfo.getName());
        if(!BungeeCordCloudAPI.getInstance().getPlayerManager().isCached(event.getPlayer().getUniqueId().toString())) {
            if(!event.getPlayer().getServer().getInfo().getName().startsWith("Verify-")){
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
            return;
        }
        CloudAPI.getInstance().getPlayerManager().getPlayerAsync(proxiedPlayer.getUniqueId())
                .onFailure(throwable -> {
                    proxiedPlayer.disconnect(throwable.getMessage());
                    CloudAPI.getInstance().getConsole().error("Failed to get player " + proxiedPlayer.getName(), throwable);
                })
                .onSuccess(player -> {
                    CloudAPI.getInstance().getServiceManager().getServiceAsync(serverInfo.getName())
                            .onFailure(throwable -> {
                                proxiedPlayer.disconnect(throwable.getMessage());
                                CloudAPI.getInstance().getConsole().error("Failed to get service " + serverInfo.getName(), throwable);
                            })
                            .onSuccess(serviceHolder -> {
                                if (!event.getPlayer().getServer().getInfo().getName().equals(serverInfo.getName()))
                                    return;
                                ((CloudPlayer)player).setLastConnectedServerId(serviceHolder.getUniqueId());
                                player.updateAsync();
                            });
                });
    }

}
