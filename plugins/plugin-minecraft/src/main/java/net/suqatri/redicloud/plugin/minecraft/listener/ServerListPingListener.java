package net.suqatri.redicloud.plugin.minecraft.listener;

import net.suqatri.redicloud.plugin.minecraft.MinecraftCloudAPI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

public class ServerListPingListener implements Listener {

    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {
        event.setMotd(MinecraftCloudAPI.getInstance().getService().getMotd());
        event.setMaxPlayers(MinecraftCloudAPI.getInstance().getService().getMaxPlayers());

    }

}
