package net.suqatri.cloud.plugin.minecraft.listener;

import net.suqatri.cloud.plugin.minecraft.MinecraftCloudAPI;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerKickListener implements Listener {

    @EventHandler
    public void onKick(PlayerQuitEvent event){
        MinecraftCloudAPI.getInstance().getService().setOnlineCount(Bukkit.getOnlinePlayers().size()-1);
        MinecraftCloudAPI.getInstance().getService().updateAsync();
    }

}
