package dev.redicloud.plugin.minecraft.listener;

import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.player.ICloudPlayer;
import dev.redicloud.api.service.ICloudService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;


public class AsyncPlayerPreLoginListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event){
        ICloudPlayer player = CloudAPI.getInstance().getPlayerManager().getPlayer(event.getUniqueId());
        ICloudService cloudService = CloudAPI.getInstance().getServiceManager().getService(player.getLastConnectedProxyId());
        if(!event.getAddress().getHostAddress().equals(cloudService.getHostName())){
            event.setKickMessage("You are not allowed to connect to this service!");
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            return;
        }
    }

}
