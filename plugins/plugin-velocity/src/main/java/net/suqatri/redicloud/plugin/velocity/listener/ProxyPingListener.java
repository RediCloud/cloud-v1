package net.suqatri.redicloud.plugin.velocity.listener;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.server.ServerPing;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.plugin.velocity.VelocityCloudAPI;
import net.suqatri.redicloud.plugin.velocity.utils.LegacyMessageUtils;

import java.util.concurrent.TimeUnit;

public class ProxyPingListener  {

    private int cachedNetworkOnlineCount = 0;
    private long cacheTime = 0L;

    @Subscribe(order = PostOrder.LATE)
    public void onPing(ProxyPingEvent event) {
        ServerPing.Builder builder = event.getPing().asBuilder();


        boolean isDefaultMOTD = builder.getDescriptionComponent().isPresent();

        String description = isDefaultMOTD ? LegacyMessageUtils.legacyText(builder.getDescriptionComponent().get()) : "";

        if(isDefaultMOTD){
            if (description.contains("Another Bungee server") || description.contains("RediCloud")) {
                builder.description(LegacyMessageUtils.component(
                        VelocityCloudAPI.getInstance().getService().getMotd()));
            }
        }

        builder.maximumPlayers(VelocityCloudAPI.getInstance().getService().getMaxPlayers());
        builder.onlinePlayers(this.cachedNetworkOnlineCount);

        event.setPing(builder.build());

        if ((System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(2)) > cacheTime) {
            CloudAPI.getInstance().getPlayerManager().getOnlineCount()
                    .onFailure(t -> CloudAPI.getInstance().getConsole().error("Error while getting online count!", t))
                    .onSuccess(onlineCount -> cachedNetworkOnlineCount = onlineCount);
            cacheTime = System.currentTimeMillis();
        }
    }

}


