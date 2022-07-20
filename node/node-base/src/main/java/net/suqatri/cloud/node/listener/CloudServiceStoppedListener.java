package net.suqatri.cloud.node.listener;

import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.event.CloudListener;
import net.suqatri.cloud.api.service.event.CloudServiceStartedEvent;
import net.suqatri.cloud.api.service.event.CloudServiceStoppedEvent;

public class CloudServiceStoppedListener {

    @CloudListener
    public void onServiceStarted(CloudServiceStoppedEvent event){
        CloudAPI.getInstance().getConsole().info("%hc" + event.getServiceName() + "%tc is disconnected from the cluster!");
    }

}
