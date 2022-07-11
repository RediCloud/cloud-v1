package net.suqatri.cloud.api.service.event;

import lombok.Data;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.event.CloudGlobalEvent;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.service.ICloudService;
import net.suqatri.cloud.commons.function.future.FutureAction;

import java.util.UUID;

@Data
public class CloudServiceEvent extends CloudGlobalEvent {

    private UUID serverId;

    public IRBucketHolder<ICloudService> getService() {
        return CloudAPI.getInstance().getServiceManager().getService(this.serverId);
    }

    public FutureAction<IRBucketHolder<ICloudService>> getServiceAsync() {
        return CloudAPI.getInstance().getServiceManager().getServiceAsync(this.serverId);
    }

}
