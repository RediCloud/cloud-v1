package net.suqatri.cloud.api.node.service.screen;

import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.service.ICloudService;
import org.redisson.api.RList;

public interface IServiceScreen {

    IRBucketHolder<ICloudService> getService();
    RList<IScreenLine> getLines();
    void addLine(String line);
    void removeUselessLines();
}
