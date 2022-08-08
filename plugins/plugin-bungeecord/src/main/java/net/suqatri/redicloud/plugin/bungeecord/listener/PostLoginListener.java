package net.suqatri.redicloud.plugin.bungeecord.listener;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.suqatri.redicloud.api.impl.player.CloudPlayer;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.plugin.bungeecord.BungeeCordCloudAPI;

import java.util.concurrent.TimeUnit;

public class PostLoginListener implements Listener {

    @EventHandler
    public void onPostLogin(PostLoginEvent event){
        if(BungeeCordCloudAPI.getInstance().getService().isMaintenance() && !event.getPlayer().hasPermission("redicloud.maintenance.bypass")){
            event.getPlayer().disconnect("§cThis proxy is currently under maintenance. Please try again later.");
            return;
        }

        if(event.getPlayer().getPendingConnection().isOnlineMode()) return;

        CloudPlayer cloudPlayer = (CloudPlayer) BungeeCordCloudAPI.getInstance().getPlayerManager().getPlayer(event.getPlayer().getName());

        if(!cloudPlayer.isLoggedIn()){

            ICloudService cloudService = BungeeCordCloudAPI.getInstance().getPlayerManager().getVerifyService();
            ServerInfo serverInfo = ProxyServer.getInstance().getServerInfo(cloudService.getServiceName());
            if(serverInfo == null){
                event.getPlayer().disconnect("§cThe verify service is currently unavailable. Please try again later.");
                return;
            }
            event.getPlayer().connect(serverInfo);

        }else {
            event.getPlayer().sendMessage("You are logged in as " + cloudPlayer.getName() + "!");
        }
    }

}
