package dev.redicloud.api.service.event;

import lombok.Getter;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.event.CloudGlobalEvent;
import dev.redicloud.api.service.ICloudService;
import dev.redicloud.commons.function.future.FutureAction;

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
