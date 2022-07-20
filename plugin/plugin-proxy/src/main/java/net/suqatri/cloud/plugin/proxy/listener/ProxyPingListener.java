package net.suqatri.cloud.plugin.proxy.listener;

import net.md_5.bungee.api.Favicon;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.plugin.proxy.ProxyCloudAPI;

import java.awt.image.BufferedImage;
import java.util.concurrent.TimeUnit;

public class ProxyPingListener implements Listener {

    private int cachedNetworkOnlineCount = 0;
    private long cacheTime = 0L;

    @EventHandler(priority = EventPriority.HIGH)
    public void onPing(ProxyPingEvent event){
        ServerPing serverPing = event.getResponse();


        if (serverPing.getDescription().contains("Another Bungee server")) {
            serverPing.setDescription(ProxyCloudAPI.getInstance().getService().getMotd());
        }

        ServerPing.Players players = new ServerPing
                .Players(ProxyCloudAPI.getInstance().getService().getMaxPlayers(), this.cachedNetworkOnlineCount, new ServerPing.PlayerInfo[0]);
        serverPing.setPlayers(players);

        event.setResponse(serverPing);

        if((System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(2)) > cacheTime){
            CloudAPI.getInstance().getPlayerManager().getOnlineCount()
                    .onFailure(throwable -> event.completeIntent(ProxyCloudAPI.getInstance().getPlugin()))
                    .onSuccess(onlineCount -> cachedNetworkOnlineCount = onlineCount);
            cacheTime = System.currentTimeMillis();
        }
    }

}


