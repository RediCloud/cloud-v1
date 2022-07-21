package net.suqatri.redicloud.api.service.event;

import lombok.Getter;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.event.CloudGlobalEvent;
import net.suqatri.redicloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.commons.function.future.FutureAction;

import java.util.UUID;

@Getter
public class CloudServiceEvent extends CloudGlobalEvent {

    private final UUID serverId;
    private final String serviceName;

    public CloudServiceEvent(IRBucketHolder<ICloudService> holder){
        this.serverId = holder.get().getUniqueId();
        this.serviceName = holder.get().getServiceName();
    }

    public CloudServiceEvent(){
        this.serverId = null;
        this.serviceName = null;
    }

    public IRBucketHolder<ICloudService> getService() {
        return CloudAPI.getInstance().getServiceManager().getService(this.serverId);
    }

    public FutureAction<IRBucketHolder<ICloudService>> getServiceAsync() {
        return CloudAPI.getInstance().getServiceManager().getServiceAsync(this.serverId);
    }

}
