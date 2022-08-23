package dev.redicloud.plugin.velocity.listener;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.impl.player.CloudPlayer;
import dev.redicloud.api.velocity.utils.LegacyMessageUtils;
import dev.redicloud.commons.UUIDUtility;
import dev.redicloud.plugin.velocity.VelocityCloudAPI;

import java.util.Random;
import java.util.UUID;

public class LoginListener {

    @Subscribe(order = PostOrder.FIRST)
    public void onLogin(LoginEvent event) {

        UUID uniqueId = event.getPlayer().getUniqueId();
        String name = event.getPlayer().getUsername();

        if(!event.getPlayer().isOnlineMode()){
            if(UUIDUtility.isPremium(uniqueId, name)){
                event.getPlayer().disconnect(LegacyMessageUtils.component("You are a premium player. Please reconnect."));
                return;
            }
            return;
        }

        try {
            if (!CloudAPI.getInstance().getPlayerManager().existsPlayer(uniqueId)) {
                CloudPlayer cloudPlayer = new CloudPlayer();
                cloudPlayer.setName(event.getPlayer().getUsername());
                cloudPlayer.setFirstLogin(System.currentTimeMillis());
                cloudPlayer.setConnected(true);
                cloudPlayer.setLastLogin(System.currentTimeMillis());
                cloudPlayer.setUniqueId(uniqueId);
                cloudPlayer.setLastIp(event.getPlayer().getRemoteAddress().getHostString());
                cloudPlayer.setLastConnectedProxyId(VelocityCloudAPI.getInstance().getService().getUniqueId());
                cloudPlayer.setCracked(!event.getPlayer().isOnlineMode());
                cloudPlayer.setPasswordLogRounds(10 + new Random().nextInt(30 - 10 + 1));
                CloudAPI.getInstance().getPlayerManager().createPlayer(cloudPlayer);
            } else {
                CloudPlayer cloudPlayer = (CloudPlayer) CloudAPI.getInstance().getPlayerManager().getPlayer(uniqueId);
                cloudPlayer.setLastLogin(System.currentTimeMillis());
                cloudPlayer.setConnected(true);
                cloudPlayer.setLastIp(event.getPlayer().getRemoteAddress().getHostString());
                cloudPlayer.setLastConnectedProxyId(VelocityCloudAPI.getInstance().getService().getUniqueId());
                cloudPlayer.setCracked(!event.getPlayer().isOnlineMode());
                String oldName = cloudPlayer.getName();
                if (!oldName.equals(event.getPlayer().getUsername())) {
                    cloudPlayer.setName(event.getPlayer().getUsername());
                    CloudAPI.getInstance().getPlayerManager().updateName(cloudPlayer.getUniqueId(), event.getPlayer().getUsername(), oldName);
                }
                cloudPlayer.updateAsync();
                CloudAPI.getInstance().getConsole().trace("Player updated!");
            }
            if (VelocityCloudAPI.getInstance().getService().isMaintenance() && !event.getPlayer().hasPermission("redicloud.maintenance.bypass")) {
                CloudAPI.getInstance().getConsole().trace("Maintenance is enabled and player dont have permission redicloud.maintenance.bypass");
                event.setResult(ResultedEvent.ComponentResult
                        .denied(LegacyMessageUtils.component(
                        "§cThis proxy is currently under maintenance. Please try again later.")));
                event.getPlayer().disconnect(LegacyMessageUtils.component(
                        "§cThis proxy is currently under maintenance. Please try again later."));
                return;
            }
        }catch (Exception e){
            CloudAPI.getInstance().getConsole().error("Error while handling login event!", e);
            event.setResult(ResultedEvent.ComponentResult.denied(
                    LegacyMessageUtils.component(
                            "§cAn error occurred while handling your login. Please try again later.")));
            event.getPlayer().disconnect(LegacyMessageUtils.component(
                    "§cAn error occurred while handling your login. Please try again later."));
        }
    }
}

