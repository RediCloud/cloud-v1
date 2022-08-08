package net.suqatri.redicloud.node;

import lombok.Getter;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.console.LogLevel;
import net.suqatri.redicloud.api.group.GroupProperty;
import net.suqatri.redicloud.api.group.ICloudGroup;
import net.suqatri.redicloud.api.impl.group.CloudGroup;
import net.suqatri.redicloud.api.impl.node.CloudNode;
import net.suqatri.redicloud.api.impl.poll.timeout.ITimeOutPollManager;
import net.suqatri.redicloud.api.impl.redis.RedisConnection;
import net.suqatri.redicloud.api.network.INetworkComponentInfo;
import net.suqatri.redicloud.api.node.ICloudNode;
import net.suqatri.redicloud.api.node.NodeCloudDefaultAPI;
import net.suqatri.redicloud.api.node.event.CloudNodeConnectedEvent;
import net.suqatri.redicloud.api.node.event.CloudNodeDisconnectEvent;
import net.suqatri.redicloud.api.node.file.event.FilePulledTemplatesEvent;
import net.suqatri.redicloud.api.packet.PacketChannel;
import net.suqatri.redicloud.api.redis.IRedisConnection;
import net.suqatri.redicloud.api.redis.RedisCredentials;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.api.service.ServiceEnvironment;
import net.suqatri.redicloud.api.service.ServiceState;
import net.suqatri.redicloud.api.service.version.ServiceVersionProperty;
import net.suqatri.redicloud.api.utils.Files;
import net.suqatri.redicloud.commons.connection.IPUtils;
import net.suqatri.redicloud.commons.file.FileWriter;
import net.suqatri.redicloud.node.commands.*;
import net.suqatri.redicloud.node.console.CommandConsoleManager;
import net.suqatri.redicloud.node.console.NodeConsole;
import net.suqatri.redicloud.node.console.setup.SetupControlState;
import net.suqatri.redicloud.node.file.FileTransferManager;
import net.suqatri.redicloud.node.listener.CloudNodeConnectedListener;
import net.suqatri.redicloud.node.listener.CloudNodeDisconnectListener;
import net.suqatri.redicloud.node.listener.CloudServiceStartedListener;
import net.suqatri.redicloud.node.listener.CloudServiceStoppedListener;
import net.suqatri.redicloud.node.node.ClusterConnectionInformation;
import net.suqatri.redicloud.node.node.packet.NodePingPacket;
import net.suqatri.redicloud.node.node.packet.NodePingPacketResponse;
import net.suqatri.redicloud.node.poll.timeout.TimeOutPollManager;
import net.suqatri.redicloud.node.scheduler.Scheduler;
import net.suqatri.redicloud.node.service.NodeCloudServiceManager;
import net.suqatri.redicloud.node.service.factory.NodeCloudServiceFactory;
import net.suqatri.redicloud.node.service.screen.ServiceScreenManager;
import net.suqatri.redicloud.node.service.version.NodeCloudServiceVersionManager;
import net.suqatri.redicloud.node.setup.node.NodeConnectionDataSetup;
import net.suqatri.redicloud.node.setup.redis.*;
import net.suqatri.redicloud.node.template.NodeCloudServiceTemplateManager;
import org.apache.commons.io.FileUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Getter
public class NodeLauncher extends NodeCloudDefaultAPI {

    @Getter
    private static NodeLauncher instance;

    private final NodeConsole console;
    private final CommandConsoleManager commandManager;
    private final Scheduler scheduler;
    private final NodeCloudServiceManager serviceManager;
    private final NodeCloudServiceFactory serviceFactory;
    private final NodeCloudServiceVersionManager serviceVersionManager;
    private final ServiceScreenManager screenManager;
    private final ITimeOutPollManager timeOutPollManager;
    private RedisConnection redisConnection;
    private INetworkComponentInfo networkComponentInfo;
    private boolean shutdownInitialized = false;
    private CloudNode node;
    private FileTransferManager fileTransferManager;
    private NodeCloudServiceTemplateManager serviceTemplateManager;
    private boolean skipTemplateSync = false;
    private boolean firstTemplatePulled = false;
    private boolean restarting = false;
    private String hostName;

