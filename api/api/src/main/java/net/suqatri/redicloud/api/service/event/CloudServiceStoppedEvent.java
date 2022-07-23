package net.suqatri.redicloud.api.service.event;

import net.suqatri.redicloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.redicloud.api.service.ICloudService;

public class CloudServiceStoppedEvent extends CloudServiceEvent {

    public CloudServiceStoppedEvent(IRBucketHolder<ICloudService> holder) {
        super(holder);
    }

    public CloudServiceStoppedEvent() {
        super();
    }
}
