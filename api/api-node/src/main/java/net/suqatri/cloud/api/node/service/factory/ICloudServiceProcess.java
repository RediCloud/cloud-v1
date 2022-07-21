package net.suqatri.cloud.api.node.service.factory;

import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.service.ICloudService;
import net.suqatri.cloud.commons.function.future.FutureAction;

import java.io.File;

public interface ICloudServiceProcess {

    IRBucketHolder<ICloudService> getServiceHolder();
    void executeCommand(String command);
    boolean start() throws Exception;
    FutureAction<Boolean> stopAsync(boolean force);
    boolean isActive();
    File getServiceDirectory();
    int getPort();
    void setPort(int port);

}