    public NodeLauncher(String[] args) throws Exception {
        instance = this;
        this.hostName = IPUtils.getPublicIP();
        this.scheduler = new Scheduler();
        this.commandManager = new CommandConsoleManager();
        this.console = this.commandManager.getNodeConsole();
        this.serviceManager = new NodeCloudServiceManager();
        this.serviceFactory = new NodeCloudServiceFactory(this.serviceManager);
        this.serviceVersionManager = new NodeCloudServiceVersionManager();
        this.screenManager = new ServiceScreenManager();
        this.timeOutPollManager = new TimeOutPollManager();
        this.handleProgrammArguments(args);

        this.init(() -> {
            this.fileTransferManager = new FileTransferManager();
            this.serviceTemplateManager = new NodeCloudServiceTemplateManager();
            this.registerCommands();
            this.registerInternalListeners();
            this.registerInternalPackets();
            this.registerListeners();
            this.registerPackets();
            this.scheduler.runTaskLater(() -> this.syncTemplates(() -> {
            }), 2, TimeUnit.SECONDS);
        });
    }

    private void syncTemplates(Runnable runnable) {
        if (this.skipTemplateSync) {
            this.console.info("Skipping template sync!");
            runnable.run();
            return;
        }
        this.console.info("Searching for node to sync pull templates from...");
        ICloudNode node = null;
        for (ICloudNode holder : getNodeManager().getNodes()) {
            if (!holder.isConnected()) continue;
            if (holder.getUniqueId().equals(this.node.getUniqueId())) continue;
            if (node == null && !this.node.isFileNode()) {
                node = holder;
                continue;
            }
            if (holder.isFileNode()) {
                if (holder.getUpTime() > node.getUpTime()) {
                    node = holder;
                    continue;
                }
                node = holder;
            }
        }
        if (node == null && !this.node.isFileNode()) {
            this.console.warn("-----------------------------------------");
            this.console.warn("No node found to sync templates from!");
            this.console.warn("You can setup a node to pull templates from by set the node-property \"filenode\" to true!");
            this.console.warn("You can also setup multiple nodes to pull templates from. The cluster will use the node with the highest uptime!");
            this.console.warn("-----------------------------------------");
            this.firstTemplatePulled = true;
            getEventManager().postLocal(new FilePulledTemplatesEvent(null, false));
            runnable.run();
            return;
        }
        if (node == null) {
            this.console.info("No node found to sync templates from!");
            this.console.info("Skipping template sync!");
            this.firstTemplatePulled = true;
            getEventManager().postLocal(new FilePulledTemplatesEvent(null, false));
            runnable.run();
            return;
        }
        ICloudNode targetNode = node;
        this.console.info("Found node to sync templates from: %hc" + node.getName());
        this.serviceTemplateManager.pullTemplates(node)
                .onFailure(e -> {
                    this.console.error("Failed to pull templates from node " + targetNode.getName(), e);
                    this.firstTemplatePulled = true;
                    runnable.run();
                })
                .onSuccess(s -> {
                    this.console.info("Successfully pulled templates from node %hc" + targetNode.getName());
                    this.firstTemplatePulled = true;
                    runnable.run();
                });

    }

    private void handleProgrammArguments(String[] args) {
        if (args.length > 0) {
            for (String argument : args) {
                switch (argument.split("=")[0]) {
                    default: {
                        if (argument.split("=").length > 1) {
                            this.console.debug("Argument key: " + argument.split("=")[0]);
                            this.console.debug("Argument value: " + argument.split("=")[1]);
                        } else {
                            this.console.debug("Argument: " + argument);
                        }
                    }
                    case "--test": {
                        this.console.setLogLevel(LogLevel.TRACE);
                        this.console.setCleanConsoleMode(false);
                        continue;
                    }
                    case "--host":
                    case "--hostname": {
                        this.hostName = argument.split("=")[1];
                        this.console.info("Using hostname: " + this.hostName);
                        continue;
                    }
                    case "--test-console": {
                        this.console.disableColors();
                        continue;
                    }
                    case "--printstacktraces": {
                        this.console.setCleanConsoleMode(false);
                        continue;
                    }
                    case "--loglevel": {
                        LogLevel logLevel = LogLevel.valueOf(argument.split("=")[1]);
                        this.console.setLogLevel(logLevel);
                        continue;
                    }
                    case "--skiptempaltesync": {
                        this.skipTemplateSync = true;
                        continue;
                    }
                }
            }
        }
    }

