package net.suqatri.redicloud.plugin.velocity.listener;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.event.CloudListener;
import net.suqatri.redicloud.api.service.event.CloudServiceStoppedEvent;
import net.suqatri.redicloud.plugin.velocity.VelocityCloudAPI;
import net.suqatri.redicloud.api.velocity.utils.LegacyMessageUtils;

import java.util.Optional;

public class CloudServiceStoppedListener {

    @CloudListener
    public void onCloudServiceStart(CloudServiceStoppedEvent event) {
        Optional<RegisteredServer> serverInfo = VelocityCloudAPI.getInstance().getProxyServer().getServer(event.getServiceName());
        if (serverInfo.isPresent()) {
            VelocityCloudAPI.getInstance().getProxyServer().unregisterServer(serverInfo.get().getServerInfo());
            CloudAPI.getInstance().getConsole().debug("Unregistered service: " + event.getServiceName());
        }
        if(event.isExternal()) return;
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
