package net.suqatri.cloud.node.service.factory;

import lombok.Getter;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.group.ICloudGroup;
import net.suqatri.cloud.api.impl.node.CloudNode;
import net.suqatri.cloud.api.impl.service.CloudService;
import net.suqatri.cloud.api.impl.service.version.CloudServiceVersion;
import net.suqatri.cloud.api.node.ICloudNode;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.service.ICloudService;
import net.suqatri.cloud.api.service.ServiceEnvironment;
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
    private static final int memoryWarningInterval = 15;
    private static final int maxServiceOnNodeInterval = 20;
    private static final int maxServiceOfGroupInterval = 15;

    @Getter
    private final PriorityQueue<IServiceStartConfiguration> queue;
    private final NodeCloudServiceFactory factory;
    @Getter
    private final ConcurrentHashMap<UUID, CloudServiceProcess> processes;
    @Getter
    private final List<UUID> waitForRemove = new ArrayList<>();
    private int checkServiceCount = 0;
    private int memoryWarningCount = 0;
    private int maxServiceOnNodeCount = 0;
    private int maxServiceOfGroupCount = 0;
    private CloudNode node;

    public CloudNodeServiceThread(NodeCloudServiceFactory factory) {
        super("redicloud-node-service-thread");
        this.factory = factory;
        this.queue = new PriorityQueue<>(new CloudServiceStartComparator());
        this.processes = new ConcurrentHashMap<>();
    }

    @Override
    public void run() {
        this.node = NodeLauncher.getInstance().getNode();
        while(!Thread.currentThread().isInterrupted() && Thread.currentThread().isAlive()) {

            if(NodeLauncher.getInstance().isShutdownInitialized()) break;

            int maxStartSize = NodeLauncher.getInstance().getNode().getMaxParallelStartingServiceCount();

            this.checkServiceCount++;
            if(this.checkServiceCount == checkServiceInterval) {
                this.checkServiceCount = 0;
                for (IRBucketHolder<ICloudGroup> groupHolder : CloudAPI.getInstance().getGroupManager().getGroups()) {
                    int count = groupHolder.get().getOnlineServiceCount().getBlockOrNull();
                    int min = groupHolder.get().getMinServices();
                    if (count < min) {
                        for (int i = count; i <= min; i++) {
                            IServiceStartConfiguration configuration = groupHolder.get().createServiceConfiguration();
                            if ((this.node.getFreeMemory() - configuration.getMaxMemory()) < 0) {
                                memoryWarningCount++;
                                if(memoryWarningCount < memoryWarningInterval) break;
                                memoryWarningCount = 0;
                                long maxRam = NodeLauncher.getInstance().getNode().getMaxMemory();
                                CloudAPI.getInstance().getConsole().warn("Not enough memory to start a required service of group "
                                        + groupHolder.get().getName() + " (" + (this.node.getMemoryUsage()) + "/" + this.node.getMaxMemory() + "MB)");
                                break;
                            }
                            this.queue.add(configuration);
                        }
                    }
                }
            }

            if(!this.queue.isEmpty()){

                this.queue.stream()
                    .filter(config -> this.waitForRemove.contains(config.getUniqueId()))
                    .forEach(config -> {
                        this.queue.remove(config);
                        if(config.isStatic()) return;
                        if(!((NodeCloudServiceManager)CloudAPI.getInstance().getServiceManager()).existsService(config.getUniqueId())) return;
                        ((NodeCloudServiceManager) CloudAPI.getInstance().getServiceManager()).removeFromFetcher(config.getName());
                        NodeLauncher.getInstance().getServiceManager().deleteBucket(config.getUniqueId().toString());
                    });

                if(!this.queue.isEmpty()){

                    if(this.node.getStartedServicesCount() >= this.node.getMaxServiceCount()
                            && this.node.getMaxServiceCount() != -1){
                        this.maxServiceOnNodeCount++;
                        if(this.maxServiceOnNodeCount < maxServiceOnNodeInterval) return;
                        this.maxServiceOnNodeCount = 0;
                        CloudAPI.getInstance().getConsole().warn("Max node service count reached, cannot start more services!");
                        return;
                    }

                    IServiceStartConfiguration configuration = this.queue.poll();
                    while(getCurrentStartingCount() <= maxStartSize
                            && !this.queue.isEmpty()
                            && this.node.getFreeMemory() - configuration.getMaxMemory() > 0){
                        if(configuration.isGroupBased()){
                            IRBucketHolder<ICloudGroup> groupHolder = CloudAPI.getInstance().getGroupManager().getGroup(configuration.getGroupName());
                            if(groupHolder.get().getOnlineServiceCount().getBlockOrNull() >= groupHolder.get().getMaxServices()){
                                if(configuration.getStartListener() != null){
                                    configuration.getStartListener().completeExceptionally(new IllegalStateException("Can't start service of group " + configuration.getGroupName() + " because max service count is reached!"));
                                }else{
                                    this.maxServiceOfGroupCount++;
                                    if(this.maxServiceOfGroupCount < maxServiceOfGroupInterval) return;
                                    this.maxServiceOfGroupCount = 0;
                                    CloudAPI.getInstance().getConsole().warn("Can't start service of group " + configuration.getGroupName() + " because max service count is reached!");
                                }
                                return;
                            }
                        }
                        try {
                            start(configuration);
                        } catch (Exception e) {
                            this.waitForRemove.add(configuration.getUniqueId());
                            if(configuration.getStartListener() != null) {
                                configuration.getStartListener().completeExceptionally(e);
                            }else{
                                CloudAPI.getInstance().getConsole().error("Error starting service " + configuration.getName(), e);
                            }
                        }
                        if(!this.queue.isEmpty()) configuration = this.queue.poll();
                    }
                }
            }

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {}
        }
    }

    private long getCurrentStartingCount(){
        return this.processes
                .values()
                .parallelStream()
                .filter(process -> process.getServiceHolder().get().getServiceState() == ServiceState.STARTING || process.getServiceHolder().get().getServiceState() == ServiceState.STARTING)
                .count();
    }

    private void start(IServiceStartConfiguration configuration) throws Exception {
        if(!CloudAPI.getInstance().getServiceVersionManager().existsServiceVersion(configuration.getServiceVersionName())) throw new Exception("Service version " + configuration.getServiceVersionName() + " not found");

        CloudService cloudService = null;
        if(CloudAPI.getInstance().getServiceManager().existsService(configuration.getUniqueId()) || configuration.isStatic()){
            cloudService = CloudAPI.getInstance().getServiceManager().getService(configuration.getUniqueId())
                    .getImpl(CloudService.class);
            if(!cloudService.isStatic() && cloudService.getNodeId().equals(NodeLauncher.getInstance().getNode().getUniqueId())){
                configuration.getStartListener().completeExceptionally(
                        new IllegalArgumentException("Can´t start static service how is stored on node "
                        + CloudAPI.getInstance().getNodeManager().getNode(cloudService.getNodeId()).get().getName()));
                return;
            }
        }else{
            configuration.setNodeId(NodeLauncher.getInstance().getNode().getUniqueId());
        }

        IRBucketHolder<ICloudServiceVersion> versionHolder = CloudAPI.getInstance().getServiceVersionManager().getServiceVersion(configuration.getServiceVersionName());
        if(!versionHolder.get().isDownloaded()) versionHolder.getImpl(CloudServiceVersion.class).download();
        if(versionHolder.get().needPatch()) versionHolder.getImpl(CloudServiceVersion.class).patch();

        Collection<IRBucketHolder<ICloudService>> serviceHolders = this.factory.getServiceManager().getServices();

        if(configuration.getId() < 1) {
            configuration.setId(this.getNextId(configuration.getName(), serviceHolders));
        }

        for (IRBucketHolder<ICloudService> serviceHolder : serviceHolders) {
            if(serviceHolder.get().getServiceName().equalsIgnoreCase(configuration.getName() + "-" + configuration.getId())) {
                throw new IllegalArgumentException("Service " + configuration.getName() + "-" + configuration.getId() + " already exists");
            }
        }

        cloudService = new CloudService();
        cloudService.setConfiguration(configuration);
        cloudService.setFallback(configuration.isFallback());
        cloudService.setServiceState(ServiceState.PREPARE);
        cloudService.setMaxPlayers(50);
        if(configuration.getEnvironment() == ServiceEnvironment.PROXY) {
            cloudService.setMotd("§7•§8● §bRedi§3Cloud §8» §fA §bredis §fbased §bcluster §fcloud system§r\n    §b§l§8× §fDiscord §8➜ §3https://discord.gg/g2HV52VV4G");
        }else{
            cloudService.setMotd("§bRedi§3Cloud§7-§fService");
        }
        cloudService.setNodeId(NodeLauncher.getInstance().getNode().getUniqueId());
        IRBucketHolder<ICloudService> holder = this.factory.getServiceManager().createBucket(cloudService.getUniqueId().toString(), cloudService);
        this.factory.getServiceManager().putInFetcher(cloudService.getServiceName(), cloudService.getUniqueId());

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
