package net.suqatri.cloud.api.service;

import net.suqatri.cloud.api.group.ICloudGroup;
import net.suqatri.cloud.api.node.ICloudNode;
import net.suqatri.cloud.api.template.ICloudServiceTemplate;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

public interface IServiceStartConfiguration {

    ServiceEnvironment getEnvironment();

    String getName();
    UUID getUniqueId();

    @Nullable
    int getId();

    int getMaxMemory();

    Collection<ICloudNode> getPossibleNodes();

    default int getStartPriority(){
        return 0;
    }

    default boolean isStatic(){
        return false;
    }

    Collection<ICloudServiceTemplate> getTemplates();

    @Nullable
    ICloudGroup getGroup();

    void applyFromGroup(ICloudGroup group);

}
