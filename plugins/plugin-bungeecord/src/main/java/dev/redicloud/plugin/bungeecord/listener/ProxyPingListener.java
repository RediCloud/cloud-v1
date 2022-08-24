package dev.redicloud.plugin.bungeecord.listener;

import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.plugin.bungeecord.BungeeCordCloudAPI;

import java.util.concurrent.TimeUnit;

public class ProxyPingListener implements Listener {

    private int cachedNetworkOnlineCount = 0;
    private long cacheTime = 0L;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPing(ProxyPingEvent event) {
        if(BungeeCordCloudAPI.getInstance().isShutdownInitiated()) return;

        ServerPing serverPing = event.getResponse();

        if (serverPing.getDescription().contains("Another Bungee server") || serverPing.getDescription().contains("RediCloud") || serverPing.getDescription().contains("Just another Waterfall")) {
            serverPing.setDescription(BungeeCordCloudAPI.getInstance().getService().getMotd());
        }

        ServerPing.Players players = new ServerPing
                .Players(BungeeCordCloudAPI.getInstance().getService().getMaxPlayers(), this.cachedNetworkOnlineCount, new ServerPing.PlayerInfo[0]);
        serverPing.setPlayers(players);

        event.setResponse(serverPing);

        if ((System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(2)) > cacheTime) {
            CloudAPI.getInstance().getPlayerManager().getOnlineCount()
                    .onFailure(t -> CloudAPI.getInstance().getConsole().error("Failed to get online count!", t))
                    .onSuccess(onlineCount -> cachedNetworkOnlineCount = onlineCount);
            cacheTime = System.currentTimeMillis();
        }
    }

}


