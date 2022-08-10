package dev.redicloud.plugin.velocity.listener;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.event.CloudListener;
import dev.redicloud.api.service.event.CloudServiceStoppedEvent;
import dev.redicloud.plugin.velocity.VelocityCloudAPI;
import dev.redicloud.api.velocity.utils.LegacyMessageUtils;

import java.util.Optional;

public class CloudServiceStoppedListener {

    @CloudListener
    public void onCloudServiceStart(CloudServiceStoppedEvent event) {
        Optional<RegisteredServer> serverInfo = VelocityCloudAPI.getInstance().getProxyServer().getServer(event.getServiceName());
        if (serverInfo.isPresent()) {
            VelocityCloudAPI.getInstance().getProxyServer().unregisterServer(serverInfo.get().getServerInfo());
            CloudAPI.getInstance().getConsole().debug("Unregistered service: " + event.getServiceName());
        }
        if(event.isExternal()) {
            for (Player player : VelocityCloudAPI.getInstance().getProxyServer().getAllPlayers()) {
                if (!player.hasPermission("redicloud.service.notify")) continue;
                player.sendMessage(LegacyMessageUtils.component(VelocityCloudAPI.getInstance().getChatPrefix()
                        + "§3" + event.getServiceName() + "§8(§fExternal§8) » §4§l■"));
            }
        }else{
            CloudAPI.getInstance().getNodeManager().getNodeAsync(event.getNodeId())
                    .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to unregister service: " + event.getServiceName(), e))
                    .onSuccess(node -> {

                        for (Player player : VelocityCloudAPI.getInstance().getProxyServer().getAllPlayers()) {
                            if (!player.hasPermission("redicloud.service.notify")) continue;
                            player.sendMessage(LegacyMessageUtils.component(VelocityCloudAPI.getInstance().getChatPrefix()
                                    + "§3" + event.getServiceName() + "§8(§f"
                                    + node.getName() + "§8) » §4§l■"));
                        }
                    });
        }
    }

}
