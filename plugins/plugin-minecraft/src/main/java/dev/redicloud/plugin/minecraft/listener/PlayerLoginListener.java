package dev.redicloud.plugin.minecraft.listener;

import dev.redicloud.plugin.minecraft.MinecraftCloudAPI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class PlayerLoginListener implements Listener {

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        if(!event.getPlayer().hasPermission("redicloud.maintenance.bypass") && MinecraftCloudAPI.getInstance().getService().isMaintenance()) {
            event.setResult(PlayerLoginEvent.Result.KICK_WHITELIST);
            event.setKickMessage("This service is currently under maintenance. Please try again later.");
        }
    }

}
