package net.suqatri.cloud.api.node.service.factory;

import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.service.ICloudService;
import net.suqatri.cloud.commons.function.future.FutureAction;

import java.io.File;
import java.io.IOException;

public interface ICloudServiceCopier {

    FutureAction<File> copyFilesAsync();
    File copyFiles() throws IOException;
    IRBucketHolder<ICloudService> getServiceHolder();
    File getServiceDirectory();

}
