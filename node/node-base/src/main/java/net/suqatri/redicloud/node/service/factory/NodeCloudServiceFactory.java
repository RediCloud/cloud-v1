package net.suqatri.redicloud.node.service.factory;

import lombok.Data;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.node.file.event.FilePulledTemplatesEvent;
import net.suqatri.redicloud.api.node.service.factory.ICloudNodeServiceFactory;
import net.suqatri.redicloud.api.node.service.factory.ICloudServiceProcess;
import net.suqatri.redicloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.api.service.ServiceState;
import net.suqatri.redicloud.api.service.configuration.IServiceStartConfiguration;
import net.suqatri.redicloud.commons.function.future.FutureAction;
import net.suqatri.redicloud.node.NodeLauncher;
import net.suqatri.redicloud.node.service.NodeCloudServiceManager;

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
                .onSuccess(holder -> this.serviceManager.putInFetcher(holder.get().getServiceName(), holder.get().getUniqueId()));
        return configuration.getStartListener();
    }

    @Override
    public FutureAction<Boolean> destroyServiceAsync(UUID uniqueId, boolean force) {
        FutureAction<Boolean> futureAction = new FutureAction<>();

        //TODO check other nodes if this service is running

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
                        if(process.getServiceHolder().get().isStatic()) return;
                        this.serviceManager.removeFromFetcher(process.getServiceHolder().get().getServiceName());
                        this.serviceManager.deleteBucketAsync(process.getServiceHolder().get().getUniqueId().toString())
                                .onFailure(futureAction)
                                .onSuccess(v -> futureAction.complete(true));
                    });
            });

        return futureAction;
    }

}
