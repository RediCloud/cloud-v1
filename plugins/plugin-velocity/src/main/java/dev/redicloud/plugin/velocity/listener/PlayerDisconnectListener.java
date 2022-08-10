package dev.redicloud.plugin.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.impl.player.CloudPlayer;
import dev.redicloud.api.impl.player.CloudPlayerManager;

public class PlayerDisconnectListener {

    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event) {

        CloudAPI.getInstance().getPlayerManager().getPlayerAsync(event.getPlayer().getUniqueId())
                .onFailure(throwable -> CloudAPI.getInstance().getConsole().error("", throwable))
                .onSuccess(player -> {
                    if (event.getPlayer().isActive()) return;
                    CloudPlayer cloudPlayer = (CloudPlayer) player;
                    cloudPlayer.setLastLogout(System.currentTimeMillis());
                    cloudPlayer.setConnected(false);
                    cloudPlayer.updateAsync()
                            .onFailure(throwable -> CloudAPI.getInstance().getConsole().error("", throwable))
                            .onSuccess(v -> {
                                if (event.getPlayer().isActive()) return;
                                ((CloudPlayerManager) CloudAPI.getInstance().getPlayerManager()).removeCache(event.getPlayer().getUniqueId().toString());
                            });
                });
    }

}
