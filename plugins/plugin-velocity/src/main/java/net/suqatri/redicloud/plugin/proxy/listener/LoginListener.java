package net.suqatri.redicloud.plugin.proxy.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import net.kyori.adventure.text.Component;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.player.CloudPlayer;
import net.suqatri.redicloud.plugin.proxy.ProxyCloudAPI;

import java.util.UUID;

public class LoginListener {

    @Subscribe
    public void onLogin(LoginEvent event) {

        UUID uniqueId = event.getPlayer().getUniqueId();

        if (!CloudAPI.getInstance().getPlayerManager().existsPlayer(uniqueId)) {
            CloudPlayer cloudPlayer = new CloudPlayer();
            cloudPlayer.setName(event.getPlayer().getUsername());
            cloudPlayer.setFirstLogin(System.currentTimeMillis());
            cloudPlayer.setConnected(true);
            cloudPlayer.setLastLogin(System.currentTimeMillis());
            cloudPlayer.setUniqueId(uniqueId);
            cloudPlayer.setLastIp(event.getPlayer().getRemoteAddress().getHostString());
            cloudPlayer.setLastConnectedProxyId(ProxyCloudAPI.getInstance().getService().getUniqueId());
            CloudAPI.getInstance().getPlayerManager().createPlayer(cloudPlayer);
        }
    }
}

