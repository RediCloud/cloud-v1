package net.suqatri.cloud.api.impl;

import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.impl.event.CloudEventHandler;
import net.suqatri.cloud.api.utils.ApplicationType;

public abstract class CloudAPIImpl extends CloudAPI {

    private CloudEventHandler eventHandler;

    public CloudAPIImpl(ApplicationType type) {
        super(type);
        this.eventHandler = new CloudEventHandler();
    }

    public CloudEventHandler getEventHandler() {
        return this.eventHandler;
    }
}
