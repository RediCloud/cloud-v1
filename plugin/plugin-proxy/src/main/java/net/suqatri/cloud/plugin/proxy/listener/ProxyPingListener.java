package net.suqatri.cloud.plugin.proxy.listener;

import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.plugin.proxy.ProxyCloudAPI;

import java.util.concurrent.TimeUnit;

public class ProxyPingListener implements Listener {

    private int cachedNetworkOnlineCount = 0;
    private long cacheTime = 0L;

    @EventHandler
    public void onPing(ProxyPingEvent event){
        ServerPing serverPing = event.getResponse();
        serverPing.setDescription(ProxyCloudAPI.getInstance().getService().getMotd());

        ServerPing.Players players = new ServerPing
                .Players(ProxyCloudAPI.getInstance().getService().getMaxPlayers(), this.cachedNetworkOnlineCount, new ServerPing.PlayerInfo[0]);
        serverPing.setPlayers(players);

        event.setResponse(serverPing);

        if((System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(2)) > cacheTime){
            event.registerIntent(ProxyCloudAPI.getInstance().getPlugin());
            CloudAPI.getInstance().getPlayerManager().getOnlineCount()
                    .onFailure(throwable -> event.completeIntent(ProxyCloudAPI.getInstance().getPlugin()))
                    .onSuccess(onlineCount -> cachedNetworkOnlineCount = onlineCount);
            cacheTime = System.currentTimeMillis();
        }
    }

}


