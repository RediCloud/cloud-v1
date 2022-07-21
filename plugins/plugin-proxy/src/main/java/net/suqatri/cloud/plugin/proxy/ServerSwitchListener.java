package net.suqatri.cloud.plugin.proxy;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.impl.player.CloudPlayer;

public class ServerSwitchListener implements Listener {

    @EventHandler
    public void onServerSwitch(ServerSwitchEvent event){
        ProxiedPlayer proxiedPlayer = event.getPlayer();
        ServerInfo serverInfo = proxiedPlayer.getServer().getInfo();
        CloudAPI.getInstance().getPlayerManager().getPlayerAsync(proxiedPlayer.getUniqueId())
            .onFailure(throwable -> proxiedPlayer.disconnect(throwable.getMessage()))
            .onSuccess(playerHolder -> {
                CloudAPI.getInstance().getServiceManager().getServiceAsync(serverInfo.getName())
                        .onFailure(throwable -> proxiedPlayer.disconnect(throwable.getMessage()))
                    .onSuccess(serviceHolder -> {
                        if(!event.getPlayer().getServer().getInfo().getName().equals(serverInfo.getName())) return;
                        playerHolder.getImpl(CloudPlayer.class).setLastConnectedServerId(serviceHolder.get().getUniqueId());
                        playerHolder.get().updateAsync();
                    });
            });
    }

}