    private void registerCommands() {
        this.console.info("Registering cloud commands for the console...");

        this.commandManager.getCommandCompletions().registerCompletion("services", context ->
                this.serviceManager.getServices().parallelStream()
                        .map(holder -> holder.getServiceName())
                        .collect(Collectors.toList()));
        this.commandManager.getCommandCompletions().registerCompletion("running_services", context ->
                this.serviceManager.getServices().parallelStream()
                        .filter(holder -> holder.getServiceState() != ServiceState.STOPPING)
                        .map(holder -> holder.getServiceName())
                        .collect(Collectors.toList()));
        this.commandManager.getCommandCompletions().registerCompletion("groups", context ->
                this.getGroupManager().getGroups().parallelStream()
                        .map(holder -> holder.getName())
                        .collect(Collectors.toList()));
        this.commandManager.getCommandCompletions().registerCompletion("service_versions", context ->
                this.getServiceVersionManager().getServiceVersions().parallelStream()
                        .map(holder -> holder.getName())
                        .collect(Collectors.toList()));
        this.commandManager.getCommandCompletions().registerCompletion("service_templates", context ->
                this.getServiceTemplateManager().getAllTemplates().parallelStream()
                        .map(holder -> holder.getName())
                        .collect(Collectors.toList()));
        this.commandManager.getCommandCompletions().registerCompletion("nodes", context ->
                this.getNodeManager().getNodes().parallelStream()
                        .map(holder -> holder.getName())
                        .collect(Collectors.toList()));
        this.commandManager.getCommandCompletions().registerCompletion("connected_nodes", context ->
                this.getNodeManager().getNodes().parallelStream()
                        .filter(holder -> holder.isConnected())
                        .map(holder -> holder.getName())
                        .collect(Collectors.toList()));
        this.commandManager.getCommandCompletions().registerAsyncCompletion("group_properties", context ->
                Arrays.stream(GroupProperty.values()).parallel().map(Enum::name).collect(Collectors.toList()));
        this.commandManager.getCommandCompletions().registerAsyncCompletion("service_version_properties", context ->
                Arrays.stream(ServiceVersionProperty.values()).parallel().map(Enum::name).collect(Collectors.toList()));

        if (CloudAPI.getInstance().getConsole().getLogLevel().getId() <= LogLevel.DEBUG.getId())
            this.commandManager.registerCommand(new DebugCommand());
        this.commandManager.registerCommand(new ClearCommand());
        this.commandManager.registerCommand(new StopCommand());
        this.commandManager.registerCommand(new ClusterCommand());
        this.commandManager.registerCommand(new TemplateCommand());
        this.commandManager.registerCommand(new ServiceCommand());
        this.commandManager.registerCommand(new GroupCommand());
        this.commandManager.registerCommand(new CloudHelpCommand());
        this.commandManager.registerCommand(new ServiceVersionCommand());
        this.commandManager.registerCommand(new VersionCommand());
        this.commandManager.registerCommand(new ScreenCommand());
    }

    private void registerPackets() {
        this.getPacketManager().registerPacket(NodePingPacket.class, PacketChannel.NODE);
        this.getPacketManager().registerPacket(NodePingPacketResponse.class, PacketChannel.NODE);
    }

    private void registerListeners() {
        this.getEventManager().register(new CloudNodeConnectedListener());
        this.getEventManager().register(new CloudNodeDisconnectListener());
        this.getEventManager().register(new CloudServiceStartedListener());
        this.getEventManager().register(new CloudServiceStoppedListener());
    }

