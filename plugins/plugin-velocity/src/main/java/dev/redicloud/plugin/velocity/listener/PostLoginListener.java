package dev.redicloud.plugin.velocity.listener;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.impl.player.CloudPlayer;
import dev.redicloud.api.velocity.utils.LegacyMessageUtils;
import dev.redicloud.plugin.velocity.VelocityCloudAPI;

public class PostLoginListener {

    @Subscribe(order = PostOrder.FIRST)
    public void onPostLogin(PostLoginEvent event){

        boolean isLoggedIn = false;
        if(VelocityCloudAPI.getInstance().getPlayerManager().existsPlayer(event.getPlayer().getUniqueId())){
            CloudPlayer cloudPlayer = (CloudPlayer) VelocityCloudAPI.getInstance().getPlayerManager().getPlayer(event.getPlayer().getUniqueId());
            isLoggedIn = cloudPlayer.isLoggedIn();
            cloudPlayer.setLastLogin(System.currentTimeMillis());
            cloudPlayer.setConnected(true);
            cloudPlayer.setLastIp(event.getPlayer().getRemoteAddress().getHostString());
            cloudPlayer.setLastConnectedProxyId(VelocityCloudAPI.getInstance().getService().getUniqueId());
            cloudPlayer.updateAsync();
            if(isLoggedIn) VelocityCloudAPI.getInstance().getPlayerManager().getConnectedList().addAsync(cloudPlayer.getUniqueId().toString());
        }

        if(event.getPlayer().isOnlineMode()) return;

        if(isLoggedIn){
            event.getPlayer().sendMessage(LegacyMessageUtils.component("You are logged in as " + event.getPlayer().getUsername() + "!"));
        }
    }

}
