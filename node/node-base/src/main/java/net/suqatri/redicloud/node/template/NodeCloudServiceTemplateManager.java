package net.suqatri.redicloud.node.template;

import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.group.ICloudGroup;
import net.suqatri.redicloud.api.impl.template.CloudServiceTemplate;
import net.suqatri.redicloud.api.impl.template.CloudServiceTemplateManager;
import net.suqatri.redicloud.api.network.NetworkComponentType;
import net.suqatri.redicloud.api.node.ICloudNode;
import net.suqatri.redicloud.api.node.file.event.FilePulledTemplatesEvent;
import net.suqatri.redicloud.api.packet.PacketChannel;
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

    public ICloudServiceTemplate createTemplate(String name) {
        CloudServiceTemplate template = new CloudServiceTemplate();
        template.setName(name);
        File file = new File(Files.TEMPLATE_FOLDER.getFile(), name);
        if (!file.exists()) {
            file.mkdirs();
        }
        return this.createBucket(name.toLowerCase(), template);
    }

    public FutureAction<ICloudServiceTemplate> createTemplateAsync(String name) {
        FutureAction<ICloudServiceTemplate> futureAction = new FutureAction<>();
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
                            .onSuccess(groups -> {
                                for (ICloudGroup group : groups) {
                                    group.getTemplateNames().remove(template.getName());
                                    group.updateAsync();
                                }
                                File file = template.getTemplateFolder();
                                if (file.exists()) {
                                    try {
                                        FileUtils.deleteDirectory(file);
                                    } catch (IOException e) {
                                        futureAction.completeExceptionally(e);
                                        return;
                                    }
                                }
                                FileDeletePacket packet = new FileDeletePacket();
                                packet.getPacketData().setChannel(PacketChannel.NODE);
                                packet.setPath(file.getPath());
                                packet.publishAllAsync(NetworkComponentType.NODE);
                                this.deleteBucketAsync(name.toLowerCase())
                                        .onFailure(futureAction)
                                        .onSuccess(r -> futureAction.complete(true));
                            });
                });
        return futureAction;
    }

    public FutureAction<ICloudServiceTemplate> pushTemplate(ICloudServiceTemplate template, ICloudNode node) {
        FutureAction<ICloudServiceTemplate> futureAction = new FutureAction<>();
        if (!node.isConnected()) {
            futureAction.completeExceptionally(new NullPointerException("Cloud node not connected!"));
            return futureAction;
        }
        return NodeLauncher.getInstance().getFileTransferManager().transferFolderToNode(
                        template.getTemplateFolder(),
                        Files.TEMPLATE_FOLDER.getFile(),
                        new File(Files.TEMPLATE_FOLDER.getFile(), template.getName()).getPath(),
                        node)
                .map(r -> template);
    }

    public FutureAction<ICloudServiceTemplate> pushTemplate(ICloudServiceTemplate template) {
        FutureAction<ICloudServiceTemplate> futureAction = new FutureAction<>();
        CloudAPI.getInstance().getNodeManager().getNodesAsync()
                .onFailure(futureAction)
                .onSuccess(holders -> {
                    FutureActionCollection<UUID, ICloudServiceTemplate> collection = new FutureActionCollection<>();
                    for (ICloudNode holder : holders) {
                        if (!holder.isConnected()) continue;
                        if (holder.getUniqueId().equals(NodeLauncher.getInstance().getNode().getUniqueId()))
                            continue;
                        collection.addToProcess(holder.getUniqueId(), pushTemplate(template, holder));
                    }
                    collection.process()
                            .onFailure(futureAction)
                            .onSuccess(r -> {
                                futureAction.complete(template);
                            });
                });
        return futureAction;
    }

    public FutureAction<ICloudNode> pushAllTemplates(ICloudNode node) {
        return NodeLauncher.getInstance().getFileTransferManager().transferFolderToNode(
                        Files.TEMPLATE_FOLDER.getFile(),
                        Files.CLOUD_FOLDER.getFile(),
                        node.getFilePath(Files.TEMPLATE_FOLDER),
                        node)
                .map(r -> node);
    }

    public FutureAction<Boolean> pullTemplates(ICloudNode node) {
        FutureAction<Boolean> futureAction =
                new FutureAction<>(NodeLauncher.getInstance().getFileTransferManager()
                        .pullFile(node.getFilePath(Files.TEMPLATE_FOLDER)
                                , Files.CLOUD_FOLDER.getFile()
                                , Files.TEMPLATE_FOLDER.getFile(), node));

        futureAction.onFailure(t ->
                        CloudAPI.getInstance().getEventManager().postLocal(new FilePulledTemplatesEvent(node.getUniqueId(), false)))
                .onSuccess(b ->
                        CloudAPI.getInstance().getEventManager().postLocal(new FilePulledTemplatesEvent(node.getUniqueId(), true)));

        return futureAction;
    }

    public FutureAction<Boolean> pullTemplatesFromCluster() {
        FutureAction<Boolean> futureAction = new FutureAction<>();
        CloudAPI.getInstance().getNodeManager().getNodesAsync()
                .onFailure(futureAction)
                .onSuccess(holders -> {
                    if (holders.size() < 2) return;
                    ICloudNode targetNode = null;
                    for (ICloudNode holder : holders) {
                        if (!holder.isConnected()) continue;
                        if (holder.getNetworkComponentInfo().equals(CloudAPI.getInstance().getNetworkComponentInfo()))
                            continue;
                        if (targetNode == null) targetNode = holder;
                        else if (targetNode.getUpTime() < holder.getUpTime())
                            targetNode = holder;
                    }
                    if (targetNode == null) return;
                    pullTemplates(targetNode)
                            .onFailure(futureAction)
                            .onSuccess(r -> futureAction.complete(true));
                });

        return futureAction;
    }

}
