package dev.redicloud.api.service.event;

import dev.redicloud.api.service.ICloudService;

public class CloudServiceStoppedEvent extends CloudServiceEvent {

    public CloudServiceStoppedEvent(ICloudService holder) {
        super(holder);
    }

    public CloudServiceStoppedEvent() {
        super();
    }
}
