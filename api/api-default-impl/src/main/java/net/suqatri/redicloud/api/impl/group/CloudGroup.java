package net.suqatri.redicloud.api.impl.group;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.group.ICloudGroup;
import net.suqatri.redicloud.api.impl.redis.bucket.RBucketObject;
import net.suqatri.redicloud.api.impl.service.configuration.GroupServiceStartConfiguration;
import net.suqatri.redicloud.api.node.ICloudNode;
import net.suqatri.redicloud.api.player.ICloudPlayer;
import net.suqatri.redicloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.api.service.ServiceEnvironment;
import net.suqatri.redicloud.api.service.ServiceState;
import net.suqatri.redicloud.api.service.configuration.IServiceStartConfiguration;
import net.suqatri.redicloud.api.service.version.ICloudServiceVersion;
import net.suqatri.redicloud.api.template.ICloudServiceTemplate;
import net.suqatri.redicloud.commons.function.future.FutureAction;
import net.suqatri.redicloud.commons.function.future.FutureActionCollection;

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
    private int minServices = 0;
    private int maxServices = -1;
    private Collection<String> templateNames = new ArrayList<>();
    private boolean staticGroup = false;
    private Collection<UUID> associatedNodeIds = new ArrayList<>();
    private int startPort;
    private boolean maintenance = false;
    private int maxMemory;
    private int startPriority = 0;
    private String serviceVersionName;
    private boolean fallback = false;

    @Override
    public FutureAction<IRBucketHolder<ICloudServiceVersion>> getServiceVersion() {
        return CloudAPI.getInstance().getServiceVersionManager().getServiceVersionAsync(this.serviceVersionName);
    }

    @Override
    public void setServiceVersion(IRBucketHolder<ICloudServiceVersion> serviceVersion) {
        this.serviceVersionName = serviceVersion.get().getName();
    }

    @Override
    public FutureAction<Collection<IRBucketHolder<ICloudServiceTemplate>>> getTemplates() {
        FutureAction<Collection<IRBucketHolder<ICloudServiceTemplate>>> futureAction = new FutureAction<>();

        FutureActionCollection<String, IRBucketHolder<ICloudServiceTemplate>> futureActionCollection = new FutureActionCollection<>();
        for (String templateName : templateNames) {
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
    public void addTemplate(IRBucketHolder<ICloudServiceTemplate> template) {
        this.templateNames.add(template.get().getName());
    }

    @Override
    public void removeTemplate(IRBucketHolder<ICloudServiceTemplate> template) {
        this.templateNames.remove(template.get().getName());
    }

    @Override
    public boolean hasTemplate(IRBucketHolder<ICloudServiceTemplate> template) {
        return this.templateNames.contains(template.get().getName());
    }

    @Override
    public FutureAction<Integer> getOnlineServiceCount() {
        return CloudAPI.getInstance().getServiceManager().readAllFetcherKeysAsync()
                .map(names -> (int) names
                        .parallelStream()
                        .filter(name -> name.startsWith(this.getName().toLowerCase() + "-"))
                        .count());
    }

    @Override
    public boolean hasTemplate(String templateName) {
        return this.templateNames.contains(templateName);
    }

    @Override
    public FutureAction<Integer> getServicesCount(ServiceState serviceState) {
        return CloudAPI.getInstance().getServiceManager().getServicesAsync()
                .map(services -> (int) services
                        .parallelStream()
                        .filter(service -> service.get().getServiceState() == serviceState)
                        .count());
    }

    @Override
    public FutureAction<Collection<IRBucketHolder<ICloudService>>> getOnlineServices() {
        return CloudAPI.getInstance().getServiceManager().getServicesAsync()
                .map(holders -> holders
                        .parallelStream()
                        .filter(holder -> holder.get().getGroup() != null)
                        .filter(holder -> holder.get().getGroupName().equals(this.name))
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

    @JsonIgnore
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
    public void setAssociatedNodes(Collection<ICloudNode> nodes) {
        this.associatedNodeIds.clear();
        this.associatedNodeIds.addAll(nodes.stream().map(ICloudNode::getUniqueId).collect(Collectors.toList()));
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

    //TODO improve this methode
    @Override
    public FutureAction<Collection<IRBucketHolder<ICloudPlayer>>> getPlayers() {
        FutureAction<Collection<IRBucketHolder<ICloudPlayer>>> futureAction = new FutureAction<>();

        CloudAPI.getInstance().getPlayerManager().getConnectedPlayers()
                .onFailure(futureAction)
                .onSuccess(players -> {
                    FutureActionCollection<UUID, IRBucketHolder<ICloudService>> futureActionCollection = new FutureActionCollection<>();
                    for (IRBucketHolder<ICloudPlayer> player : players) {
                        futureActionCollection.addToProcess(player.get().getUniqueId(), CloudAPI.getInstance().getServiceManager().getServiceAsync(player.get().getLastConnectedServerId()));
                    }
                    futureActionCollection.process()
                            .onFailure(futureAction)
                            .onSuccess(playersServers -> {
                                List<IRBucketHolder<ICloudPlayer>> list = new ArrayList<>();
                                for (IRBucketHolder<ICloudPlayer> player : players) {
                                    if (!playersServers.get(player.get().getUniqueId()).get().getName().equals(this.name))
                                        continue;
                                    list.add(player);
                                }
                                futureAction.complete(list);
                            });
                });

        return futureAction;
    }

    //TODO improve this methode
    @Override
    public FutureAction<Integer> getPlayersCount() {
        return getOnlineServices().map(services -> {
            int count = 0;
            for (IRBucketHolder<ICloudService> service : services) {
                if (service.get().getName().equals(this.name)) count++;
            }
            return count;
        });
    }

    @Override
    public IServiceStartConfiguration createServiceConfiguration() {
        return new GroupServiceStartConfiguration().applyFromGroup(this.getHolder());
    }
}
