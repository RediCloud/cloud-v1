package net.suqatri.redicloud.plugin.minecraft.listener;

import net.suqatri.redicloud.plugin.minecraft.MinecraftCloudAPI;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerKickListener implements Listener {

    @EventHandler
    public void onKick(PlayerQuitEvent event) {
        MinecraftCloudAPI.getInstance().setOnlineCount(Bukkit.getOnlinePlayers().size() - 1);
    }

}
