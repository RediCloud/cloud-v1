package net.suqatri.cloud.node.service.factory;

import lombok.Data;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.node.file.event.FilePulledTemplatesEvent;
import net.suqatri.cloud.api.node.service.factory.ICloudNodeServiceFactory;
import net.suqatri.cloud.api.node.service.factory.ICloudServiceProcess;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.service.ICloudService;
import net.suqatri.cloud.api.service.configuration.IServiceStartConfiguration;
import net.suqatri.cloud.api.service.ServiceState;
import net.suqatri.cloud.commons.function.future.FutureAction;
import net.suqatri.cloud.node.NodeLauncher;
import net.suqatri.cloud.node.service.NodeCloudServiceManager;

import java.util.UUID;

@Data
public class NodeCloudServiceFactory implements ICloudNodeServiceFactory {

    private final NodeCloudServiceManager serviceManager;
    private final CloudNodePortManager portManager;
    private final CloudNodeServiceThread thread;

    public NodeCloudServiceFactory(NodeCloudServiceManager serviceManager) {
        this.serviceManager = serviceManager;
        this.portManager = new CloudNodePortManager();
        this.thread = new CloudNodeServiceThread(this);
        if(NodeLauncher.getInstance().isFirstTemplatePulled()){
            this.thread.start();
        }else{
            CloudAPI.getInstance().getEventManager().register(FilePulledTemplatesEvent.class, event -> this.thread.start());
        }
    }

    public FutureAction<IRBucketHolder<ICloudService>> queueService(IServiceStartConfiguration configuration) {
        this.thread.getQueue().add(configuration);
        configuration.listenToStart();
        configuration.getStartListener()
                .onSuccess(holder -> this.serviceManager.getServiceIdFetcherMap()
                        .putAsync(holder.get().getServiceName(), holder.get().getUniqueId()));
        return configuration.getStartListener();
    }

    @Override
    public FutureAction<Boolean> destroyServiceAsync(UUID uniqueId, boolean force) {
        FutureAction<Boolean> futureAction = new FutureAction<>();

        ICloudServiceProcess process = this.thread.getProcesses().get(uniqueId);
        if(process == null) {
            this.thread.getWaitForRemove().add(uniqueId);
            futureAction.complete(true);
            return futureAction;
        }

        process.stopAsync(force)
            .onFailure(futureAction)
            .onSuccess(b -> {
                process.getServiceHolder().get().setServiceState(ServiceState.OFFLINE);
                process.getServiceHolder().get().updateAsync()
                    .onFailure(futureAction)
                    .onSuccess(s -> {
                        this.serviceManager.getServiceIdFetcherMap()
                                .removeAsync(process.getServiceHolder().get().getServiceName(),
                                        process.getServiceHolder().get().getUniqueId());
                        this.serviceManager.deleteBucketAsync(process.getServiceHolder().get().getUniqueId().toString())
                                .onFailure(futureAction)
                                .onSuccess(v -> futureAction.complete(true));
                    });
            });

        return futureAction;
    }

}
