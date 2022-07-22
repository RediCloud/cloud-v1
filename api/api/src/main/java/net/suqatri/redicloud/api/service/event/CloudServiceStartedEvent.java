package net.suqatri.redicloud.api.service.event;

import net.suqatri.redicloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.redicloud.api.service.ICloudService;

public class CloudServiceStartedEvent extends CloudServiceEvent {

    public CloudServiceStartedEvent(IRBucketHolder<ICloudService> holder) {
        super(holder);
    }

    public CloudServiceStartedEvent(){
        super();
    }
}
