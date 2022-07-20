package net.suqatri.cloud.api.service.event;

import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.service.ICloudService;

import java.util.UUID;

public class CloudServiceStoppedEvent extends CloudServiceEvent {

    public CloudServiceStoppedEvent(IRBucketHolder<ICloudService> holder) {
        super(holder);
    }
}
