package dev.redicloud.plugin.velocity.listener;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.server.ServerPing;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.plugin.velocity.VelocityCloudAPI;
import dev.redicloud.api.velocity.utils.LegacyMessageUtils;

import java.util.concurrent.TimeUnit;

public class ProxyPingListener  {

    private int cachedNetworkOnlineCount = 0;
    private long cacheTime = 0L;

    @Subscribe(order = PostOrder.LATE)
    public void onPing(ProxyPingEvent event) {
        if(!VelocityCloudAPI.getInstance().isShutdownInitiated()) return;

        ServerPing.Builder builder = event.getPing().asBuilder();

        boolean isDefaultMOTD = builder.getDescriptionComponent().isPresent();

        String description = isDefaultMOTD ? LegacyMessageUtils.legacyText(builder.getDescriptionComponent().get()) : "";

        if(isDefaultMOTD){
            CloudAPI.getInstance().getConsole().trace("Default MOTD: " + description + "|");
            if (description.contains("A Velocity Server") || description.contains("RediCloud")) {
                builder.description(LegacyMessageUtils.component(
                        VelocityCloudAPI.getInstance().getService().getMotd()));
            }
        }else{
            builder.description(LegacyMessageUtils.component(
                    VelocityCloudAPI.getInstance().getService().getMotd()));
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


