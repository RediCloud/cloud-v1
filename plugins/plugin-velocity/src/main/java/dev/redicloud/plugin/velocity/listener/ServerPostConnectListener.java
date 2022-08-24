package dev.redicloud.plugin.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.impl.player.CloudPlayer;
import dev.redicloud.api.service.ICloudService;
import dev.redicloud.plugin.velocity.VelocityCloudAPI;

import java.util.Optional;

public class ServerPostConnectListener {

    @Subscribe
    public void onServerConnect(ServerPostConnectEvent event){
        Optional<ServerConnection > connection = event.getPlayer().getCurrentServer();
        if(!connection.isPresent()) return;
        RegisteredServer registeredServer = connection.get().getServer();
        if(!VelocityCloudAPI.getInstance().getPlayerManager().isCached(event.getPlayer().getUniqueId().toString())) return;
        CloudPlayer player = (CloudPlayer) CloudAPI.getInstance().getPlayerManager().getPlayer(event.getPlayer().getUniqueId());
        CloudAPI.getInstance().getConsole().trace("Player " + player.getName() + " switched to server " + registeredServer.getServerInfo().getName());
        ICloudService serviceHolder = CloudAPI.getInstance().getServiceManager()
                .getService(registeredServer.getServerInfo().getName());
        player.setLastConnectedServerId(serviceHolder.getUniqueId());
        player.updateAsync();
        CloudAPI.getInstance().getConsole().trace("Player updated after server connect!");
    }

}
