package net.suqatri.cloud.plugin.minecraft.listener;

import net.suqatri.cloud.plugin.minecraft.MinecraftCloudAPI;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

public class ServerListPingListener implements Listener {

    @EventHandler
    public void onServerListPing(ServerListPingEvent event){
        event.setMotd(MinecraftCloudAPI.getInstance().getService().getMotd());
        event.setMaxPlayers(MinecraftCloudAPI.getInstance().getService().getMaxPlayers());

    }

}
