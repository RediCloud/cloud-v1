package net.suqatri.cloud.node.service.factory;

import lombok.Data;
import net.suqatri.cloud.api.impl.service.CloudService;
import net.suqatri.cloud.api.node.service.factory.ICloudNodeServiceFactory;
import net.suqatri.cloud.api.node.service.factory.ICloudServiceProcess;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.service.ICloudService;
import net.suqatri.cloud.api.service.configuration.IServiceStartConfiguration;
import net.suqatri.cloud.api.service.ServiceState;
import net.suqatri.cloud.commons.function.future.FutureAction;
import net.suqatri.cloud.node.service.NodeCloudServiceManager;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class NodeCloudServiceFactory implements ICloudNodeServiceFactory {

    private final NodeCloudServiceManager serviceManager;
    private final CloudNodePortManager portManager = new CloudNodePortManager();

    private final ConcurrentHashMap<UUID, CloudServiceServiceProcess> processes = new ConcurrentHashMap<>();

    @Override
    public IRBucketHolder<ICloudService> createService(IServiceStartConfiguration configuration) throws Exception{
        for (IRBucketHolder<ICloudService> serviceHolder : this.serviceManager.getServices()) {
            if(serviceHolder.get().getServiceName().equalsIgnoreCase(configuration.getName() + "-" + configuration.getId())) {
                return serviceHolder;
            }
        }
        CloudService cloudService = new CloudService();
        cloudService.setConfiguration(configuration);
        cloudService.setServiceState(ServiceState.STARTING);
        cloudService.setMaxPlayers(50);
        cloudService.setMotd("Welcome to Suqatri Cloud");
        IRBucketHolder<ICloudService> holder = this.serviceManager.createBucket(cloudService.getUniqueId().toString(), cloudService);

        CloudServiceServiceProcess process = new CloudServiceServiceProcess(this, holder);
        process.start();

        return holder;
    }

    @Override
    public FutureAction<IRBucketHolder<ICloudService>> createServiceAsync(IServiceStartConfiguration configuration) {
        FutureAction<IRBucketHolder<ICloudService>> futureAction= new FutureAction<>();

        this.serviceManager.getServicesAsync()
            .onFailure(futureAction)
            .onSuccess(services -> {
                for (IRBucketHolder<ICloudService> serviceHolder : services) {
                    if(serviceHolder.get().getServiceName().equalsIgnoreCase(configuration.getName() + "-" + configuration.getId())){
                        futureAction.completeExceptionally(new IllegalStateException("Service already exists"));
                        return;
                    }
                }
                CloudService cloudService = new CloudService();
                cloudService.setConfiguration(configuration);
                cloudService.setServiceState(ServiceState.PREPARE);
                cloudService.setMaxPlayers(50);
                cloudService.setMotd("Welcome to Suqatri Cloud");
                this.serviceManager.createBucketAsync(cloudService.getUniqueId().toString(), cloudService)
                    .onFailure(futureAction)
                    .onSuccess(serviceHolder -> {

                        CloudServiceServiceProcess process = new CloudServiceServiceProcess(this, serviceHolder);
                        process.startAsync()
                            .onFailure(futureAction)
                            .onSuccess(s -> {
                                futureAction.complete(serviceHolder);
                            });
                    });
            });

        return futureAction;
    }

    @Override
    public boolean destroyService(UUID uniqueId, boolean force) throws IOException {

        ICloudServiceProcess process = this.processes.get(uniqueId);
        if(process == null) throw new NullPointerException("Process not found");

        process.stop(force);

        process.getServiceHolder().get().setServiceState(ServiceState.OFFLINE);
        process.getServiceHolder().get().update();

        this.serviceManager.deleteBucket(process.getServiceHolder().get().getUniqueId().toString());

        return true;
    }

    @Override
    public FutureAction<Boolean> destroyServiceAsync(UUID uniqueId, boolean force) {
        FutureAction<Boolean> futureAction = new FutureAction<>();

        ICloudServiceProcess process = this.processes.get(uniqueId);
        if(process == null) {
            futureAction.completeExceptionally(new NullPointerException("Process not found"));
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
