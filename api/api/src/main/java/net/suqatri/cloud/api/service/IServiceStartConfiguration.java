package net.suqatri.cloud.api.service;

import net.suqatri.cloud.api.group.ICloudGroup;
import net.suqatri.cloud.api.node.ICloudNode;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.template.ICloudServiceTemplate;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

public interface IServiceStartConfiguration {

    ServiceEnvironment getEnvironment();

    String getName();
    UUID getUniqueId();

    int getId();

    int getMaxMemory();

    Collection<UUID> getPossibleNodeIds();

    int getStartPriority();

    boolean isStatic();

    Collection<String> getTemplatesNames();

    @Nullable
    IRBucketHolder<ICloudGroup> getGroup();

}
