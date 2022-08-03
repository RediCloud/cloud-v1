package net.suqatri.redicloud.plugin.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.player.CloudPlayer;
import net.suqatri.redicloud.api.impl.player.CloudPlayerManager;
import net.suqatri.redicloud.plugin.velocity.VelocityCloudAPI;

public class PlayerDisconnectListener {

    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event) {
        //TODO remove online count from api, get directly from proxy server
        VelocityCloudAPI.getInstance().setOnlineCount(VelocityCloudAPI.getInstance().getOnlineCount() - 1);

        CloudAPI.getInstance().getPlayerManager().getPlayerAsync(event.getPlayer().getUniqueId())
                .onFailure(throwable -> CloudAPI.getInstance().getConsole().error("", throwable))
                .onSuccess(playerHolder -> {
                    if (event.getPlayer().isActive()) return;
                    CloudPlayer cloudPlayer = playerHolder.getImpl(CloudPlayer.class);
                    cloudPlayer.setLastLogout(System.currentTimeMillis());
                    cloudPlayer.setConnected(false);
                    cloudPlayer.updateAsync()
                            .onFailure(throwable -> CloudAPI.getInstance().getConsole().error("", throwable))
                            .onSuccess(v -> {
                                if (event.getPlayer().isActive()) return;
                                ((CloudPlayerManager) CloudAPI.getInstance().getPlayerManager()).removeCachedBucketHolder(event.getPlayer().getUniqueId().toString());
                            });
                });
    }

}
