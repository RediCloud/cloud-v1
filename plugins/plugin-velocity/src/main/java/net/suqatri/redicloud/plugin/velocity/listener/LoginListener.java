package net.suqatri.redicloud.plugin.velocity.listener;

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.player.CloudPlayer;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.api.velocity.utils.LegacyMessageUtils;
import net.suqatri.redicloud.plugin.velocity.VelocityCloudAPI;

import java.util.Optional;
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
            cloudPlayer.setLastConnectedProxyId(VelocityCloudAPI.getInstance().getService().getUniqueId());
            CloudAPI.getInstance().getPlayerManager().createPlayer(cloudPlayer);
        }else{
            CloudAPI.getInstance().getConsole().trace("Player already exists!");
            CloudPlayer cloudPlayer = (CloudPlayer) CloudAPI.getInstance().getPlayerManager().getPlayer(uniqueId);
            cloudPlayer.setLastLogin(System.currentTimeMillis());
            cloudPlayer.setConnected(true);
            cloudPlayer.setLastIp(event.getPlayer().getRemoteAddress().getHostString());
            cloudPlayer.setLastConnectedProxyId(VelocityCloudAPI.getInstance().getService().getUniqueId());
            String oldName = cloudPlayer.getName();
            if(!oldName.equals(event.getPlayer().getUsername())){
                cloudPlayer.setName(event.getPlayer().getUsername());
                CloudAPI.getInstance().getPlayerManager().updateName(cloudPlayer.getUniqueId(), event.getPlayer().getUsername(), oldName);
            }
            cloudPlayer.updateAsync();
            CloudAPI.getInstance().getConsole().trace("Player updated!");
        }
        if(VelocityCloudAPI.getInstance().getService().isMaintenance() && !event.getPlayer().hasPermission("redicloud.maintenance.bypass")){
            event.setResult(ResultedEvent.ComponentResult.denied(LegacyMessageUtils.component("§cThis proxy is currently under maintenance. Please try again later.")));
            return;
        }

        ICloudService holder = CloudAPI.getInstance().getServiceManager().getFallbackService(event.getPlayer().hasPermission("redicloud.maintenance.bypass"));
        if (holder == null) {
            event.getPlayer().disconnect(LegacyMessageUtils.component("Fallback service is not available."));
            return;
        }
    }
}

