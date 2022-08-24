package dev.redicloud.plugin.bungeecord.listener;

import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import dev.redicloud.api.impl.player.CloudPlayer;
import dev.redicloud.plugin.bungeecord.BungeeCordCloudAPI;

public class PostLoginListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPostLogin(PostLoginEvent event){
        if(BungeeCordCloudAPI.getInstance().getService().isMaintenance() && !event.getPlayer().hasPermission("redicloud.maintenance.bypass")){
            event.getPlayer().disconnect("§cThis proxy is currently under maintenance. Please try again later.");
            return;
        }


        boolean isLoggedIn = false;

        if(BungeeCordCloudAPI.getInstance().getPlayerManager().existsPlayer(event.getPlayer().getUniqueId())){
            CloudPlayer cloudPlayer = (CloudPlayer) BungeeCordCloudAPI.getInstance().getPlayerManager().getPlayer(event.getPlayer().getUniqueId());
            isLoggedIn = cloudPlayer.isLoggedIn();
            cloudPlayer.setLastLogin(System.currentTimeMillis());
            cloudPlayer.setConnected(true);
            cloudPlayer.setLastIp(event.getPlayer().getPendingConnection().getAddress().getHostString());
            cloudPlayer.setLastConnectedProxyId(BungeeCordCloudAPI.getInstance().getService().getUniqueId());
            cloudPlayer.updateAsync();
            if(isLoggedIn){
                BungeeCordCloudAPI.getInstance().getPlayerManager().getConnectedList().addAsync(cloudPlayer.getUniqueId().toString());
            }
        }

        if(event.getPlayer().getPendingConnection().isOnlineMode()) return;

        if(isLoggedIn){
            event.getPlayer().sendMessage("You are logged in as " + event.getPlayer().getName() + "!");
        }
    }

}
