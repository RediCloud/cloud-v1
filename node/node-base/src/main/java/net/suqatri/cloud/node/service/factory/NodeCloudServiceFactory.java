package net.suqatri.cloud.node.service.factory;

import lombok.Data;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.impl.CloudDefaultAPIImpl;
import net.suqatri.cloud.api.impl.service.CloudService;
import net.suqatri.cloud.api.impl.service.version.CloudServiceVersion;
import net.suqatri.cloud.api.node.service.factory.ICloudNodeServiceFactory;
import net.suqatri.cloud.api.node.service.factory.ICloudServiceProcess;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.redis.event.RedisConnectedEvent;
import net.suqatri.cloud.api.service.ICloudService;
import net.suqatri.cloud.api.service.configuration.IServiceStartConfiguration;
import net.suqatri.cloud.api.service.ServiceState;
import net.suqatri.cloud.api.service.version.ICloudServiceVersion;
import net.suqatri.cloud.commons.function.future.FutureAction;
import net.suqatri.cloud.node.NodeLauncher;
import net.suqatri.cloud.node.service.NodeCloudServiceManager;
import org.redisson.api.RLock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class NodeCloudServiceFactory implements ICloudNodeServiceFactory {

    private final NodeCloudServiceManager serviceManager;
    private final CloudNodePortManager portManager;
    private RLock lock;

    private final ConcurrentHashMap<UUID, CloudServiceProcess> processes;

    public NodeCloudServiceFactory(NodeCloudServiceManager serviceManager) {
        this.serviceManager = serviceManager;
        this.portManager = new CloudNodePortManager();
        this.processes = new ConcurrentHashMap<>();
        CloudAPI.getInstance().getEventManager().register(RedisConnectedEvent.class, event -> this.lock = CloudDefaultAPIImpl.getInstance().getRedisConnection().getClient().getLock("cloud:service-factory:start"));
    }

    @Override
    public IRBucketHolder<ICloudService> createService(IServiceStartConfiguration configuration) throws Exception{

        if(!CloudAPI.getInstance().getServiceVersionManager().existsServiceVersion(configuration.getServiceVersionName())) throw new Exception("Service version " + configuration.getServiceVersionName() + " not found");

        //TODO check for empty node
        configuration.setNodeId(NodeLauncher.getInstance().getNode().getUniqueId());

        IRBucketHolder<ICloudServiceVersion> versionHolder = CloudAPI.getInstance().getServiceVersionManager().getServiceVersion(configuration.getServiceVersionName());
        if(!versionHolder.get().isDownloaded()) versionHolder.getImpl(CloudServiceVersion.class).download();
        if(versionHolder.get().needPatch()) versionHolder.getImpl(CloudServiceVersion.class).patch();

        Collection<IRBucketHolder<ICloudService>> serviceHolders = this.serviceManager.getServices();

        if(configuration.getId() < 1) {
            configuration.setId(this.getNextId(configuration.getName(), this.serviceManager.getServices()));
        }

        for (IRBucketHolder<ICloudService> serviceHolder : serviceHolders) {
            if(serviceHolder.get().getServiceName().equalsIgnoreCase(configuration.getName() + "-" + configuration.getId())) {
                throw new IllegalArgumentException("Service " + configuration.getName() + "-" + configuration.getId() + " already exists");
            }
        }

        CloudService cloudService = new CloudService();
        cloudService.setConfiguration(configuration);
        cloudService.setServiceState(ServiceState.STARTING);
        cloudService.setMaxPlayers(50);
        cloudService.setMotd("Welcome to RediCloud");
        cloudService.setNodeId(NodeLauncher.getInstance().getNode().getUniqueId());
        IRBucketHolder<ICloudService> holder = this.serviceManager.createBucket(cloudService.getUniqueId().toString(), cloudService);

        CloudServiceProcess process = new CloudServiceProcess(this, holder);
        process.start();

        return holder;
    }

    @Override
    public FutureAction<IRBucketHolder<ICloudService>> createServiceAsync(IServiceStartConfiguration configuration) {
        FutureAction<IRBucketHolder<ICloudService>> futureAction= new FutureAction<>();

        System.out.println("sfc1");

        this.serviceManager.getServicesAsync()
            .onFailure(futureAction)
            .onSuccess(serviceHolders -> {

                System.out.println("sfc2");

                if (configuration.getId() < 1) {
                    configuration.setId(this.getNextId(configuration.getName(), serviceHolders));
                }

                for (IRBucketHolder<ICloudService> serviceHolder : serviceHolders) {
                    if (serviceHolder.get().getServiceName().equalsIgnoreCase(configuration.getName() + "-" + configuration.getId())) {
                        futureAction.completeExceptionally(new IllegalArgumentException("Service " + configuration.getName() + "-" + configuration.getId() + " already exists"));
                        return;
                    }
                }

                System.out.println("sfc3");

                CloudAPI.getInstance().getServiceVersionManager().existsServiceVersionAsync(configuration.getServiceVersionName())
                        .onFailure(futureAction)
                        .onSuccess(existVersion -> {

                            System.out.println("sfc4");

                            if(!existVersion){
                                futureAction.completeExceptionally(new IllegalStateException("Service version " + configuration.getServiceVersionName() + " not found"));
                                return;
                            }

                            System.out.println("sfc5");

                            CloudAPI.getInstance().getServiceVersionManager().getServiceVersionAsync(configuration.getServiceVersionName())
                                        .onFailure(futureAction)
                                        .onSuccess(versionHolder -> {
                                            versionHolder.get().getPatchedFileAsync(true)
                                                    .onFailure(futureAction)
                                                    .onSuccess(versionFile -> {
                                                        System.out.println("sfc6");
                                                        CloudService cloudService = new CloudService();
                                                        cloudService.setConfiguration(configuration);
                                                        cloudService.setServiceState(ServiceState.PREPARE);
                                                        cloudService.setMaxPlayers(50);
                                                        cloudService.setMotd("Welcome to RediCloud");
                                                        cloudService.setNodeId(NodeLauncher.getInstance().getNode().getUniqueId());
                                                        this.serviceManager.createBucketAsync(cloudService.getUniqueId().toString(), cloudService)
                                                                .onFailure(futureAction)
                                                                .onSuccess(serviceHolder -> {
                                                                    System.out.println("sfc7");

                                                                    CloudServiceProcess process = new CloudServiceProcess(this, serviceHolder);
                                                                    process.startAsync()
                                                                            .onFailure(futureAction)
                                                                            .onSuccess(s -> {
                                                                                System.out.println("sfc8");
                                                                                futureAction.complete(serviceHolder);
                                                                            });
                                                                });
                                                    });
                                        });
                        });
            });

        return futureAction;
    }

    //TODO fix when service is not correctly started (!this.processes.containsKey(serviceId) | because process is not started but service exists)
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

    //TODO fix when service is not correctly started (!this.processes.containsKey(serviceId) | because process is not started but service exists)
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

    private int getNextId(String groupName, Collection<IRBucketHolder<ICloudService>> servicesHolders){
        int i = 1;
        List<Integer> ids = new ArrayList<>();
        for (IRBucketHolder<ICloudService> serviceHolder : servicesHolders) {
            if(serviceHolder.get().getGroupName().equalsIgnoreCase(groupName)){
                ids.add(serviceHolder.get().getId());
            }
        }
        while (ids.contains(i)) i++;
        return i;
    }

}
