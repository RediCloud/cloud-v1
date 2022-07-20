package net.suqatri.cloud.node.service.factory;

import lombok.Getter;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.impl.redis.bucket.RedissonBucketManager;
import net.suqatri.cloud.api.impl.service.CloudService;
import net.suqatri.cloud.api.impl.service.version.CloudServiceVersion;
import net.suqatri.cloud.api.node.service.factory.ICloudNodeServiceFactory;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.redis.bucket.IRedissonBucketManager;
import net.suqatri.cloud.api.service.ICloudService;
import net.suqatri.cloud.api.service.ServiceState;
import net.suqatri.cloud.api.service.configuration.IServiceStartConfiguration;
import net.suqatri.cloud.api.service.version.ICloudServiceVersion;
import net.suqatri.cloud.node.NodeLauncher;
import net.suqatri.cloud.node.service.NodeCloudServiceManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CloudNodeServiceThread extends Thread{

    private final int maxStartSize = 4;

    @Getter
    private final PriorityQueue<IServiceStartConfiguration> queue;
    private final NodeCloudServiceFactory factory;
    @Getter
    private final ConcurrentHashMap<UUID, CloudServiceProcess> processes;
    @Getter
    private final List<UUID> waitForRemove = new ArrayList<>();

    public CloudNodeServiceThread(NodeCloudServiceFactory factory) {
        super("redicloud-node-service-thread");
        this.factory = factory;
        this.queue = new PriorityQueue<>(new CloudServiceStartComparator());
        this.processes = new ConcurrentHashMap<>();
    }

    @Override
    public void run() {
        while(!Thread.currentThread().isInterrupted() && Thread.currentThread().isAlive()) {

            if(!this.queue.isEmpty()){

                this.queue.stream()
                    .filter(config -> this.waitForRemove.contains(config.getUniqueId()))
                    .forEach(config -> {
                        this.queue.remove(config);
                        if(!((NodeCloudServiceManager)CloudAPI.getInstance().getServiceManager()).existsService(config.getUniqueId().toString())) return;
                        ((NodeCloudServiceManager)CloudAPI.getInstance().getServiceManager()).getServiceIdFetcherMap()
                                .removeAsync(config.getGroupName() + "-" + config.getId(), config.getUniqueId());
                        ((NodeCloudServiceManager)CloudAPI.getInstance().getServiceManager()).deleteBucket(config.getUniqueId().toString());
                    });

                int currentStartSize = 0;
                while(currentStartSize <= this.maxStartSize && !this.queue.isEmpty()) {
                    currentStartSize++;
                    IServiceStartConfiguration configuration = this.queue.poll();
                    try {
                        start(configuration);
                    } catch (Exception e) {
                        this.waitForRemove.add(configuration.getUniqueId());
                        if(configuration.getStartListener() != null) {
                            configuration.getStartListener().completeExceptionally(e);
                        }else{
                            CloudAPI.getInstance().getConsole().error("Error starting service " + configuration.getName() + configuration.getName(), e);
                        }
                    }
                }
            }

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void start(IServiceStartConfiguration configuration) throws Exception {
        if(!CloudAPI.getInstance().getServiceVersionManager().existsServiceVersion(configuration.getServiceVersionName())) throw new Exception("Service version " + configuration.getServiceVersionName() + " not found");

        //TODO check for empty node
        configuration.setNodeId(NodeLauncher.getInstance().getNode().getUniqueId());

        IRBucketHolder<ICloudServiceVersion> versionHolder = CloudAPI.getInstance().getServiceVersionManager().getServiceVersion(configuration.getServiceVersionName());
        if(!versionHolder.get().isDownloaded()) versionHolder.getImpl(CloudServiceVersion.class).download();
        if(versionHolder.get().needPatch()) versionHolder.getImpl(CloudServiceVersion.class).patch();

        Collection<IRBucketHolder<ICloudService>> serviceHolders = this.factory.getServiceManager().getServices();

        if(configuration.getId() < 1) {
            configuration.setId(this.getNextId(configuration.getName(), this.factory.getServiceManager().getServices()));
        }

        for (IRBucketHolder<ICloudService> serviceHolder : serviceHolders) {
            if(serviceHolder.get().getServiceName().equalsIgnoreCase(configuration.getName() + "-" + configuration.getId())) {
                throw new IllegalArgumentException("Service " + configuration.getName() + "-" + configuration.getId() + " already exists");
            }
        }

        CloudService cloudService = new CloudService();
        cloudService.setConfiguration(configuration);
        cloudService.setFallback(configuration.isFallback());
        cloudService.setServiceState(ServiceState.PREPARE);
        cloudService.setMaxPlayers(50);
        cloudService.setMotd("Welcome to RediCloud");
        cloudService.setNodeId(NodeLauncher.getInstance().getNode().getUniqueId());
        IRBucketHolder<ICloudService> holder = this.factory.getServiceManager().createBucket(cloudService.getUniqueId().toString(), cloudService);

        CloudServiceProcess process = new CloudServiceProcess(this.factory, holder);
        process.start();
        this.processes.put(cloudService.getUniqueId(), process);
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

    private static final class CloudServiceStartComparator implements Comparator<IServiceStartConfiguration> {

        @Override
        public int compare(IServiceStartConfiguration o1, IServiceStartConfiguration o2) {
            return o1.getStartPriority() - o2.getStartPriority();
        }

        @Override
        public boolean equals(Object obj) {
            return false;
        }
    }
}
