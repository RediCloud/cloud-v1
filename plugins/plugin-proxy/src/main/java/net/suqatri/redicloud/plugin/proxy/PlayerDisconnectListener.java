package net.suqatri.redicloud.plugin.proxy;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.player.CloudPlayer;
import net.suqatri.redicloud.api.impl.player.CloudPlayerManager;

public class PlayerDisconnectListener implements Listener {

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        ProxyCloudAPI.getInstance().getService().setOnlineCount(ProxyServer.getInstance().getOnlineCount()-1);
        ProxyCloudAPI.getInstance().getService().updateAsync();
        CloudAPI.getInstance().getPlayerManager().getPlayerAsync(event.getPlayer().getUniqueId())
            .onFailure(throwable -> CloudAPI.getInstance().getConsole().error("", throwable))
            .onSuccess(playerHolder -> {
                if(event.getPlayer().isConnected()) return;
                CloudPlayer cloudPlayer = playerHolder.getImpl(CloudPlayer.class);
                cloudPlayer.setLastLogout(System.currentTimeMillis());
                cloudPlayer.setConnected(false);
                cloudPlayer.updateAsync()
                        .onFailure(throwable -> CloudAPI.getInstance().getConsole().error("", throwable))
                        .onSuccess(v -> {
                            if(event.getPlayer().isConnected()) return;
                            ((CloudPlayerManager)CloudAPI.getInstance().getPlayerManager()).removeCachedBucketHolder(event.getPlayer().getUniqueId().toString());
                        });
            });
    }

}
