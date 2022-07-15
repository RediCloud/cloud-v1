package net.suqatri.cloud.node.template;

import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.group.ICloudGroup;
import net.suqatri.cloud.api.impl.template.CloudServiceTemplate;
import net.suqatri.cloud.api.impl.template.CloudServiceTemplateManager;
import net.suqatri.cloud.api.network.NetworkComponentType;
import net.suqatri.cloud.api.node.ICloudNode;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.template.ICloudServiceTemplate;
import net.suqatri.cloud.api.utils.Files;
import net.suqatri.cloud.commons.function.future.FutureAction;
import net.suqatri.cloud.commons.function.future.FutureActionCollection;
import net.suqatri.cloud.node.NodeLauncher;
import net.suqatri.cloud.node.file.packet.FileDeletePacket;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class NodeCloudServiceTemplateManager extends CloudServiceTemplateManager {

    public IRBucketHolder<ICloudServiceTemplate> createTemplate(String name) {
        CloudServiceTemplate template = new CloudServiceTemplate();
        template.setName(name);
        File file = new File(Files.TEMPLATE_FOLDER.getFile(), name);
        if(!file.exists()){
            file.mkdirs();
        }
        return this.createBucket(name, template);
    }

    public FutureAction<IRBucketHolder<ICloudServiceTemplate>> createTemplateAsync(String name) {
        FutureAction<IRBucketHolder<ICloudServiceTemplate>> futureAction = new FutureAction<>();
        CloudServiceTemplate template = new CloudServiceTemplate();
        template.setName(name);
        File file = new File(Files.TEMPLATE_FOLDER.getFile(), name);
        if(!file.exists()){
            file.mkdirs();
        }
        System.out.println("c4------");
        this.createBucketAsync(name, template)
                .onFailure(futureAction)
                .onSuccess(holder -> {
                    System.out.println("c5------");
                    pushTemplate(holder)
                            .onFailure(futureAction)
                            .onSuccess(r -> {
                                System.out.println("c6------");
                                futureAction.complete(holder);
                            });
                });
        return futureAction;
    }

    public FutureAction<Boolean> deleteTemplateAsync(String name) {
        System.out.println("d1----");
        FutureAction<Boolean> futureAction = new FutureAction<>();
        getTemplateAsync(name)
                .onFailure(futureAction)
                .onSuccess(template -> {
                    System.out.println("d2----");
                    CloudAPI.getInstance().getGroupManager().getGroupsAsync()
                            .onFailure(futureAction)
                            .onSuccess(groupHolders -> {
                                System.out.println("d3----");
                                for (IRBucketHolder<ICloudGroup> groupHolder : groupHolders) {
                                    groupHolder.get().getTemplateNames().remove(template.get().getName());
                                    groupHolder.get().updateAsync();
                                }
                                System.out.println("d4----");
                                File file = template.get().getTemplateFolder();
                                if(file.exists()) {
                                    try {
                                        FileUtils.deleteDirectory(file);
                                        System.out.println("d5----");
                                    } catch (IOException e) {
                                        futureAction.completeExceptionally(e);
                                        System.out.println("d6----");
                                        return;
                                    }
                                }
                                System.out.println("d7----");
                                FileDeletePacket packet = new FileDeletePacket();
                                packet.setPath(file.getPath());
                                packet.publishAllAsync(NetworkComponentType.NODE);
                                System.out.println("d8----");
                                this.deleteBucketAsync(name)
                                        .onFailure(futureAction)
                                        .onSuccess(r -> futureAction.complete(true));
                            });
                });
        return futureAction;
    }

    public FutureAction<IRBucketHolder<ICloudServiceTemplate>> pushTemplate(IRBucketHolder<ICloudServiceTemplate> template, IRBucketHolder<ICloudNode> nodeHolder){
        FutureAction<IRBucketHolder<ICloudServiceTemplate>> futureAction = new FutureAction<>();
        System.out.println("c15------");
        if(!nodeHolder.get().isConnected()){
            futureAction.completeExceptionally(new NullPointerException("Cloud node not connected!"));
            return futureAction;
        }
        System.out.println("c16------");
        return NodeLauncher.getInstance().getFileTransferManager().transferFolderToNode(
                template.get().getTemplateFolder(),
                Files.TEMPLATE_FOLDER.getFile(),
                new File(Files.TEMPLATE_FOLDER.getFile(), template.get().getName()).getPath(),
                nodeHolder)
                .map(r -> template);
    }

    public FutureAction<IRBucketHolder<ICloudServiceTemplate>> pushTemplate(IRBucketHolder<ICloudServiceTemplate> template){
        FutureAction<IRBucketHolder<ICloudServiceTemplate>> futureAction = new FutureAction<>();
        System.out.println("c10------");
        CloudAPI.getInstance().getNodeManager().getNodesAsync()
                .onFailure(futureAction)
                .onSuccess(holders -> {
                    System.out.println("c11------");
                    FutureActionCollection<UUID, IRBucketHolder<ICloudServiceTemplate>> collection = new FutureActionCollection<>();
                    for(IRBucketHolder<ICloudNode> holder : holders){
                        if(!holder.get().isConnected()) continue;
                        if(holder.get().getUniqueId().equals(NodeLauncher.getInstance().getNode().getUniqueId())) continue;
                        collection.addToProcess(holder.get().getUniqueId(), pushTemplate(template, holder));
                    }
                    System.out.println("c12------");
                    collection.process()
                            .onFailure(futureAction)
                            .onSuccess(r -> {
                                System.out.println("c13------");
                                    futureAction.complete(template);
                            });
                });
        return futureAction;
    }

    public FutureAction<IRBucketHolder<ICloudNode>> pushAllTemplates(IRBucketHolder<ICloudNode> nodeHolder){
        return NodeLauncher.getInstance().getFileTransferManager().transferFolderToNode(
                Files.TEMPLATE_FOLDER.getFile(),
                Files.CLOUD_FOLDER.getFile(),
                nodeHolder.get().getFilePath(Files.TEMPLATE_FOLDER),
                nodeHolder)
                .map(r -> nodeHolder);
    }

    public FutureAction<Boolean> pullTemplates(IRBucketHolder<ICloudNode> nodeHolder){
        return NodeLauncher.getInstance().getFileTransferManager().pullFile(nodeHolder.get().getFilePath(Files.TEMPLATE_FOLDER), Files.CLOUD_FOLDER.getFile(), Files.TEMPLATE_FOLDER.getFile(), nodeHolder);
    }

    public FutureAction<Boolean> pullTemplatesFromCluster(){
        FutureAction<Boolean> futureAction = new FutureAction<>();
        CloudAPI.getInstance().getNodeManager().getNodesAsync()
                .onFailure(futureAction)
                .onSuccess(holders -> {
                    if(holders.size() < 2) return;
                    IRBucketHolder<ICloudNode> targetNodeHolder = null;
                    for (IRBucketHolder<ICloudNode> holder : holders) {
                        if(!holder.get().isConnected()) continue;
                        if(holder.get().getNetworkComponentInfo().equals(CloudAPI.getInstance().getNetworkComponentInfo())) continue;
                        if(targetNodeHolder == null) targetNodeHolder = holder;
                        else if(targetNodeHolder.get().getUpTime() < holder.get().getUpTime()) targetNodeHolder = holder;
                    }
                    if(targetNodeHolder == null) return;
                    pullTemplates(targetNodeHolder)
                            .onFailure(futureAction)
                            .onSuccess(r -> futureAction.complete(true));
                });

        return futureAction;
    }

}
