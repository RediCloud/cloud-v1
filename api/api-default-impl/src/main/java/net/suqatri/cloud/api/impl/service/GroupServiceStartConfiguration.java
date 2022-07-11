package net.suqatri.cloud.api.impl.service;

import lombok.Getter;
import lombok.Setter;
import net.suqatri.cloud.api.group.ICloudGroup;
import net.suqatri.cloud.api.impl.group.CloudGroup;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.service.IServiceStartConfiguration;
import net.suqatri.cloud.api.service.ServiceEnvironment;

import java.util.Collection;
import java.util.UUID;

@Getter
@Setter
public class GroupServiceStartConfiguration implements IServiceStartConfiguration {

    private UUID uniqueId;
    private ServiceEnvironment environment;
    private String name;
    private int id = -1;
    private int maxMemory;
    private Collection<UUID> possibleNodeIds;
    private int startPriority = 0;
    private boolean staticService = false;
    private Collection<String> templatesNames;
    private IRBucketHolder<ICloudGroup> group;

    @Override
    public boolean isStatic() {
        return this.staticService;
    }

    public void applyFromGroup(IRBucketHolder<ICloudGroup> holder) {
        this.uniqueId = UUID.randomUUID();
        CloudGroup group = holder.getImpl(CloudGroup.class);
        this.environment = group.getServiceEnvironment();
        this.name = group.getName();
        this.maxMemory = group.getMaxMemory();
        this.possibleNodeIds = group.getAssociatedNodeIds();
        this.startPriority = group.getStartPriority();
        this.staticService = group.isStatic();
        this.templatesNames = group.getTemplateNames();
        this.group = holder;
    }
}
