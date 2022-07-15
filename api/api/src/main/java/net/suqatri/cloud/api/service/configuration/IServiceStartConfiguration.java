package net.suqatri.cloud.api.service.configuration;

import net.suqatri.cloud.api.group.ICloudGroup;
import net.suqatri.cloud.api.node.ICloudNode;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.service.ServiceEnvironment;
import net.suqatri.cloud.api.template.ICloudServiceTemplate;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface IServiceStartConfiguration {

    ServiceEnvironment getEnvironment();
    String getJavaCommand();

    String getName();
    UUID getUniqueId();

    int getId();

    int getMaxMemory();

    default int getStartPort(){
        return -1;
    }

    Collection<UUID> getPossibleNodeIds();

    int getStartPriority();

    boolean isStatic();

    Collection<String> getTemplateNames();

    @Nullable
    IRBucketHolder<ICloudGroup> getGroup();

    List<String> getProcessParameters();
    List<String> getJvmArguments();

}
