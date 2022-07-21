package net.suqatri.cloud.plugin.minecraft.listener;

import net.suqatri.cloud.plugin.minecraft.MinecraftCloudAPI;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        MinecraftCloudAPI.getInstance().getService().setOnlineCount(Bukkit.getOnlinePlayers().size()-1);
        MinecraftCloudAPI.getInstance().getService().updateAsync();
    }

}
