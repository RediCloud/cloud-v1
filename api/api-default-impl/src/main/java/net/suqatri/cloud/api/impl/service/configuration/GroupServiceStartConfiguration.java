package net.suqatri.cloud.api.impl.service.configuration;

import lombok.Getter;
import lombok.Setter;
import net.suqatri.cloud.api.group.ICloudGroup;
import net.suqatri.cloud.api.impl.group.CloudGroup;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.service.configuration.IServiceStartConfiguration;
import net.suqatri.cloud.api.service.ServiceEnvironment;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
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
    private List<String> processParameters;
    private List<String> jvmArguments;
    private String javaCommand;

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
        this.processParameters = Arrays.asList(group.getProcessParameters());
        this.jvmArguments = Arrays.asList(group.getJvmArguments());
        this.javaCommand = group.getJavaCommand();
    }
}