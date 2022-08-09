package net.suqatri.redicloud.plugin.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.player.CloudPlayer;
import net.suqatri.redicloud.api.player.ICloudPlayer;
import net.suqatri.redicloud.api.service.ICloudService;

import java.util.Optional;

public class ServerPostConnectListener {

    @Subscribe
    public void onServerConnect(ServerPostConnectEvent event){
        Optional<ServerConnection > connection = event.getPlayer().getCurrentServer();
        if(!connection.isPresent()) return;
        RegisteredServer registeredServer = connection.get().getServer();
        CloudPlayer player = (CloudPlayer) CloudAPI.getInstance().getPlayerManager().getPlayer(event.getPlayer().getUniqueId());
        CloudAPI.getInstance().getConsole().trace("Player: " + player.getName() + " connected to: " + registeredServer.getServerInfo().getName());
        if(player == null) return;
        CloudAPI.getInstance().getConsole().trace("Player " + player.getName() + " switched to server " + registeredServer.getServerInfo().getName());
        ICloudService serviceHolder = CloudAPI.getInstance().getServiceManager()
                .getService(registeredServer.getServerInfo().getName());
        player.setLastConnectedServerId(serviceHolder.getUniqueId());
        player.updateAsync();
        CloudAPI.getInstance().getConsole().trace("Player updated after server connect!");
    }

}
