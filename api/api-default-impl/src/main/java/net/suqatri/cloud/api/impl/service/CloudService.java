package net.suqatri.cloud.api.impl.service;

import lombok.Getter;
import lombok.Setter;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.impl.CloudDefaultAPIImpl;
import net.suqatri.cloud.api.impl.redis.bucket.RBucketObject;
import net.suqatri.cloud.api.network.NetworkComponentType;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.service.ICloudService;
import net.suqatri.cloud.api.service.configuration.IServiceStartConfiguration;
import net.suqatri.cloud.api.service.ServiceState;
import net.suqatri.cloud.api.service.version.ICloudServiceVersion;
import net.suqatri.cloud.commons.function.future.FutureAction;


@Getter
@Setter
public class CloudService extends RBucketObject implements ICloudService {

    private IServiceStartConfiguration configuration;
    private String motd = "Unknown";
    private int maxPlayers = -1;
    private ServiceState serviceState = ServiceState.UNKNOWN;
    private int onlineCount = -1;
    private int maxRam = -1;
    private int ramUsage = -1;

    @Override
    public void merged() {
        if(CloudAPI.getInstance().getApplicationType().getNetworkComponentType() != NetworkComponentType.SERVICE) return;
        CloudDefaultAPIImpl.getInstance().updateApplicationProperties(this);
    }

    @Override
    public FutureAction<IRBucketHolder<ICloudServiceVersion>> getServiceVersion() {
        return CloudAPI.getInstance().getServiceVersionManager().getServiceVersionAsync(this.configuration.getServiceVersionName());
    }

}