    private void init(Runnable runnable) throws IOException {
        this.console.clearScreen();
        this.console.printCloudHeader(true);
        this.createDefaultFiles();
        this.initRedis(redisConnection -> this.initClusterConnection(node -> {
            this.networkComponentInfo = node.getNetworkComponentInfo();
            this.console.setMainPrefix(this.console.translateColorCodes("§b" + System.getProperty("user.name") + "§a@§c" + node.getName() + " §f=> "));
            this.console.resetPrefix();
            this.node = node;
            this.node.setTimeOut(0L);
            this.node.setLastConnection(System.currentTimeMillis());
            this.node.setLastStart(System.currentTimeMillis());
            this.node.setConnected(true);
            this.node.setCpuUsage(0);
            this.node.setStartedServiceUniqueIds(new ArrayList<>());
            this.node.setMemoryUsage(0);
            this.node.setHostname(this.hostName);
            this.node.setFilePath(Files.CLOUD_FOLDER.getFile().getAbsolutePath());

            this.serviceManager.checkOldService(this.node.getUniqueId());

            runnable.run();

            this.node.update();

            CloudNodeConnectedEvent event = new CloudNodeConnectedEvent();
            event.setNodeId(this.node.getUniqueId());
            getEventManager().postGlobalAsync(event);

            this.console.info(this.node.getName() + " is now connected to the cluster!");
            this.console.setMainPrefix(this.console.translateColorCodes("§b" + System.getProperty("user.name") + "§a@" + this.console.getHighlightColor() + this.node.getName() + " §f=> "));
        }));
    }

    private void initClusterConnection(Consumer<CloudNode> consumer) {
        String publicIp = IPUtils.getPublicIP();
        boolean firstClusterConnection = !Files.NODE_JSON.exists();
        if (firstClusterConnection) {
            try {
                Files.NODE_JSON.getFile().createNewFile();
            } catch (IOException e) {
                this.console.fatal("Failed to create node.json file", e);
                this.console.info("Stopping node in 10 seconds...");
                this.scheduler.runTaskLater(() -> {
                    this.console.info("Stopping node...");
                    this.shutdown(false);
                }, 10, TimeUnit.SECONDS);
                return;
            }
            new NodeConnectionDataSetup().start((setup, state) -> {
                if (state == SetupControlState.FINISHED) {
                    ClusterConnectionInformation connectionInformation = new ClusterConnectionInformation();
                    connectionInformation.setUniqueId(setup.getUniqueId());

                    if (getNodeManager().existsNode(connectionInformation.getUniqueId())) {
                        this.console.error("A Node with the same name already exists!");
                        this.console.error("Please choose a different name!");
                        this.console.info("Restarting cluster connection setup in 10 seconds...");
                        Files.NODE_JSON.getFile().delete();
                        this.scheduler.runTaskLater(() -> {
                            this.initClusterConnection(consumer);
                        }, 10, TimeUnit.SECONDS);
                        return;
                    }

                    this.getEventManager().register(CloudNodeConnectedEvent.class, event -> {
                        if (this.getNodeManager().getNodes().size() == 1) {
                            this.serviceVersionManager.installDefaultVersions(false);

                            if(!this.serviceTemplateManager.existsTemplate("global-minecraft"))
                                this.serviceTemplateManager.createTemplate("global-minecraft");
                            if(!this.serviceTemplateManager.existsTemplate("global-bungeecord"))
                                this.serviceTemplateManager.createTemplate("global-bungeecord");
                            if(!this.serviceTemplateManager.existsTemplate("global-velocity"))
                                this.serviceTemplateManager.createTemplate("global-velocity");
                            if(!this.serviceTemplateManager.existsTemplate("global-limbo"))
                                this.serviceTemplateManager.createTemplate("global-limbo");
                            if(!this.serviceTemplateManager.existsTemplate("global-all"))
                                this.serviceTemplateManager.createTemplate("global-all");

                            if(!this.getGroupManager().existsGroup("Fallback")){
                                CloudGroup group = new CloudGroup();
                                group.setUniqueId(UUID.randomUUID());
                                group.setPercentToStartNewService(100);
                                group.setFallback(true);
                                group.setMaxMemory(500);
                                group.setMaintenance(false);
                                group.setName("Fallback");
                                group.setMaxServices(1);
                                group.setMinServices(1);
                                group.setServiceVersionName("limbo");
                                group.setStartPriority(0);
                                group.setStatic(false);
                                group.setStartPort(25500);
                                group.setServiceEnvironment(ServiceEnvironment.LIMBO);
                                this.getGroupManager().createGroup(group);
                            }
                        }
                    });

                    CloudNode cloudNode = new CloudNode();
                    connectionInformation.applyToNode(cloudNode);
                    cloudNode.setName(setup.getName());
                    cloudNode.setFileNode(setup.isFileNode());
                    cloudNode.setHostname(publicIp);
                    cloudNode.setMaxParallelStartingServiceCount(setup.getMaxParallelServiceCount());
                    cloudNode.setMaxMemory(setup.getMaxMemory());
                    cloudNode.setMaxServiceCount(setup.getMaxServiceCount());
                    cloudNode.setTimeOut(0L);
                    FileWriter.writeObject(connectionInformation, Files.NODE_JSON.getFile());
                    cloudNode = (CloudNode) this.getNodeManager().createNode(cloudNode);
                    consumer.accept(cloudNode);

                }
            });
            return;
        }
        ClusterConnectionInformation connectionInformation;
        try {
            connectionInformation = FileWriter.readObject(Files.NODE_JSON.getFile(), ClusterConnectionInformation.class);
        } catch (Exception e) {
            this.console.error("Failed to read node.json file, but it's not the first cluster connection!");
            this.console.error("Starting cluster connection setup in 10 seconds...");
            Files.NODE_JSON.getFile().delete();
            this.scheduler.runTaskLater(() -> {
                this.initClusterConnection(consumer);
            }, 10, TimeUnit.SECONDS);
            return;
        }

        if (!this.getNodeManager().existsNode(connectionInformation.getUniqueId())) {
            this.console.error("The node.json file contains a node that doesn't exist!");
            this.console.error("Starting cluster connection setup in 10 seconds...");
            Files.NODE_JSON.getFile().delete();
            this.scheduler.runTaskLater(() -> {
                this.initClusterConnection(consumer);
            }, 10, TimeUnit.SECONDS);
            return;
        } else {
            this.node = (CloudNode) this.getNodeManager().getNode(connectionInformation.getUniqueId());
            if (this.node.getUniqueId().equals(connectionInformation.getUniqueId()) && this.node.isConnected()) {
                this.console.warn("A Node with the same uniqueId is already connected!");
                this.console.warn("Please check if this node is already running in the background!");
            }
            connectionInformation.applyToNode(this.node);
        }

        consumer.accept(this.node);
    }

