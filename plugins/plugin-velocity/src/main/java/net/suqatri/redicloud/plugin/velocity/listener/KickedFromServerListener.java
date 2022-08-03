package net.suqatri.redicloud.plugin.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.plugin.velocity.VelocityCloudAPI;
import net.suqatri.redicloud.plugin.velocity.utils.LegacyMessageUtils;

import java.util.Optional;

public class KickedFromServerListener {

    @Subscribe
    public void onServerKick(KickedFromServerEvent event) {
        IRBucketHolder<ICloudService> fallbackHolder = CloudAPI.getInstance().getServiceManager().getFallbackService();
        if (fallbackHolder == null) {
            event.getPlayer().disconnect(LegacyMessageUtils.component("Fallback service is not available."));
            return;
        }
        if(event.getServerKickReason().isPresent()) event.getPlayer().sendMessage(event.getServerKickReason().get());
        Optional<RegisteredServer> registeredServer = VelocityCloudAPI.getInstance().getProxyServer().getServer(fallbackHolder.get().getServiceName());
        if(!registeredServer.isPresent()) {
            event.getPlayer().disconnect(LegacyMessageUtils.component("Fallback service is not available."));
            return;
        }
        event.setResult(KickedFromServerEvent.RedirectPlayer.create(registeredServer.get()));
    }

}
