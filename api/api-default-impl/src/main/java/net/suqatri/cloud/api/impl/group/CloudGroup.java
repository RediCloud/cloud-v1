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
import net.suqatri.cloud.api.service.configuration.IServiceStartConfiguration;
import net.suqatri.cloud.api.service.ServiceEnvironment;
import net.suqatri.cloud.api.service.ServiceState;
import net.suqatri.cloud.api.template.ICloudServiceTemplate;
import net.suqatri.cloud.commons.function.future.FutureAction;
import net.suqatri.cloud.commons.function.future.FutureActionCollection;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public class CloudGroup extends RBucketObject implements ICloudGroup {

    private ServiceEnvironment serviceEnvironment;
    private UUID uniqueId;
    private String name;
    private String[] processParameters = new String[0];
    private String[] jvmArguments = new String[0];
    private String javaCommand = "java";
    private int minServices = 0;
    private int maxServices = -1;
    private Collection<String> templateNames = new ArrayList<>();
    private boolean staticGroup = false;
    private Collection<UUID> associatedNodeIds = new ArrayList<>();
    private int startPort = 5200;
    private boolean maintenance = false;
    private int maxMemory;
    private int startPriority = 0;

    @Override
    public FutureAction<Collection<IRBucketHolder<ICloudServiceTemplate>>> getTemplates() {
        FutureAction<Collection<IRBucketHolder<ICloudServiceTemplate>>> futureAction = new FutureAction<>();

        FutureActionCollection<String, IRBucketHolder<ICloudServiceTemplate>> futureActionCollection = new FutureActionCollection<>();
        for(String templateName : templateNames) {
            futureActionCollection.addToProcess(templateName, CloudAPI.getInstance().getServiceTemplateManager().getTemplateAsync(templateName));
        }
        futureActionCollection.process()
                .onFailure(futureAction)
                .onSuccess(templates -> {
                    futureAction.complete(templates.values());
                });

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
    public FutureAction<Collection<IRBucketHolder<ICloudService>>> getOnlineServices() {
        return CloudAPI.getInstance().getServiceManager().getServicesAsync()
                .map(holders -> holders
                        .parallelStream()
                        .filter(holder -> holder.get().getGroup() != null)
                        .filter(holder -> holder.get().getGroup().get().getUniqueId().equals(this.uniqueId))
                        .collect(Collectors.toList())
                );
    }

    @Override
    public FutureAction<Collection<IRBucketHolder<ICloudService>>> getOnlineServices(ServiceState serviceState) {
        return getOnlineServices()
                .map(holders -> holders
                        .parallelStream()
                        .filter(holder -> holder.get().getServiceState() == serviceState)
                        .collect(Collectors.toList()));
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
