package net.suqatri.redicloud.node.listener;

import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.event.CloudListener;
import net.suqatri.redicloud.api.service.event.CloudServiceStoppedEvent;

public class CloudServiceStoppedListener {

    @CloudListener
    public void onServiceStarted(CloudServiceStoppedEvent event) {
        CloudAPI.getInstance().getConsole().info("%hc" + event.getServiceName() + "%tc is disconnected from the cluster!");
    }

}
