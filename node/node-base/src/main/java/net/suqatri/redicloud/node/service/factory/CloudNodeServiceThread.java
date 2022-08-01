package net.suqatri.redicloud.node.service.factory;

import lombok.Getter;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.group.ICloudGroup;
import net.suqatri.redicloud.api.impl.node.CloudNode;
import net.suqatri.redicloud.api.impl.service.CloudService;
import net.suqatri.redicloud.api.impl.service.version.CloudServiceVersion;
import net.suqatri.redicloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.api.service.ServiceEnvironment;
import net.suqatri.redicloud.api.service.ServiceState;
import net.suqatri.redicloud.api.service.configuration.IServiceStartConfiguration;
import net.suqatri.redicloud.api.service.version.ICloudServiceVersion;
import net.suqatri.redicloud.node.NodeLauncher;
import net.suqatri.redicloud.node.service.NodeCloudServiceManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CloudNodeServiceThread extends Thread {

    private static final int checkServiceInterval = 4;
    private static final int memoryWarningInterval = 30;
    private static final int maxServiceOnNodeInterval = 35;
    private static final int maxServiceOfGroupInterval = 25;
    private static final int valueCheckInterval = 300;

    @Getter
    private final PriorityQueue<IServiceStartConfiguration> queue;
    private final NodeCloudServiceFactory factory;
    @Getter
    private final ConcurrentHashMap<UUID, CloudServiceProcess> processes;
    @Getter
    private final List<UUID> waitForRemove = new ArrayList<>();
    private int checkServiceCount = Integer.MAX_VALUE-1;
    private int memoryWarningCount = Integer.MAX_VALUE-1;
    private int maxServiceOnNodeCount = Integer.MAX_VALUE-1;
    private int maxServiceOfGroupCount = Integer.MAX_VALUE-1;
    private int currentValueCount = Integer.MAX_VALUE-1;
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
        while (!Thread.currentThread().isInterrupted() && Thread.currentThread().isAlive()) {

            if (NodeLauncher.getInstance().isShutdownInitialized()) break;
            if (NodeLauncher.getInstance().isInstanceTimeOuted()) break;
            if (NodeLauncher.getInstance().isRestarting()) break;
            if (NodeLauncher.getInstance().isShutdownInitialized()) break;

            int maxStartSize = NodeLauncher.getInstance().getNode().getMaxParallelStartingServiceCount();

            this.checkServiceCount++;
            if (this.checkServiceCount >= checkServiceInterval) {
                this.checkServiceCount = 0;
                for (IRBucketHolder<ICloudGroup> groupHolder : CloudAPI.getInstance().getGroupManager().getGroups()) {

                    int count = (int) groupHolder.get().getOnlineServices().getBlockOrNull().parallelStream()
                            .filter(holder -> holder.get().getServiceState() != ServiceState.RUNNING_DEFINED)
                            .filter(holder -> holder.get().getServiceState() != ServiceState.OFFLINE)
                            .filter(holder -> {
                                if(holder.get().getPercentToStartNewService() == -1) return true;
                                int percent = ((int)(100 / ((double)holder.get().getMaxPlayers())) * holder.get().getOnlineCount());
                                if(percent >= holder.get().getPercentToStartNewService()) return false;
                                if(holder.get().getMaxPlayers() == -1) return true;
                                return holder.get().getOnlineCount() >= holder.get().getMaxPlayers();
                            })
                            .count();

                    int min = groupHolder.get().getMinServices();

                    if (count < min) {
                        CloudAPI.getInstance().getConsole().trace("Group " + groupHolder.get().getName() + " need to start " + (min - count) + " services");
                        for (int i = count; i < min; i++) {
                            IServiceStartConfiguration configuration = groupHolder.get().createServiceConfiguration();
                            if ((this.node.getFreeMemory() - configuration.getMaxMemory()) < 0) {
                                memoryWarningCount++;
                                if (memoryWarningCount < memoryWarningInterval) break;
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

            if (!this.queue.isEmpty()) {

                this.queue.stream()
                        .filter(config -> this.waitForRemove.contains(config.getUniqueId()))
                        .forEach(config -> {
                            this.queue.remove(config);
                            if (config.isStatic()) return;
                            if (!((NodeCloudServiceManager) CloudAPI.getInstance().getServiceManager()).existsService(config.getUniqueId()))
                                return;
                            ((NodeCloudServiceManager) CloudAPI.getInstance().getServiceManager()).removeFromFetcher(config.getName());
                            NodeLauncher.getInstance().getServiceManager().deleteBucket(config.getUniqueId().toString());
                        });

                if (!this.queue.isEmpty()) {

                    if (this.node.getStartedServicesCount() >= this.node.getMaxServiceCount()
                            && this.node.getMaxServiceCount() != -1) {
                        this.maxServiceOnNodeCount++;
                        if (this.maxServiceOnNodeCount < maxServiceOnNodeInterval) return;
                        this.maxServiceOnNodeCount = 0;
                        CloudAPI.getInstance().getConsole().warn("Max node service count reached, cannot start more services!");
                        return;
                    }

                    IServiceStartConfiguration configuration = this.queue.poll();
                    while ((this.node.getMaxServiceCount() == -1 || getCurrentStartingCount() < maxStartSize)
                            && configuration != null
                            && this.node.getFreeMemory() > 0) {
                        CloudAPI.getInstance().getConsole().debug("Service " + configuration.getName() + " is now inside a big thread of a POWER cpu!");
                        if (configuration.isGroupBased()) {
                            IRBucketHolder<ICloudGroup> groupHolder = CloudAPI.getInstance().getGroupManager().getGroup(configuration.getGroupName());
                            if (groupHolder.get().getOnlineServiceCount().getBlockOrNull() >= groupHolder.get().getMaxServices() && groupHolder.get().getMaxServices() != -1) {
                                if (configuration.getStartListener() != null) {
                                    configuration.getStartListener().completeExceptionally(new IllegalStateException("Can't start service of group " + configuration.getGroupName() + " because max service count is reached!"));
                                } else {
                                    this.maxServiceOfGroupCount++;
                                    if (!this.queue.isEmpty()) {
                                        configuration = this.queue.poll();
                                    } else {
                                        configuration = null;
                                    }
                                    if (this.maxServiceOfGroupCount < maxServiceOfGroupInterval) continue;
                                    this.maxServiceOfGroupCount = 0;
                                    CloudAPI.getInstance().getConsole().warn("Can't start service of group " + configuration.getGroupName() + " because max service count is reached!");
                                }
                                continue;
                            }
                        }
                        try {
                            start(configuration);
                        } catch (Exception e) {
                            this.waitForRemove.add(configuration.getUniqueId());
                            if (configuration.getStartListener() != null) {
                                configuration.getStartListener().completeExceptionally(e);
                            } else {
                                CloudAPI.getInstance().getConsole().error("Error starting service " + configuration.getName(), e);
                            }
                        }
                        if (!this.queue.isEmpty()) {
                            configuration = this.queue.poll();
                        } else {
                            configuration = null;
                        }
                    }
                    if (configuration != null) {
                        this.queue.add(configuration);
                        this.currentValueCount++;
                        if (this.currentValueCount > valueCheckInterval) {
                            this.currentValueCount = 0;
                            CloudAPI.getInstance().getConsole().warn("Failed to start service " + configuration.getName() + "! Check following values:");
                            CloudAPI.getInstance().getConsole().warn(" - Max service count: " + getCurrentStartingCount() + "/" + this.node.getMaxServiceCount());
                            CloudAPI.getInstance().getConsole().warn(" - Max memory: " + this.node.getMemoryUsage() + "/" + this.node.getMaxMemory());
                        }
                    }
                }
            }

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
            }
        }
    }

    private long getCurrentStartingCount() {
        return this.processes
                .values()
                .parallelStream()
                .filter(process -> process.getServiceHolder().get().getServiceState() == ServiceState.STARTING || process.getServiceHolder().get().getServiceState() == ServiceState.STARTING)
                .count();
    }

    private void start(IServiceStartConfiguration configuration) throws Exception {
        CloudAPI.getInstance().getConsole().debug("Preparing to start service " + configuration.getName());

        if (!CloudAPI.getInstance().getServiceVersionManager().existsServiceVersion(configuration.getServiceVersionName()))
            throw new Exception("Service version " + configuration.getServiceVersionName() + " not found");

        CloudService cloudService = null;

        IRBucketHolder<ICloudServiceVersion> versionHolder = CloudAPI.getInstance().getServiceVersionManager().getServiceVersion(configuration.getServiceVersionName());
        if (!versionHolder.get().isDownloaded()) versionHolder.getImpl(CloudServiceVersion.class).download();
        if (versionHolder.get().needPatch()) versionHolder.getImpl(CloudServiceVersion.class).patch();

        Collection<IRBucketHolder<ICloudService>> serviceHolders = this.factory.getServiceManager().getServices();

        if(configuration.isStatic()){
            if(configuration.getId() < 1){
                configuration.setId(getNextId(configuration.getName(), configuration.isStatic(), serviceHolders));
            }
            String serviceName = configuration.getName() + "-" + configuration.getId();
            CloudAPI.getInstance().getConsole().trace("Checking if service " + serviceName + " exists...");
            if(CloudAPI.getInstance().getServiceManager().existsService(serviceName)){
                cloudService = CloudAPI.getInstance().getServiceManager().getService(serviceName).getImpl(CloudService.class);
                configuration.setUniqueId(cloudService.getUniqueId());
            }
        }else{
            if (CloudAPI.getInstance().getServiceManager().existsService(configuration.getUniqueId())) {
                NodeLauncher.getInstance().getServiceManager().removeFromFetcher(configuration.getName() + "-" + configuration.getId(), configuration.getUniqueId());
                NodeLauncher.getInstance().getServiceManager().deleteBucket(configuration.getUniqueId().toString());
            }
            if(configuration.getId() < 1){
                configuration.setId(this.getNextId(configuration.getName(), configuration.isStatic(), serviceHolders));
            }
            configuration.setNodeId(NodeLauncher.getInstance().getNode().getUniqueId());
        }

        if(!configuration.isStatic()){
            for (IRBucketHolder<ICloudService> serviceHolder : serviceHolders) {
                if (serviceHolder.get().getServiceName().equalsIgnoreCase(configuration.getName() + "-" + configuration.getId())) {
                    throw new IllegalArgumentException("Service " + configuration.getName() + "-" + configuration.getId() + " already exists");
                }
            }
        }

        IRBucketHolder<ICloudService> holder = null;
        if(cloudService == null) {
            cloudService = new CloudService();
            cloudService.setConfiguration(configuration);
            cloudService.setExternal(false);
            cloudService.setServiceState(ServiceState.PREPARE);
            cloudService.setMaxPlayers(50);
            if (configuration.getEnvironment() == ServiceEnvironment.PROXY) {
                cloudService.setMotd("§7•§8● §bRedi§3Cloud §8» §fA §bredis §fbased §bcluster §fcloud system§r\n    §b§l§8× §fDiscord §8➜ §3https://discord.gg/g2HV52VV4G");
            } else {
                cloudService.setMotd("§bRedi§3Cloud§7-§fService");
            }
            cloudService.setNodeId(NodeLauncher.getInstance().getNode().getUniqueId());
            holder = this.factory.getServiceManager().createBucket(cloudService.getUniqueId().toString(), cloudService);
        }else{
            holder = cloudService.getHolder();
            holder.getImpl(CloudService.class).setExternal(false);
            holder.get().setServiceState(ServiceState.PREPARE);
            holder.getImpl(CloudService.class).setNodeId(NodeLauncher.getInstance().getNode().getUniqueId());
            holder.get().update();
        }
        this.factory.getServiceManager().putInFetcher(cloudService.getServiceName(), cloudService.getUniqueId());

        CloudServiceProcess process = new CloudServiceProcess(this.factory, holder);
        process.start();
        this.processes.put(cloudService.getUniqueId(), process);

        CloudAPI.getInstance().getConsole().debug("Started service process for " + cloudService.getServiceName() + " successfully");
    }

    private int getNextId(String groupName, boolean staticService, Collection<IRBucketHolder<ICloudService>> servicesHolders) {
        int i = 1;
        List<Integer> ids = new ArrayList<>();
        for (IRBucketHolder<ICloudService> serviceHolder : servicesHolders) {
            if(serviceHolder.get().getServiceState() == ServiceState.OFFLINE && staticService) continue;
            if (serviceHolder.get().getGroupName().equalsIgnoreCase(groupName)) {
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