    private void initRedisNodes(RedisCredentials redisCredentials, boolean first, Consumer<RedisConnection> consumer){
        new RedisClusterNodeSetup(first).start((setup, state) -> {
            if(state == SetupControlState.FINISHED){
                redisCredentials.getNodeAddresses().put(setup.getHostName(), setup.getPort());
                new RedisNewNodeQuestion().start((question, state1) -> {
                    if(state1 == SetupControlState.FINISHED){
                        if(question.isAddNewNode()){
                            this.initRedisNodes(redisCredentials, false, consumer);
                            return;
                        }
                    }
                    this.finishRedisInit(redisCredentials, consumer);
                });
            }else{
                this.finishRedisInit(redisCredentials, consumer);
            }
        });
    }

    private void finishRedisInit(RedisCredentials redisCredentials, Consumer<RedisConnection> consumer){
        try {
            this.redisConnection.connect();
            FileWriter.writeObject(redisCredentials, Files.REDIS_CONFIG.getFile());
            this.console.info("Redis connection established!");
            consumer.accept(this.redisConnection);
        } catch (Exception e) {
            this.console.error("§cFailed to connect to redis server. Please check your credentials.", e);
            this.console.info("Restarting redis setup in 10 seconds...");
            this.scheduler.runTaskLater(() -> {
                Files.REDIS_CONFIG.getFile().delete();
                initRedis(consumer);
            }, 10, TimeUnit.SECONDS);
        }
    }

