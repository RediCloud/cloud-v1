package dev.redicloud.api.service.event;

import dev.redicloud.api.service.ICloudService;

public class CloudServiceStartedEvent extends CloudServiceEvent {

    public CloudServiceStartedEvent(ICloudService holder) {
        super(holder);
    }

    public CloudServiceStartedEvent() {
        super();
    }
}
