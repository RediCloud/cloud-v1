package net.suqatri.cloud.node.service.factory;

import lombok.Data;
import lombok.Getter;
import net.suqatri.cloud.api.node.ICloudNode;
import net.suqatri.cloud.api.node.service.factory.ICloudServiceCopier;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.service.ICloudService;
import net.suqatri.cloud.commons.function.future.FutureAction;

import java.io.File;

@Data
public class CloudServiceCopier implements ICloudServiceCopier {

    private final CloudServiceServiceProcess process;

    @Override
    public FutureAction<File> copyFilesAsync() {
        return null;
    }

    @Override
    public File copyFiles() {
        return null;
    }

    @Override
    public IRBucketHolder<ICloudService> getServiceHolder() {
        return this.process.getServiceHolder();
    }

    @Override
    public File getServiceDirectory() {
        return this.process.getServiceDirectory();
    }
}
