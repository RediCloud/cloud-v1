package dev.redicloud.node.listener;

import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.event.CloudListener;
import dev.redicloud.api.service.event.CloudServiceStoppedEvent;

public class CloudServiceStoppedListener {

    @CloudListener
    public void onServiceStarted(CloudServiceStoppedEvent event) {
        CloudAPI.getInstance().getConsole().info("%hc" + event.getServiceName() + "%tc is disconnected from the cluster!");
    }

}
