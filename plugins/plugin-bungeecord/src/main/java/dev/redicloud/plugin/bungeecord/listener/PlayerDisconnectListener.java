package dev.redicloud.plugin.bungeecord.listener;

import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.impl.player.CloudPlayer;
import dev.redicloud.api.impl.player.CloudPlayerManager;
import dev.redicloud.plugin.bungeecord.BungeeCordCloudAPI;

public class PlayerDisconnectListener implements Listener {

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {

        if(BungeeCordCloudAPI.getInstance().getPlayerManager().isCached(event.getPlayer().getUniqueId().toString())) {
            CloudAPI.getInstance().getPlayerManager().getPlayerAsync(event.getPlayer().getUniqueId())
                    .onFailure(throwable -> CloudAPI.getInstance().getConsole().error("Error while getting player", throwable))
                    .onSuccess(player -> {
                        if (event.getPlayer().isConnected()) return;
                        CloudPlayer cloudPlayer = (CloudPlayer) player;
                        cloudPlayer.setLastLogout(System.currentTimeMillis());
                        cloudPlayer.setConnected(false);
                        cloudPlayer.updateAsync()
                                .onFailure(throwable -> CloudAPI.getInstance().getConsole().error("", throwable))
                                .onSuccess(v -> {
                                    if (event.getPlayer().isConnected()) return;
                                    ((CloudPlayerManager) CloudAPI.getInstance().getPlayerManager()).removeCache(event.getPlayer().getUniqueId().toString());
                                });
                    });
        }
    }

}
