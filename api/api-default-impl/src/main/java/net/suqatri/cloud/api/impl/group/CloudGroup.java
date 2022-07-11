package net.suqatri.cloud.api.impl.group;

import lombok.Getter;
import lombok.Setter;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.group.ICloudGroup;
import net.suqatri.cloud.api.impl.redis.bucket.RBucketObject;
import net.suqatri.cloud.api.node.ICloudNode;
import net.suqatri.cloud.api.player.ICloudPlayer;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.service.ICloudService;
import net.suqatri.cloud.api.service.IServiceStartConfiguration;
import net.suqatri.cloud.api.service.ServiceEnvironment;
import net.suqatri.cloud.api.service.ServiceState;
import net.suqatri.cloud.api.template.ICloudServiceTemplate;
import net.suqatri.cloud.commons.function.future.FutureAction;
import net.suqatri.cloud.commons.function.future.FutureActionCollection;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public class CloudGroup extends RBucketObject implements ICloudGroup {

    private ServiceEnvironment serviceEnvironment;
    private UUID uniqueId;
    private String name;
    @Setter
    private String[] processParameters;
    @Setter
    private String[] jvmArguments;
    @Setter
    private String javaCommand;
    @Setter
    private int minServices;
    @Setter
    private int maxServices;
    private Collection<String> templateNames;
    @Setter
    private boolean staticGroup;
    private Collection<UUID> associatedNodeIds;
    @Setter
    private int startPort;
    @Setter
    private boolean maintenance;
    @Setter
    private int maxMemory;
    @Setter
    private int startPriority;

    @Override
    public FutureAction<Collection<IRBucketHolder<ICloudServiceTemplate>>> getTemplates() {
        FutureAction<Collection<IRBucketHolder<ICloudServiceTemplate>>> futureAction = new FutureAction<>();

        //TODO SErviceTEmplateManager

        return futureAction;
    }

    @Override
    public void setTemplates(Collection<ICloudServiceTemplate> templates) {
        this.templateNames = templates.stream().map(ICloudServiceTemplate::getName).collect(Collectors.toList());
    }

    @Override
    public void addTemplate(ICloudServiceTemplate template) {
        this.templateNames.add(template.getName());
    }

    @Override
    public void removeTemplate(ICloudServiceTemplate template) {
        this.templateNames.remove(template.getName());
    }

    @Override
    public boolean hasTemplate(ICloudServiceTemplate template) {
        return this.templateNames.contains(template.getName());
    }

    @Override
    public int getOnlineServiceCount() {
        return 0; //TODO ServiceManager
    }

    @Override
    public int getServicesCount(ServiceState serviceState) {
        return 0; //TODO ServiceManager
    }

    @Override
    public Collection<ICloudService> getOnlineServices() {
        return null; //TODO ServiceManager
    }

    @Override
    public Collection<ICloudService> getOnlineServices(ServiceState serviceState) {
        return null; //TODO ServiceManager
    }

    @Override
    public boolean isStatic() {
        return this.staticGroup;
    }

    @Override
    public void setStatic(boolean staticGroup) {
        this.staticGroup = staticGroup;
    }

    @Override
    public FutureAction<Collection<IRBucketHolder<ICloudNode>>> getAssociatedNodes() {
        FutureActionCollection<UUID, IRBucketHolder<ICloudNode>> futureActionCollection = new FutureActionCollection<>();
        for (UUID nodeId : this.associatedNodeIds) {
            futureActionCollection.addToProcess(nodeId, CloudAPI.getInstance().getNodeManager().getNodeAsync(nodeId));
        }
        return futureActionCollection.process().map(HashMap::values);
    }

    @Override
    public void addAssociatedNode(ICloudNode node) {
        this.associatedNodeIds.remove(node.getUniqueId());
    }

    @Override
    public void removeAssociatedNode(ICloudNode node) {
        this.associatedNodeIds.remove(node.getUniqueId());
    }

    @Override
    public boolean hasAssociatedNode(ICloudNode node) {
        return this.associatedNodeIds.contains(node.getUniqueId());
    }

    @Override
    public void setAssociatedNodes(Collection<ICloudNode> nodes) {
        this.associatedNodeIds.clear();
        this.associatedNodeIds.addAll(nodes.stream().map(ICloudNode::getUniqueId).collect(Collectors.toList()));
    }

    @Override
    public Collection<ICloudPlayer> getPlayers() {
        return null; //TODO PlayerManager / serviceManager
    }

    @Override
    public int getPlayersCount() {
        return 0; //TODO playerManager / serviceManager
    }

    @Override
    public IServiceStartConfiguration createServiceConfiguration() {
        return null; // TODO ServiceManager
    }
}
