package net.suqatri.redicloud.api.node.service.screen;

import net.suqatri.redicloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.redicloud.api.service.ICloudService;
import org.redisson.api.RList;

public interface IServiceScreen {

    IRBucketHolder<ICloudService> getService();

    RList<IScreenLine> getLines();

    void addLine(String line);

    void removeUselessLines();

    void deleteLines();
}
