package net.suqatri.redicloud.plugin.minecraft.listener;

import net.suqatri.redicloud.plugin.minecraft.MinecraftCloudAPI;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

public class PlayerLoginListener implements Listener {

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        if(!event.getPlayer().hasPermission("redicloud.maintenance.bypass") && MinecraftCloudAPI.getInstance().getService().isInMaintenance()) {
            event.setResult(PlayerLoginEvent.Result.KICK_WHITELIST);
            event.setKickMessage("This service is currently under maintenance. Please try again later.");
        }
    }

}