    private void initRedis(Consumer<RedisConnection> consumer) {
        boolean redisConfigExits = Files.REDIS_CONFIG.exists();
        RedisCredentials redisCredentials = new RedisCredentials();
        if (!redisConfigExits) {

            new RedisGenerellSetup().start((generellSetup, state) -> {
                if(state == SetupControlState.FINISHED) {
                    redisCredentials.setPassword(generellSetup.getPassword());
                    redisCredentials.setType(generellSetup.getRedisType());
                    switch (generellSetup.getRedisType()){
                        case CLUSTER:
                            this.initRedisNodes(redisCredentials, true, consumer);
                            break;
                        case SINGLE_SERVICE:
                            new RedisSingleSetup().start((singleSetup, state1) -> {
                                if(state1 == SetupControlState.FINISHED){
                                    redisCredentials.getNodeAddresses()
                                            .put(singleSetup.getHostname(), singleSetup.getPort());
                                    redisCredentials.setDatabaseId(singleSetup.getDatabaseId());
                                    this.redisConnection = new RedisConnection(redisCredentials);
                                    try {
                                        this.redisConnection.connect();
                                        FileWriter.writeObject(redisCredentials, Files.REDIS_CONFIG.getFile());
                                        this.console.info("Redis connection established!");
                                        consumer.accept(this.redisConnection);
                                    } catch (Exception e) {
                                        this.console.error("§cFailed to connect to redis server. Please check your credentials.", e);
                                        this.console.info("Restarting redis setup in 10 seconds...");
                                        this.console.trace("URL: " + redisCredentials.getAnyNodeAddress());
                                        this.scheduler.runTaskLater(() -> {
                                            Files.REDIS_CONFIG.getFile().delete();
                                            initRedis(consumer);
                                        }, 10, TimeUnit.SECONDS);
                                    }
                                }
                            });
                            break;
                    }
                }
            });
        } else {
            RedisCredentials credentials;
            try {
                credentials = FileWriter.readObject(Files.REDIS_CONFIG.getFile(), RedisCredentials.class);
            } catch (Exception e) {
                this.console.error("Failed to read redis.json file! Please check your credentials.");
                this.console.error("Restarting redis setup in 10 seconds...");
                Files.REDIS_CONFIG.getFile().delete();
                this.scheduler.runTaskLater(() -> {
                    initRedis(consumer);
                }, 10, TimeUnit.SECONDS);
                return;
            }
            this.redisConnection = new RedisConnection(credentials);
            try {
                this.redisConnection.connect();
                this.console.info("Redis connection established!");
                consumer.accept(this.redisConnection);
            } catch (Exception e) {
                this.console.error("§cFailed to connect to redis server. Please check your credentials.", e);
                this.console.info("Trying to reconnect in 10 seconds...");
                this.scheduler.runTaskLater(() -> {
                    initRedis(consumer);
                }, 10, TimeUnit.SECONDS);
            }
        }
    }

    private void createDefaultFiles() throws IOException {
        this.console.info("Creating default folders...");

        if (Files.TEMP_FOLDER.exists()) FileUtils.deleteDirectory(Files.TEMP_FOLDER.getFile());

        Files.MODULES_FOLDER.createIfNotExists();
        Files.STORAGE_FOLDER.createIfNotExists();
        if (!Files.MINECRAFT_PLUGIN_JAR.exists()) {
            throw new FileNotFoundException("Minecraft plugin jar not found!");
        }
        if (!Files.BUNGEECORD_PLUGIN_JAR.exists()) {
            throw new FileNotFoundException("Proxy plugin jar not found!");
        }
        if(!Files.VELOCITY_PLUGIN_JAR.exists()){
            throw new FileNotFoundException("Velocity plugin jar not found!");
        }
        Files.LIBS_FOLDER.createIfNotExists();
        Files.LIBS_BLACKLIST_FOLDER.createIfNotExists();
        Files.LIBS_REPO_FOLDER.createIfNotExists();
        Files.TEMP_FOLDER.createIfNotExists();
        Files.STATIC_SERVICE_FOLDER.createIfNotExists();
        Files.TEMP_TRANSFER_FOLDER.createIfNotExists();
        Files.TEMP_SERVICE_FOLDER.createIfNotExists();
        Files.TEMP_VERSION_FOLDER.createIfNotExists();
        Files.TEMPLATE_FOLDER.createIfNotExists();
        Files.VERSIONS_FOLDER.createIfNotExists();
    }

    public void restartNode() {
        this.restarting = true;
        this.shutdown(false);
    }

