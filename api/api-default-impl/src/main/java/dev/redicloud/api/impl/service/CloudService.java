package dev.redicloud.api.impl.service;

import dev.redicloud.api.impl.redis.bucket.RBucketObject;
import dev.redicloud.api.impl.redis.bucket.fetch.RBucketFetchAble;
import lombok.Getter;
import lombok.Setter;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.impl.CloudDefaultAPIImpl;
import dev.redicloud.api.network.INetworkComponentInfo;
import dev.redicloud.api.network.NetworkComponentType;
import dev.redicloud.api.service.ICloudService;
import dev.redicloud.api.service.ServiceState;
import dev.redicloud.api.service.configuration.IServiceStartConfiguration;
import dev.redicloud.api.service.version.ICloudServiceVersion;
import dev.redicloud.commons.function.future.FutureAction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

@Getter
@Setter
public class CloudService extends RBucketFetchAble implements ICloudService {

    private IServiceStartConfiguration configuration;
    private String motd = "Unknown";
    private int maxPlayers = -1;
    private ServiceState serviceState = ServiceState.UNKNOWN;
    private int onlineCount = -1;
    private long maxRam = -1;
    private UUID nodeId;
    private Collection<UUID> consoleNodeListenerIds = new ArrayList<>();
    private int port;
    private String hostName;
    private boolean isExternal = true;
    private boolean maintenance = false;
    private long lastPlayerAction = -1L;

    @Override
    public void executeCommand(String command) {
        CloudAPI.getInstance().getServiceManager().executeCommand(this, command);
    }

    @Override
    public String getIdentifier() {
        return this.getUniqueId().toString();
    }

    @Override
    public void merged() {
        if (CloudAPI.getInstance().getApplicationType().getNetworkComponentType() != NetworkComponentType.SERVICE)
            return;
        CloudDefaultAPIImpl.getInstance().updateApplicationProperties(this);
    }

    @Override
    public INetworkComponentInfo getNetworkComponentInfo() {
        return CloudAPI.getInstance().getNetworkComponentManager().getComponentInfo(NetworkComponentType.SERVICE, this.getUniqueId());
    }

    @Override
    public FutureAction<ICloudServiceVersion> getServiceVersion() {
        return CloudAPI.getInstance().getServiceVersionManager().getServiceVersionAsync(this.configuration.getServiceVersionName());
    }

    @Override
    public String getFetchKey() {
        return this.getServiceName().toLowerCase();
    }

    @Override
    public String getFetchValue() {
        return getUniqueId().toString();
    }
}
