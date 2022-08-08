package net.suqatri.redicloud.node.listener;

import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.event.CloudListener;
import net.suqatri.redicloud.api.player.ICloudPlayer;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.api.service.ServiceEnvironment;
import net.suqatri.redicloud.api.service.event.CloudServiceStartedEvent;

public class CloudServiceStartedListener {

    @CloudListener
    public void onServiceStarted(CloudServiceStartedEvent event) {
        CloudAPI.getInstance().getConsole().info("%hc" + event.getServiceName() + "%tc is now connected to the cluster!");

        event.getServiceAsync()
            .onFailure(e -> CloudAPI.getInstance().getConsole().error("%hc" + event.getServiceName() + "%tc is not connected to the cluster!", e))
            .onSuccess(service -> {
                if(!event.getService().isFallback()) return;

                CloudAPI.getInstance().getServiceManager().getServicesAsync()
                    .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to get services!", e))
                    .onSuccess(services -> {

                        ICloudService fallback = services.parallelStream()
                                .filter(s -> s.getEnvironment() == ServiceEnvironment.LIMBO
                                        && s.getName().equals("Fallback")
                                        && s.isFallback())
                                .findFirst()
                                .orElse(null);

                        if(fallback == null) return;

                        CloudAPI.getInstance().getPlayerManager().getConnectedPlayers()
                            .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to get connected players!", e))
                            .onSuccess(players -> {
                                for (ICloudPlayer player : players) {
                                    if(!player.getLastConnectedServerId().equals(fallback.getUniqueId())) continue;
                                    player.getBridge().connect(service);
                                }
                            });
                    });
            });
    }

}
