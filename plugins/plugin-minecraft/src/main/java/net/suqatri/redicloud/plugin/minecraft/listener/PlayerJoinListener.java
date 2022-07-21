package net.suqatri.redicloud.plugin.minecraft.listener;

import net.suqatri.redicloud.plugin.minecraft.MinecraftCloudAPI;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        MinecraftCloudAPI.getInstance().getService().setOnlineCount(Bukkit.getOnlinePlayers().size());
        MinecraftCloudAPI.getInstance().getService().updateAsync();
    }

}
