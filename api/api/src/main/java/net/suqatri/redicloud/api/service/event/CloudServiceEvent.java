package net.suqatri.redicloud.api.service.event;

import lombok.Getter;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.event.CloudGlobalEvent;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.commons.function.future.FutureAction;

import java.util.UUID;

@Getter
public class CloudServiceEvent extends CloudGlobalEvent {

    private final UUID serverId;
    private final String serviceName;
    private final UUID nodeId;
    private final boolean external;

    public CloudServiceEvent(ICloudService service) {
        this.serverId = service.getUniqueId();
        this.serviceName = service.getServiceName();
        this.nodeId = service.getNodeId();
        this.external = this.nodeId == null;
    }

    public CloudServiceEvent() {
        this.serverId = null;
        this.serviceName = null;
        this.nodeId = null;
        this.external = false;
    }

    public ICloudService getService() {
        return CloudAPI.getInstance().getServiceManager().getService(this.serverId);
    }

    public FutureAction<ICloudService> getServiceAsync() {
        return CloudAPI.getInstance().getServiceManager().getServiceAsync(this.serverId);
    }

}
