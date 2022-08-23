package dev.redicloud.node.service.factory;

import dev.redicloud.api.impl.redis.bucket.fetch.RedissonBucketFetchManager;
import lombok.Getter;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.group.ICloudGroup;
import dev.redicloud.api.impl.configuration.impl.LimboFallbackConfiguration;
import dev.redicloud.api.impl.node.CloudNode;
import dev.redicloud.api.impl.service.CloudService;
import dev.redicloud.api.impl.service.version.CloudServiceVersion;
import dev.redicloud.api.redis.event.RedisConnectedEvent;
import dev.redicloud.api.service.ICloudService;
import dev.redicloud.api.service.ServiceEnvironment;
import dev.redicloud.api.service.ServiceState;
import dev.redicloud.api.service.configuration.IServiceStartConfiguration;
import dev.redicloud.node.NodeLauncher;
import org.redisson.api.RPriorityBlockingDeque;
import org.redisson.codec.JsonJacksonCodec;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class CloudNodeServiceThread extends Thread {

    private static final int checkServiceInterval = 4;
    private static final int memoryWarningInterval = 30;
    private static final int maxServiceOnNodeInterval = 35;
    private static final int maxServiceOfGroupInterval = 25;
    private static final int valueCheckInterval = 300;

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
    @Getter
    private RPriorityBlockingDeque<IServiceStartConfiguration> queue;
    @Getter
    private LimboFallbackConfiguration limboFallbackConfiguration = new LimboFallbackConfiguration();

    public CloudNodeServiceThread(NodeCloudServiceFactory factory) {
        super("redicloud-node-service-thread");
        this.factory = factory;
        this.processes = new ConcurrentHashMap<>();
        CloudAPI.getInstance().getEventManager().registerWithoutBlockWarning(RedisConnectedEvent.class, event
                -> {
            this.queue = event.getConnection().getClient().getPriorityBlockingDeque("factory:queue", new JsonJacksonCodec());
            this.queue.trySetComparator(new CloudServiceStartComparator());
            this.limboFallbackConfiguration = CloudAPI.getInstance().getConfigurationManager().existsConfiguration(this.limboFallbackConfiguration.getIdentifier())
                    ? CloudAPI.getInstance().getConfigurationManager().getConfiguration(this.limboFallbackConfiguration.getIdentifier(), LimboFallbackConfiguration.class)
                    : CloudAPI.getInstance().getConfigurationManager().createConfiguration(this.limboFallbackConfiguration);
        });
    }

    @Override
    public void run() {
        while(this.queue == null){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.node = NodeLauncher.getInstance().getNode();
        while (!Thread.currentThread().isInterrupted() && Thread.currentThread().isAlive()) {

            if(NodeLauncher.getInstance().isShutdownInitialized() ){
                this.interrupt();
                return;
            }

            try {
                if (NodeLauncher.getInstance().isInstanceTimeOuted()) return;
                if (NodeLauncher.getInstance().isRestarting()) return;

                this.checkServiceCount++;

                //Check min group service count
                if (this.checkServiceCount >= checkServiceInterval) {
                    this.checkServiceCount = 0;
                    for (ICloudGroup group : CloudAPI.getInstance().getGroupManager().getGroups()) {

                        if(group.getName().equals("Fallback") && !this.limboFallbackConfiguration.isEnabled()) continue;

                        Collection<ICloudService> services = group.getConnectedServices().getBlockOrNull();
                        if(services == null) continue;

                        long count = services
                                .parallelStream()
                                .filter(ICloudService::isGroupBased)
                                .filter(holder -> holder.getGroupName().equalsIgnoreCase(group.getName()))
                                .filter(holder -> holder.getServiceState() != ServiceState.OFFLINE)
                                .filter(holder -> holder.getServiceState() != ServiceState.RUNNING_DEFINED)
                                .filter(holder -> {
                                    if (holder.getMaxPlayers() == -1) return true;
                                    if (holder.getPercentToStartNewService() == -1) return true;
                                    if(holder.getServiceState() == ServiceState.STARTING
                                            || holder.getServiceState() == ServiceState.PREPARE
                                            || holder.getServiceState() == ServiceState.STOPPING) return true;
                                    int percent = ((int) (100 / ((double) holder.getMaxPlayers())) * holder.getOnlineCount());
                                    if (percent >= holder.getPercentToStartNewService()) return false;
                                    return holder.getOnlineCount() <= holder.getMaxPlayers();
                                })
                                .count();
                        if(this.queue.isExists()){
                            for (IServiceStartConfiguration configuration : this.queue.readAll()) {
                                if(!configuration.isGroupBased()) continue;
                                if(!configuration.getGroupName().equalsIgnoreCase(group.getName())) continue;
                                count++;
                            }
                        }

                        int min = group.getMinServices();
                        if (count < min) {
                            CloudAPI.getInstance().getConsole().trace("Group " + group.getName() + " need to start " + (min - count) + " services");
                            for (long i = count; i < min; i++) {
                                IServiceStartConfiguration configuration = group.createServiceConfiguration();
                                if ((this.node.getFreeMemory() - configuration.getMaxMemory()) < 0) {
                                    memoryWarningCount++;
                                    if (memoryWarningCount < memoryWarningInterval) continue;
                                    memoryWarningCount = 0;
                                    long maxRam = NodeLauncher.getInstance().getNode().getMaxMemory();
                                    CloudAPI.getInstance().getConsole().warn("Not enough memory to start a required service of group "
                                            + group.getName() + " (" + (this.node.getMemoryUsage()) + "/" + this.node.getMaxMemory() + "MB)");
                                    continue;
                                }
                                this.queue.add(configuration);
                            }
                        }else if(count > min){
                            int preCount = (int) count;
                            for (ICloudService service : group.getServices().getBlockOrNull()) {
                                if(preCount <= min) break;
                                if((System.currentTimeMillis()-service.getLastPlayerAction()) < TimeUnit.MINUTES.toMillis(3)) continue;
                                if(service.getOnlineCount() != 0) continue;
                                if(service.getServiceState() == ServiceState.STOPPING){
                                    preCount--;
                                    continue;
                                }
                                CloudAPI.getInstance().getConsole().debug("Service " + service.getServiceName() + " will be stopped");
                                CloudAPI.getInstance().getServiceManager().stopServiceAsync(service.getUniqueId(), false);
                                preCount--;
                            }
                        }
                    }
                }

                if (this.queue.isExists()) {

                    //Remove stopped service from start config queue
                    this.queue.readAll().stream()
                        .filter(config -> this.waitForRemove.contains(config.getUniqueId()))
                        .forEach(config -> this.queue.remove(config));

                    if (this.queue.isExists()) {

                        //Check started service count of the node
                        if (this.node.getStartedServicesCount() >= this.node.getMaxServiceCount()
                                && this.node.getMaxServiceCount() != -1) {
                            this.maxServiceOnNodeCount++;
                            if (this.maxServiceOnNodeCount < maxServiceOnNodeInterval) return;
                            this.maxServiceOnNodeCount = 0;
                            CloudAPI.getInstance().getConsole().warn("Max node service count reached, cannot start more services!");
                            return;
                        }

                        IServiceStartConfiguration configuration = this.queue.poll();

                        while (
                                (this.node.getMaxParallelStartingServiceCount() == -1
                                    || getCurrentStartingCount() < this.node.getMaxParallelStartingServiceCount())
                                && configuration != null
                                && this.node.getFreeMemory() > 0
                                && (this.node.getMaxServiceCount() == -1
                                    || this.node.getStartedServicesCount() < this.node.getMaxServiceCount())
                        ) {

                            CloudAPI.getInstance().getConsole().debug("Service " + configuration.getName() + " is now inside a start agent!");

                            //Check max started service limit of the group
                            if (configuration.isGroupBased()) {
                                ICloudGroup group = CloudAPI.getInstance().getGroupManager().getGroup(configuration.getGroupName());
                                if (group.getOnlineServiceCount().getBlockOrNull() >= group.getMaxServices() && group.getMaxServices() != -1) {
                                    if (configuration.getStartListener() != null) {
                                        configuration.getStartListener().completeExceptionally(new IllegalStateException("Can't start service of group " + configuration.getGroupName() + " because max service count is reached!"));
                                    } else {
                                        this.maxServiceOfGroupCount++;
                                        if (this.queue.isExists()) {
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

                            //Start the service
                            try {
                                start(configuration);
                            } catch (Exception e) {
                                this.waitForRemove.add(configuration.getUniqueId());
                                if (configuration.getStartListener() != null) {
                                    configuration.getStartListener().completeExceptionally(e);
                                } else {
                                    CloudAPI.getInstance().getConsole().error("Failed to start service " + configuration.getName(), e);
                                }
                            }

                            //Set new configuration if there is one
                            if (this.queue.isExists()) {
                                configuration = this.queue.poll();
                            } else {
                                configuration = null;
                            }
                        }

                        //Handle service start failure
                        if (configuration != null) {
                            this.queue.add(configuration);
                            this.currentValueCount++;
                            if (this.currentValueCount > valueCheckInterval
                            && (getCurrentStartingCount() < this.node.getMaxParallelStartingServiceCount())) {
                                this.currentValueCount = 0;
                                CloudAPI.getInstance().getConsole().warn("Failed to start service " + configuration.getName() + "! Check following values:");
                                CloudAPI.getInstance().getConsole().warn(" - Current parallel starting service count: " + getCurrentStartingCount() + "/" + this.node.getMaxParallelStartingServiceCount());
                                CloudAPI.getInstance().getConsole().warn(" - Max Service count: " + this.node.getMaxServiceCount() + "/" + this.node.getMaxServiceCount());
                                CloudAPI.getInstance().getConsole().warn(" - Max memory: " + this.node.getMemoryUsage() + "/" + this.node.getMaxMemory());
                            }
                        }
                    }
                }
                Thread.sleep(200);
            }catch (Exception e){
                if(e instanceof InterruptedException) return;
                CloudAPI.getInstance().getConsole().error("Error in service factory thread", e);
            }
        }
    }

    private long getCurrentStartingCount() {
        return this.processes
                .values()
                .parallelStream()
                .filter(process -> process.getService().getServiceState() == ServiceState.STARTING
                        || process.getService().getServiceState() == ServiceState.PREPARE)
                .count();
    }

    private void start(IServiceStartConfiguration configuration) throws Exception {
        CloudAPI.getInstance().getConsole().debug("Preparing to start service " + configuration.getName());

        if (!CloudAPI.getInstance().getServiceVersionManager().existsServiceVersion(configuration.getServiceVersionName()))
            throw new Exception("Service version " + configuration.getServiceVersionName() + " not found");

        CloudService cloudService = null;

        CloudServiceVersion versionHolder = (CloudServiceVersion) CloudAPI.getInstance().getServiceVersionManager().getServiceVersion(configuration.getServiceVersionName());
        if (!versionHolder.isDownloaded()) versionHolder.download();
        if (versionHolder.needPatch()) versionHolder.patch();

        Collection<ICloudService> services = this.factory.getServiceManager().getServices();

        if(configuration.isStatic()){
            if(configuration.getId() < 1){
                configuration.setId(getNextId(configuration.getName(), configuration.isStatic(), services));
            }
            String serviceName = configuration.getName() + "-" + configuration.getId();
            CloudAPI.getInstance().getConsole().trace("Checking if service " + serviceName + " exists...");
            if(CloudAPI.getInstance().getServiceManager().existsService(serviceName)){
                cloudService = (CloudService) CloudAPI.getInstance().getServiceManager().getService(serviceName);
                configuration.setUniqueId(cloudService.getUniqueId());
            }
        }else{
            if(configuration.getId() < 1){
                configuration.setId(this.getNextId(configuration.getName(), configuration.isStatic(), services));
            }
            configuration.setNodeId(NodeLauncher.getInstance().getNode().getUniqueId());
        }

        if(!configuration.isStatic()){
            for (ICloudService serviceHolder : services) {
                if (serviceHolder.getServiceName().equalsIgnoreCase(configuration.getName() + "-" + configuration.getId())) {
                    throw new IllegalArgumentException("Service " + configuration.getName() + "-" + configuration.getId() + " already exists");
                }
            }
        }

        CloudService holder = null;
        if(cloudService == null) {
            cloudService = new CloudService();
            cloudService.setConfiguration(configuration);
            cloudService.setExternal(false);
            cloudService.setServiceState(ServiceState.PREPARE);
            cloudService.setMaintenance(configuration.isGroupBased()
                    && CloudAPI.getInstance().getGroupManager().getGroup(configuration.getGroupName()).isMaintenance());
            cloudService.setMaxPlayers(50);
            if (configuration.getEnvironment() == ServiceEnvironment.BUNGEECORD || configuration.getEnvironment() == ServiceEnvironment.VELOCITY) {
                cloudService.setMotd("§7•§8● §bRedi§3Cloud §8» §fA §bredis §fbased §bcluster §fcloud system§r\n    §b§l§8× §fDiscord §8➜ §3https://discord.gg/g2HV52VV4G");
            } else {
                cloudService.setMotd("§bRedi§3Cloud§7-§fService");
            }
            cloudService.setNodeId(NodeLauncher.getInstance().getNode().getUniqueId());
            holder = (CloudService) this.factory.getServiceManager().createBucket(cloudService.getUniqueId().toString(), cloudService);
        }else{
            holder = cloudService;
            holder.setExternal(false);
            holder.setServiceState(ServiceState.PREPARE);
            holder.setNodeId(NodeLauncher.getInstance().getNode().getUniqueId());
            holder.update();
        }

        ((RedissonBucketFetchManager)CloudAPI.getInstance().getServiceManager()).putInFetcher(holder.getFetchKey(), holder.getFetchValue());

        CloudServiceProcess process = new CloudServiceProcess(this.factory, holder);
        process.start();
        this.processes.put(cloudService.getUniqueId(), process);

        CloudAPI.getInstance().getConsole().debug("Started service process for " + cloudService.getServiceName() + " successfully");
    }

    private int getNextId(String groupName, boolean staticService, Collection<ICloudService> servicesHolders) {
        int i = 1;
        List<Integer> ids = new ArrayList<>();
        for (ICloudService serviceHolder : servicesHolders) {
            if(serviceHolder.getServiceState() == ServiceState.OFFLINE && staticService) continue;
            if (serviceHolder.getGroupName().equalsIgnoreCase(groupName)) {
                ids.add(serviceHolder.getId());
            }
        }
        while (ids.contains(i)) i++;
        return i;
    }

}
