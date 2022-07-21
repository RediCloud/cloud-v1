package net.suqatri.redicloud.node.listener;

import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.event.CloudListener;
import net.suqatri.redicloud.api.service.event.CloudServiceStartedEvent;

public class CloudServiceStartedListener {

    @CloudListener
    public void onServiceStarted(CloudServiceStartedEvent event){
        CloudAPI.getInstance().getConsole().info("%hc" + event.getServiceName() + "%tc is now connected to the cluster!");
    }

}
