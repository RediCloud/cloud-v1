package net.suqatri.cloud.node.template;

import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.impl.template.CloudServiceTemplate;
import net.suqatri.cloud.api.impl.template.CloudServiceTemplateManager;
import net.suqatri.cloud.api.node.ICloudNode;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.template.ICloudServiceTemplate;
import net.suqatri.cloud.commons.file.Files;
import net.suqatri.cloud.commons.function.future.FutureAction;
import net.suqatri.cloud.commons.function.future.FutureActionCollection;
import net.suqatri.cloud.node.NodeLauncher;

import java.io.File;
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
        this.createBucketAsync(name, template)
                .onFailure(futureAction)
                .onSuccess(holder -> pushTemplate(template)
                        .onFailure(futureAction)
                        .onSuccess(r -> futureAction.complete(holder)));
        return futureAction;
    }

    public FutureAction<ICloudServiceTemplate> pushTemplate(ICloudServiceTemplate template, ICloudNode cloudNode){
        return NodeLauncher.getInstance().getFileTransferManager().transferFolderToNode(template.getTemplateFolder(),
                Files.TEMPLATE_FOLDER.getFile(),
                cloudNode.getNetworkComponentInfo())
                .map(r -> template);
    }

    public FutureAction<ICloudServiceTemplate> pushTemplate(ICloudServiceTemplate template){
        FutureAction<ICloudServiceTemplate> futureAction = new FutureAction<>();

        CloudAPI.getInstance().getNodeManager().getNodesAsync()
                .onFailure(futureAction)
                .onSuccess(holders -> {
                    FutureActionCollection<UUID, ICloudServiceTemplate> collection = new FutureActionCollection<>();
                    for(IRBucketHolder<ICloudNode> holder : holders){
                        collection.addToProcess(holder.get().getUniqueId(), pushTemplate(template, holder.get()));
                    }
                    collection.process()
                            .onFailure(futureAction)
                            .onSuccess(r -> futureAction.complete(template));
                });

        return futureAction;
    }

}
