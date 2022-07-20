package net.suqatri.cloud.node.service.factory;

import lombok.Getter;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.group.ICloudGroup;
import net.suqatri.cloud.api.impl.service.CloudService;
import net.suqatri.cloud.api.impl.service.version.CloudServiceVersion;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.service.ICloudService;
import net.suqatri.cloud.api.service.ServiceState;
import net.suqatri.cloud.api.service.configuration.IServiceStartConfiguration;
import net.suqatri.cloud.api.service.version.ICloudServiceVersion;
import net.suqatri.cloud.node.NodeLauncher;
import net.suqatri.cloud.node.service.NodeCloudServiceManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class CloudNodeServiceThread extends Thread{

    private static final int checkServiceInterval = 4;

    @Getter
    private final PriorityQueue<IServiceStartConfiguration> queue;
    private final NodeCloudServiceFactory factory;
    @Getter
    private final ConcurrentHashMap<UUID, CloudServiceProcess> processes;
    @Getter
    private final List<UUID> waitForRemove = new ArrayList<>();
    private int checkServiceCount = 0;

    public CloudNodeServiceThread(NodeCloudServiceFactory factory) {
        super("redicloud-node-service-thread");
        this.factory = factory;
        this.queue = new PriorityQueue<>(new CloudServiceStartComparator());
        this.processes = new ConcurrentHashMap<>();
    }

    @Override
    public void run() {
        while(!Thread.currentThread().isInterrupted() && Thread.currentThread().isAlive()) {

            long freeSpace = getFreeMemory();
            long preCalculatedFreeSpace = freeSpace;
            int maxStartSize = NodeLauncher.getInstance().getNode().getMaxParallelStartingServiceCount();

            this.checkServiceCount++;
            if(this.checkServiceCount == checkServiceInterval) {
                this.checkServiceCount = 0;
                for (IRBucketHolder<ICloudGroup> groupHolder : CloudAPI.getInstance().getGroupManager().getGroups()) {
                    int count = groupHolder.get().getOnlineServiceCount().getBlockOrNull();
                    int min = groupHolder.get().getMinServices();
                    int max = groupHolder.get().getMaxServices();
                    if (count < min) {
                        int added = 0;
                        for (int i = count; i <= min && added <= maxStartSize; i++) {
                            IServiceStartConfiguration configuration = groupHolder.get().createServiceConfiguration();
                            preCalculatedFreeSpace -= configuration.getMaxMemory();
                            if (preCalculatedFreeSpace < 0) {
                                CloudAPI.getInstance().getConsole().warn("Not enough memory to start a required service of group" + groupHolder.get().getName());
                                break;
                            }
                            this.queue.add(configuration);
                            added++;
                        }
                    }
                }
            }

            if(!this.queue.isEmpty()){

                this.queue.stream()
                    .filter(config -> this.waitForRemove.contains(config.getUniqueId()))
                    .forEach(config -> {
                        this.queue.remove(config);
                        if(!((NodeCloudServiceManager)CloudAPI.getInstance().getServiceManager()).existsService(config.getUniqueId().toString())) return;
                        CloudAPI.getInstance().getServiceManager().getServiceIdFetcherMap()
                                .removeAsync(config.getGroupName() + "-" + config.getId(), config.getUniqueId());
                        ((NodeCloudServiceManager)CloudAPI.getInstance().getServiceManager()).deleteBucket(config.getUniqueId().toString());
                    });

                if(!this.queue.isEmpty()){

                    IServiceStartConfiguration configuration = this.queue.poll();
                    while(getCurrentStartingCount() <= maxStartSize
                            && !this.queue.isEmpty()
                            && freeSpace - configuration.getMaxMemory() > 0){
                        try {
                            start(configuration);
                            freeSpace -= configuration.getMaxMemory();
                        } catch (Exception e) {
                            this.waitForRemove.add(configuration.getUniqueId());
                            if(configuration.getStartListener() != null) {
                                configuration.getStartListener().completeExceptionally(e);
                            }else{
                                CloudAPI.getInstance().getConsole().error("Error starting service " + configuration.getName() + configuration.getName(), e);
                            }
                        }
                        if(!this.queue.isEmpty()) configuration = this.queue.poll();
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

    private long getCurrentStartingCount(){
        return this.processes
                .values()
                .parallelStream()
                .filter(process -> process.getServiceHolder().get().getServiceState() == ServiceState.STARTING || process.getServiceHolder().get().getServiceState() == ServiceState.STARTING)
                .count();
    }

    private long getFreeMemory(){
        try {
            int currentRam = 0;
            for (IRBucketHolder<ICloudService> holder : NodeLauncher.getInstance().getNode().getStartedServices().get(5, TimeUnit.SECONDS)) {
                currentRam += holder.get().getMaxRam();
            }
            return NodeLauncher.getInstance().getNode().getMaxMemory() - currentRam;
        }catch (Exception e){
            e.printStackTrace();
            return 0;
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
