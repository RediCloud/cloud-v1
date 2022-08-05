package net.suqatri.redicloud.plugin.bungeecord.listener;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.event.CloudListener;
import net.suqatri.redicloud.api.service.event.CloudServiceStoppedEvent;
import net.suqatri.redicloud.plugin.bungeecord.BungeeCordCloudAPI;

public class CloudServiceStoppedListener {

    @CloudListener
    public void onCloudServiceStart(CloudServiceStoppedEvent event) {
        ServerInfo serverInfo = ProxyServer.getInstance().getServers().get(event.getServiceName());
        if (serverInfo != null) {
            ProxyServer.getInstance().getServers().remove(serverInfo.getName());
            CloudAPI.getInstance().getConsole().debug("Unregistered service: " + event.getServiceName());
        }
        if(event.isExternal()) return;
        CloudAPI.getInstance().getNodeManager().getNodeAsync(event.getNodeId())
                .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to unregister service: " + event.getServiceName(), e))
                .onSuccess(nodeHolder -> {
                    for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                        if (!player.hasPermission("redicloud.service.notify")) continue;
                        player.sendMessage(BungeeCordCloudAPI.getInstance().getChatPrefix()
                                + "§3" + event.getServiceName() + "§8(§f" + nodeHolder.get().getName() + "§8) » §4§l■");
                    }
                });
    }

}