    @Override
    public void shutdown(boolean fromHook) {
        if (this.shutdownInitialized) return;
        this.shutdownInitialized = true;

        if(this.console != null) {
            if(this.console.getCurrentSetup() != null) this.console.getCurrentSetup().exit(false);
        }

        String sleepArgument = "";
        if (this.node != null) {
            sleepArgument = isInstanceTimeOuted() ? " --sleep=" + (this.node.getTimeOut() - System.currentTimeMillis()) : "";

            if (this.isInstanceTimeOuted()) {
                if (this.console != null) this.console.info("Instance time out detected, shutting down...");
            }

            if (this.console != null) this.console.info("Disconnecting from cluster...");

            if (this.serviceManager != null) {

                int stopCount = 0;
                boolean isLastNode = this.getNodeManager().getNodes().parallelStream()
                        .filter(ICloudNode::isConnected)
                        .count() == 1;

                if (this.console != null) this.console.info("Stopping services...");
                for (ICloudService serviceHolder : this.serviceManager.getServices()) {
                    //TODO create event for cluster shutdown
                    if(serviceHolder.isExternal() && !isLastNode) continue;
                    if(!serviceHolder.getNodeId().equals(this.node.getUniqueId())) continue;
                    if (serviceHolder.getServiceState() == ServiceState.OFFLINE) continue;
                    if (!this.serviceManager.existsService(serviceHolder.getUniqueId())) continue;
                    stopCount++;
                    try {
                        this.serviceManager.stopServiceAsync(serviceHolder.getUniqueId(), false).get(10, TimeUnit.SECONDS);
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        try {
                            if (this.console != null)
                                this.console.warn("Failed to stop service " + serviceHolder.getServiceName() + "! Try to force stop...");
                            this.serviceManager.stopServiceAsync(serviceHolder.getUniqueId(), true).get(5, TimeUnit.SECONDS);
                        } catch (InterruptedException | ExecutionException | TimeoutException e1) {
                            if (this.console != null) {
                                this.console.error("Failed to stop service " + serviceHolder.getServiceName(), e1);
                            } else {
                                e1.printStackTrace();
                            }
                        }
                    }
                }
            }

            CloudNodeDisconnectEvent event = new CloudNodeDisconnectEvent();
            event.setNodeId(this.node.getUniqueId());
            getEventManager().postGlobal(event);
            this.node.setConnected(false);
            this.node.setLastDisconnect(System.currentTimeMillis());
            this.node.setStartedServiceUniqueIds(new ArrayList<>());
            this.node.setLastConnection(System.currentTimeMillis());
            this.node.update();
        }
        if (this.fileTransferManager != null) {
            if (this.console != null) this.console.info("Stopping file transfer manager...");
            this.fileTransferManager.getThread().interrupt();
        }
        if (this.redisConnection != null) {
            if (this.console != null) this.console.info("Disconnecting from redis...");
            this.redisConnection.disconnect();
        }
        String finalSleepArgument = sleepArgument;
        getScheduler().runTaskLater(() -> {
            if (this.console != null) this.console.info("Shutting down...");

            if (this.isRestarting()) {
                String startCommand = "java -jar " + this.node.getFilePath(Files.NODE_JAR) + (NodeLauncherMain.ARGUMENTS.length != 0 ? " " : "") + String.join(" ", NodeLauncherMain.ARGUMENTS) + finalSleepArgument;
                if (this.console != null) this.console.info("Restarting node with command: " + startCommand);
                try {
                    Runtime.getRuntime().exec(startCommand);
                } catch (IOException e) {
                    this.console.error("Failed to restart node: " + e);
                }
            }

            if (this.console != null) {
                this.console.info("Stopping node console thread...");
                if(this.console.getFileHandler() != null) this.console.getFileHandler().close();
                this.console.stopThread();
            }

            if (!fromHook) System.exit(0);
        }, 2, TimeUnit.SECONDS);
    }

    public boolean isInstanceTimeOuted() {
        return this.node.getTimeOut() > System.currentTimeMillis();
    }

    @Override
    public void updateApplicationProperties(CloudNode object) {
        if (!object.getNetworkComponentInfo().equals(this.networkComponentInfo)) return;

    }

    @Override
    public IRedisConnection getRedisConnection() {
        return this.redisConnection;
    }

    @Override
    public Scheduler getScheduler() {
        return this.scheduler;
    }

    @Override
    public CloudNode getNode() {
        return this.node;
    }
}
