package net.suqatri.cloud.api.node.service.screen;

import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.service.ICloudService;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface IServiceScreenManager {

    IServiceScreen getServiceScreen(IRBucketHolder<ICloudService> serviceHolder);

    void join(IServiceScreen serviceScreen);

    void leave(IServiceScreen serviceScreen);

    boolean isActive(IServiceScreen serviceScreen);
    boolean isActive(UUID serviceId);

    Collection<IServiceScreen> getActiveScreens();

    boolean isAnyScreenActive();

}
