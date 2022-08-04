package net.suqatri.redicloud.node.template;

import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.group.ICloudGroup;
import net.suqatri.redicloud.api.impl.template.CloudServiceTemplate;
import net.suqatri.redicloud.api.impl.template.CloudServiceTemplateManager;
import net.suqatri.redicloud.api.network.NetworkComponentType;
import net.suqatri.redicloud.api.node.ICloudNode;
import net.suqatri.redicloud.api.node.file.event.FilePulledTemplatesEvent;
import net.suqatri.redicloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.redicloud.api.template.ICloudServiceTemplate;
import net.suqatri.redicloud.api.utils.Files;
import net.suqatri.redicloud.commons.function.future.FutureAction;
import net.suqatri.redicloud.commons.function.future.FutureActionCollection;
import net.suqatri.redicloud.node.NodeLauncher;
import net.suqatri.redicloud.node.file.packet.FileDeletePacket;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class NodeCloudServiceTemplateManager extends CloudServiceTemplateManager {

    public IRBucketHolder<ICloudServiceTemplate> createTemplate(String name) {
        CloudServiceTemplate template = new CloudServiceTemplate();
        template.setName(name);
        File file = new File(Files.TEMPLATE_FOLDER.getFile(), name);
        if (!file.exists()) {
            file.mkdirs();
        }
        return this.createBucket(name.toLowerCase(), template);
    }

    public FutureAction<IRBucketHolder<ICloudServiceTemplate>> createTemplateAsync(String name) {
        FutureAction<IRBucketHolder<ICloudServiceTemplate>> futureAction = new FutureAction<>();
        CloudServiceTemplate template = new CloudServiceTemplate();
        template.setName(name);
        File file = new File(Files.TEMPLATE_FOLDER.getFile(), name);
        if (!file.exists()) {
            file.mkdirs();
        }
        this.createBucketAsync(name.toLowerCase(), template)
                .onFailure(futureAction)
                .onSuccess(holder -> {
                    pushTemplate(holder)
                            .onFailure(futureAction)
                            .onSuccess(r -> {
                                futureAction.complete(holder);
                            });
                });
        return futureAction;
    }

    public FutureAction<Boolean> deleteTemplateAsync(String name) {
        FutureAction<Boolean> futureAction = new FutureAction<>();
        getTemplateAsync(name)
                .onFailure(futureAction)
                .onSuccess(template -> {
                    CloudAPI.getInstance().getGroupManager().getGroupsAsync()
                            .onFailure(futureAction)
                            .onSuccess(groupHolders -> {
                                for (IRBucketHolder<ICloudGroup> groupHolder : groupHolders) {
                                    groupHolder.get().getTemplateNames().remove(template.get().getName());
                                    groupHolder.get().updateAsync();
                                }
                                File file = template.get().getTemplateFolder();
                                if (file.exists()) {
                                    try {
                                        FileUtils.deleteDirectory(file);
                                    } catch (IOException e) {
                                        futureAction.completeExceptionally(e);
                                        return;
                                    }
                                }
                                FileDeletePacket packet = new FileDeletePacket();
                                packet.setPath(file.getPath());
                                packet.publishAllAsync(NetworkComponentType.NODE);
                                this.deleteBucketAsync(name.toLowerCase())
                                        .onFailure(futureAction)
                                        .onSuccess(r -> futureAction.complete(true));
                            });
                });
        return futureAction;
    }

    public FutureAction<IRBucketHolder<ICloudServiceTemplate>> pushTemplate(IRBucketHolder<ICloudServiceTemplate> template, IRBucketHolder<ICloudNode> nodeHolder) {
        FutureAction<IRBucketHolder<ICloudServiceTemplate>> futureAction = new FutureAction<>();
        if (!nodeHolder.get().isConnected()) {
            futureAction.completeExceptionally(new NullPointerException("Cloud node not connected!"));
            return futureAction;
        }
        return NodeLauncher.getInstance().getFileTransferManager().transferFolderToNode(
                        template.get().getTemplateFolder(),
                        Files.TEMPLATE_FOLDER.getFile(),
                        new File(Files.TEMPLATE_FOLDER.getFile(), template.get().getName()).getPath(),
                        nodeHolder)
                .map(r -> template);
    }

    public FutureAction<IRBucketHolder<ICloudServiceTemplate>> pushTemplate(IRBucketHolder<ICloudServiceTemplate> template) {
        FutureAction<IRBucketHolder<ICloudServiceTemplate>> futureAction = new FutureAction<>();
        CloudAPI.getInstance().getNodeManager().getNodesAsync()
                .onFailure(futureAction)
                .onSuccess(holders -> {
                    FutureActionCollection<UUID, IRBucketHolder<ICloudServiceTemplate>> collection = new FutureActionCollection<>();
                    for (IRBucketHolder<ICloudNode> holder : holders) {
                        if (!holder.get().isConnected()) continue;
                        if (holder.get().getUniqueId().equals(NodeLauncher.getInstance().getNode().getUniqueId()))
                            continue;
                        collection.addToProcess(holder.get().getUniqueId(), pushTemplate(template, holder));
                    }
                    collection.process()
                            .onFailure(futureAction)
                            .onSuccess(r -> {
                                futureAction.complete(template);
                            });
                });
        return futureAction;
    }

    public FutureAction<IRBucketHolder<ICloudNode>> pushAllTemplates(IRBucketHolder<ICloudNode> nodeHolder) {
        return NodeLauncher.getInstance().getFileTransferManager().transferFolderToNode(
                        Files.TEMPLATE_FOLDER.getFile(),
                        Files.CLOUD_FOLDER.getFile(),
                        nodeHolder.get().getFilePath(Files.TEMPLATE_FOLDER),
                        nodeHolder)
                .map(r -> nodeHolder);
    }

    public FutureAction<Boolean> pullTemplates(IRBucketHolder<ICloudNode> nodeHolder) {
        FutureAction<Boolean> futureAction =
                new FutureAction<>(NodeLauncher.getInstance().getFileTransferManager()
                        .pullFile(nodeHolder.get().getFilePath(Files.TEMPLATE_FOLDER)
                                , Files.CLOUD_FOLDER.getFile()
                                , Files.TEMPLATE_FOLDER.getFile(), nodeHolder));

        futureAction.onFailure(t ->
                        CloudAPI.getInstance().getEventManager().postLocal(new FilePulledTemplatesEvent(nodeHolder.get().getUniqueId(), false)))
                .onSuccess(b ->
                        CloudAPI.getInstance().getEventManager().postLocal(new FilePulledTemplatesEvent(nodeHolder.get().getUniqueId(), true)));

        return futureAction;
    }

    public FutureAction<Boolean> pullTemplatesFromCluster() {
        FutureAction<Boolean> futureAction = new FutureAction<>();
        CloudAPI.getInstance().getNodeManager().getNodesAsync()
                .onFailure(futureAction)
                .onSuccess(holders -> {
                    if (holders.size() < 2) return;
                    IRBucketHolder<ICloudNode> targetNodeHolder = null;
                    for (IRBucketHolder<ICloudNode> holder : holders) {
                        if (!holder.get().isConnected()) continue;
                        if (holder.get().getNetworkComponentInfo().equals(CloudAPI.getInstance().getNetworkComponentInfo()))
                            continue;
                        if (targetNodeHolder == null) targetNodeHolder = holder;
                        else if (targetNodeHolder.get().getUpTime() < holder.get().getUpTime())
                            targetNodeHolder = holder;
                    }
                    if (targetNodeHolder == null) return;
                    pullTemplates(targetNodeHolder)
                            .onFailure(futureAction)
                            .onSuccess(r -> futureAction.complete(true));
                });

        return futureAction;
    }

}
