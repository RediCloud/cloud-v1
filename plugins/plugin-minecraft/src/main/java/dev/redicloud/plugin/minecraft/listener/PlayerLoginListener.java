package dev.redicloud.plugin.minecraft.listener;

import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.player.ICloudPlayer;
import dev.redicloud.api.service.ICloudService;
import dev.redicloud.plugin.minecraft.MinecraftCloudAPI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;

public class PlayerLoginListener implements Listener {

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        try {
            ICloudPlayer player = CloudAPI.getInstance().getPlayerManager().getPlayer(event.getPlayer().getUniqueId());
            if(player == null){
                event.setKickMessage("Player not found");
                event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
                return;
            }
            ICloudService cloudService = CloudAPI.getInstance().getServiceManager().getService(player.getLastConnectedProxyId());
            CloudAPI.getInstance().getConsole().debug("Player " + player.getName() + " is connecting to via " + event.getAddress().getHostAddress() + " from proxy " + cloudService.getHostName());
            if(!event.getRealAddress().getHostAddress().equals(cloudService.getHostName())){
                event.setKickMessage("You are not allowed to connect to this service!");
                event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
                return;
            }
        }catch (Exception e){
            e.printStackTrace();
            event.setKickMessage("Player not found");
            event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
            return;
        }
        if(!event.getPlayer().hasPermission("redicloud.maintenance.bypass") && MinecraftCloudAPI.getInstance().getService().isMaintenance()) {
            event.setResult(PlayerLoginEvent.Result.KICK_WHITELIST);
            event.setKickMessage("This service is currently under maintenance. Please try again later.");
        }
    }

}
