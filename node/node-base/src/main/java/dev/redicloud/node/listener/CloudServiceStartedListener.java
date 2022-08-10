package dev.redicloud.node.listener;

import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.event.CloudListener;
import dev.redicloud.api.service.event.CloudServiceStartedEvent;

public class CloudServiceStartedListener {

    @CloudListener
    public void onServiceStarted(CloudServiceStartedEvent event) {
        CloudAPI.getInstance().getConsole().info("%hc" + event.getServiceName() + "%tc is now connected to the cluster!");
    }

}
