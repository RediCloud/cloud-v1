package net.suqatri.cloud.api.service.event;

import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.service.ICloudService;

public class CloudServiceStartedEvent extends CloudServiceEvent {

    public CloudServiceStartedEvent(IRBucketHolder<ICloudService> holder) {
        super(holder);
    }
}
