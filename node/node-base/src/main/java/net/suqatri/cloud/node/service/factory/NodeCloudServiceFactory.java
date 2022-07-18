package net.suqatri.cloud.node.service.factory;

import jdk.nashorn.internal.runtime.options.Option;
import lombok.Data;
import net.suqatri.cloud.api.node.service.factory.ICloudNodeServiceFactory;
import net.suqatri.cloud.api.node.service.factory.ICloudServiceProcess;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.service.ICloudService;
import net.suqatri.cloud.api.service.configuration.IServiceStartConfiguration;
import net.suqatri.cloud.api.service.ServiceState;
import net.suqatri.cloud.commons.function.future.FutureAction;
import net.suqatri.cloud.node.service.NodeCloudServiceManager;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class NodeCloudServiceFactory implements ICloudNodeServiceFactory {

    private final NodeCloudServiceManager serviceManager;
    private final CloudNodePortManager portManager;
    private final CloudNodeServiceThread thread;

    public NodeCloudServiceFactory(NodeCloudServiceManager serviceManager) {
        this.serviceManager = serviceManager;
        this.portManager = new CloudNodePortManager();
        this.thread = new CloudNodeServiceThread(this);
        //TODO start after template is ready
        this.thread.start();
    }

    public FutureAction<IRBucketHolder<ICloudService>> queueService(IServiceStartConfiguration configuration) {
        this.thread.getQueue().add(configuration);
        configuration.listenToStart();
        return configuration.getStartListener();
    }

    //TODO fix when service is not correctly started (!this.processes.containsKey(serviceId) | because process is not started but service exists)
    @Override
    public boolean destroyService(UUID uniqueId, boolean force) throws IOException {

        ICloudServiceProcess process = this.thread.getProcesses().get(uniqueId);
        if(process == null) {
            this.thread.getWaitForRemove().add(uniqueId);
            return true;
        }

        process.stop(force);

        process.getServiceHolder().get().setServiceState(ServiceState.OFFLINE);
        process.getServiceHolder().get().update();

        this.serviceManager.deleteBucket(process.getServiceHolder().get().getUniqueId().toString());
        return true;
    }

    //TODO fix when service is not correctly started (!this.processes.containsKey(serviceId) | because process is not started but service exists)
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
                        this.serviceManager.deleteBucketAsync(process.getServiceHolder().get().getUniqueId().toString())
                                .onFailure(futureAction)
                                .onSuccess(v -> futureAction.complete(true));
                    });
            });

        return futureAction;
    }

}
