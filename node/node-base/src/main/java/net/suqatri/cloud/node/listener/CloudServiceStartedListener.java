package net.suqatri.cloud.node.listener;

import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.event.CloudListener;
import net.suqatri.cloud.api.service.event.CloudServiceStartedEvent;

public class CloudServiceStartedListener {

    @CloudListener
    public void onServiceStarted(CloudServiceStartedEvent event){
        CloudAPI.getInstance().getConsole().info("%hc" + event.getServiceName() + "%tc is now connected to the cluster!");
    }

}
