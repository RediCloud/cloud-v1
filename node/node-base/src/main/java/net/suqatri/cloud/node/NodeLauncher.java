package net.suqatri.cloud.node;

import lombok.Getter;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.console.LogLevel;
import net.suqatri.cloud.api.impl.template.CloudServiceTemplateManager;
import net.suqatri.cloud.api.network.INetworkComponentInfo;
import net.suqatri.cloud.api.node.ICloudNode;
import net.suqatri.cloud.api.node.NodeCloudDefaultAPI;
import net.suqatri.cloud.api.node.event.CloudNodeConnectedEvent;
import net.suqatri.cloud.api.impl.node.CloudNode;
import net.suqatri.cloud.api.impl.redis.RedisConnection;
import net.suqatri.cloud.api.node.event.CloudNodeDisconnectEvent;
import net.suqatri.cloud.api.player.ICloudPlayerManager;
import net.suqatri.cloud.api.redis.IRedisConnection;
import net.suqatri.cloud.api.redis.RedisCredentials;
import net.suqatri.cloud.api.redis.event.RedisConnectedEvent;
import net.suqatri.cloud.api.service.factory.ICloudServiceFactory;
import net.suqatri.cloud.api.template.ICloudServiceTemplateManager;
import net.suqatri.cloud.commons.connection.IPUtils;
import net.suqatri.cloud.commons.file.FileWriter;
import net.suqatri.cloud.node.commands.*;
import net.suqatri.cloud.node.console.CommandConsoleManager;
import net.suqatri.cloud.node.console.NodeConsole;
import net.suqatri.cloud.node.console.setup.SetupControlState;
import net.suqatri.cloud.node.file.FileTransferManager;
import net.suqatri.cloud.node.listener.CloudNodeConnectedListener;
import net.suqatri.cloud.node.listener.CloudNodeDisconnectListener;
import net.suqatri.cloud.node.node.ClusterConnectionInformation;
import net.suqatri.cloud.node.scheduler.Scheduler;
import net.suqatri.cloud.node.setup.node.NodeConnectionDataSetup;
import net.suqatri.cloud.node.setup.redis.RedisSetup;
import net.suqatri.cloud.commons.file.Files;
import net.suqatri.cloud.node.template.NodeCloudServiceTemplateManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Getter
public class NodeLauncher extends NodeCloudDefaultAPI {

    @Getter
    private static NodeLauncher instance;

    private final NodeConsole console;
    private final CommandConsoleManager commandManager;
    private RedisConnection redisConnection;
    private final Scheduler scheduler;
    private INetworkComponentInfo networkComponentInfo;
    private boolean shutdownInitialized = false;
    private CloudNode node;
    private FileTransferManager fileTransferManager;
    private NodeCloudServiceTemplateManager serviceTemplateManager;

    public NodeLauncher(String[] args) throws Exception{
        instance = this;
        this.scheduler = new Scheduler();
        this.commandManager = new CommandConsoleManager();
        this.console = this.commandManager.getNodeConsole();
        this.handleProgrammArguments(args);

        this.init(() -> {
            this.fileTransferManager = new FileTransferManager();
            this.serviceTemplateManager = new NodeCloudServiceTemplateManager();
            this.registerInternalListeners();
            this.registerListeners();
            this.registerCommands();
        });
    }

    private void handleProgrammArguments(String[] args) {
        if(args.length > 0){
            for (String argument : args) {
                switch (argument.toLowerCase()){
                    case "--printstacktraces": {
                        this.console.setCleanConsoleMode(false);
                        continue;
                    }
                    case "--loglevel=debug": {
                        this.console.setLogLevel(LogLevel.DEBUG);
                        continue;
                    }
                    case "--loglevel=info": {
                        this.console.setLogLevel(LogLevel.INFO);
                        continue;
                    }
                    case "--loglevel=warn": {
                        this.console.setLogLevel(LogLevel.WARN);
                        continue;
                    }
                    case "--loglevel=error": {
                        this.console.setLogLevel(LogLevel.ERROR);
                        continue;
                    }
                    case "--loglevel=fatal": {
                        this.console.setLogLevel(LogLevel.FATAL);
                        continue;
                    }
                }
            }
        }
    }

    private void registerCommands(){
        this.console.info("Registering cloud commands for the console...");
        if(CloudAPI.getInstance().getConsole().canLog(LogLevel.DEBUG)) this.commandManager.registerCommand(new DebugCommand());
        this.commandManager.registerCommand(new ClearCommand());
        this.commandManager.registerCommand(new StopCommand());
        this.commandManager.registerCommand(new ClusterCommand());
        this.commandManager.registerCommand(new TemplateCommand());
        this.commandManager.registerCommand(new ServiceCommand());
        this.commandManager.registerCommand(new GroupCommand());
    }

    private void registerListeners(){
        this.getEventManager().register(new CloudNodeConnectedListener());
        this.getEventManager().register(new CloudNodeDisconnectListener());
    }

    private void init(Runnable runnable){
        this.console.printCloudHeader();
        this.createDefaultFiles();
        this.initRedis(redisConnection -> this.initClusterConnection(node -> {
            this.networkComponentInfo = node.getNetworkComponentInfo();
            this.console.setMainPrefix(this.console.translateColorCodes("§b" + System.getProperty("user.name") + "§a@§c" + node.getName() + " §f=> "));
            this.console.resetPrefix();
            this.node = node;
            this.node.setLastConnection(System.currentTimeMillis());
            this.node.setLastStart(System.currentTimeMillis());
            this.node.setConnected(true);
            this.node.setCpuUsage(0);
            this.node.setStartedServiceUniqueIds(new ArrayList<>());
            this.node.setMemoryUsage(0);
            this.node.setHostname(IPUtils.getPublicIP());

            runnable.run();

            RedisConnectedEvent redisConnectedEvent = new RedisConnectedEvent(this.redisConnection);
            getEventManager().postLocal(redisConnectedEvent);

            this.node.update();

            CloudNodeConnectedEvent event = new CloudNodeConnectedEvent();
            event.setNodeId(this.node.getUniqueId());
            getEventManager().postGlobal(event);

            this.console.info(this.node.getName() + " is now connected to the cluster!");
            this.console.setMainPrefix(this.console.translateColorCodes("§b" + System.getProperty("user.name") + "§a@" + this.console.getHighlightColor() + this.node.getName() + " §f=> "));
        }));
    }

    private void initClusterConnection(Consumer<CloudNode> consumer) {
        String publicIp = IPUtils.getPublicIP();
        boolean firstClusterConnection = !Files.NODE_JSON.exists();
        if(firstClusterConnection){
            try {
                Files.NODE_JSON.getFile().createNewFile();
            }catch (IOException e){
                this.console.fatal("Failed to create node.json file", e);
                this.console.info("Stopping node in 5 seconds...");
                this.scheduler.runTaskLater(() -> {
                    this.console.info("Stopping node...");
                    this.shutdown(false);
                }, 5, TimeUnit.SECONDS);
                return;
            }
            new NodeConnectionDataSetup().start((setup, state) -> {
                if(state == SetupControlState.FINISHED){
                    ClusterConnectionInformation connectionInformation = new ClusterConnectionInformation();
                    connectionInformation.setUniqueId(setup.getUniqueId());

                    if(getNodeManager().existsNode(connectionInformation.getUniqueId())){
                        this.console.error("A Node with the same name already exists!");
                        this.console.error("Please choose a different name!");
                        this.console.log(LogLevel.INFO, "Restarting cluster connection setup in 5 seconds...", true, true);
                        this.scheduler.runTaskLater(() -> {
                            Files.NODE_JSON.getFile().delete();
                            this.initClusterConnection(consumer);
                        }, 5, TimeUnit.SECONDS);
                        return;
                    }

                    CloudNode cloudNode = new CloudNode();
                    connectionInformation.applyToNode(cloudNode);
                    cloudNode.setName(setup.getName());
                    cloudNode.setHostname(publicIp);
                    cloudNode.setMaxParallelStartingServiceCount(setup.getMaxParallelServiceCount());
                    cloudNode.setMaxMemory(setup.getMaxMemory());
                    cloudNode.setMaxServiceCount(setup.getMaxServiceCount());
                    FileWriter.writeObject(connectionInformation, Files.NODE_JSON.getFile());
                    cloudNode = this.getNodeManager().createNode(cloudNode).getImpl(CloudNode.class);
                    //TODO pull templates
                    consumer.accept(cloudNode);
                }
            });
            return;
        }
        ClusterConnectionInformation connectionInformation;
        try {
            connectionInformation = FileWriter.readObject(Files.NODE_JSON.getFile(), ClusterConnectionInformation.class);
        }catch (Exception e){
            this.console.error("Failed to read node.json file, but it's not the first cluster connection!");
            this.console.error("Starting cluster connection setup in 5 seconds...");
            this.scheduler.runTaskLater(() -> {
                Files.NODE_JSON.getFile().delete();
                this.initClusterConnection(consumer);
            }, 5, TimeUnit.SECONDS);
            return;
        }

        if(!this.getNodeManager().existsNode(connectionInformation.getUniqueId())){
            CloudNode cloudNode = new CloudNode();
            connectionInformation.applyToNode(cloudNode);
            cloudNode.setConnected(true);
            cloudNode.setLastConnection(System.currentTimeMillis());
            cloudNode.setLastStart(System.currentTimeMillis());
            this.node = this.getNodeManager().createNode(cloudNode).getImpl(CloudNode.class);
        }else {
            this.node = this.getNodeManager().getNode(connectionInformation.getUniqueId()).getImpl(CloudNode.class);
            if(this.node.getUniqueId().equals(connectionInformation.getUniqueId()) && this.node.isConnected()){
                this.console.warn("A Node with the same uniqueId is already connected!");
                this.console.warn("Please check if this node is already running in the background!");
            }
            connectionInformation.applyToNode(this.node);
        }

        consumer.accept(this.node);
    }

    private void initRedis(Consumer<RedisConnection> consumer){
        boolean redisConfigExits = Files.REDIS_CONFIG.exists();
        if(!redisConfigExits){
            new RedisSetup().start(((redisSetup, state) -> {
                if(state == SetupControlState.FINISHED){
                    RedisCredentials credentials = new RedisCredentials();
                    credentials.setHostname(redisSetup.getHostname());
                    credentials.setPort(redisSetup.getPort());
                    credentials.setPassword(redisSetup.getPassword());
                    credentials.setDatabaseId(redisSetup.getDatabaseId());
                    this.redisConnection = new RedisConnection(credentials);
                    try {
                        this.redisConnection.connect();
                        FileWriter.writeObject(credentials, Files.REDIS_CONFIG.getFile());
                        this.console.info("Redis connection established!");
                    }catch (Exception e){
                        this.console.error("§cFailed to connect to redis server. Please check your credentials.", e);
                        this.console.info("Restarting redis setup in 5 seconds...");
                        this.scheduler.runTaskLater(() -> {
                            Files.REDIS_CONFIG.getFile().delete();
                            initRedis(consumer);
                        }, 5, TimeUnit.SECONDS);
                        return;
                    }
                    consumer.accept(this.redisConnection);
                }
            }));
        }else{
            RedisCredentials credentials;
            try {
                credentials = FileWriter.readObject(Files.REDIS_CONFIG.getFile(), RedisCredentials.class);
            }catch (Exception e){
                this.console.error("Failed to read redis.json file! Please check your credentials.");
                this.console.error("Restarting redis setup in 5 seconds...");
                this.scheduler.runTaskLater(() -> {
                    Files.REDIS_CONFIG.getFile().delete();
                    initRedis(consumer);
                }, 5, TimeUnit.SECONDS);
                return;
            }
            this.redisConnection = new RedisConnection(credentials);
            try {
                this.redisConnection.connect();
                this.console.info("Redis connection established!");
            }catch (Exception e){
                this.console.error("§cFailed to connect to redis server. Please check your credentials.", e);
                this.console.info("Trying to reconnect in 5 seconds...");
                this.scheduler.runTaskLater(() -> {
                    initRedis(consumer);
                }, 5, TimeUnit.SECONDS);
                return;
            }
            consumer.accept(this.redisConnection);
        }
    }

    private void createDefaultFiles(){
        this.console.info("Creating default folders...");
        Files.MODULES_FOLDER.createIfNotExists();
        Files.STORAGE_FOLDER.createIfNotExists();
        if(!Files.MINECRAFT_PLUGIN_JAR.exists()){
            Files.MINECRAFT_PLUGIN_JAR.downloadFromUrl("");
        }
        if(!Files.PROXY_PLUGIN_JAR.exists()){
            Files.PROXY_PLUGIN_JAR.downloadFromUrl("");
        }
        Files.LIBS_FOLDER.createIfNotExists();
        Files.LIBS_BLACKLIST_FOLDER.createIfNotExists();
        Files.LIBS_REPO_FOLDER.createIfNotExists();
        Files.TEMP_FOLDER.createIfNotExists();
        Files.TEMPLATE_FOLDER.createIfNotExists();
        Files.VERSIONS_FOLDER.createIfNotExists();
    }

    @Override
    public void shutdown(boolean fromHook) {
        if (this.shutdownInitialized) return;

        this.shutdownInitialized = true;
        if (this.node != null) {
            if (this.console != null) this.console.info("Disconnecting from cluster...");
            CloudNodeDisconnectEvent event = new CloudNodeDisconnectEvent();
            event.setNodeId(this.node.getUniqueId());
            getEventManager().postGlobal(event);
            this.node.setConnected(false);
            this.node.setStartedServiceUniqueIds(new ArrayList<>());
            this.node.setLastConnection(System.currentTimeMillis());
            this.node.update();
        }
        if (this.fileTransferManager != null) {
            this.console.info("Stopping file transfer manager...");
            this.fileTransferManager.getThread().interrupt();
        }
        if (this.redisConnection != null) {
            this.console.info("Disconnecting from redis...");
            this.redisConnection.disconnect();
         }
        if(this.console != null) {
            this.console.info("Stopping node console thread...");
            this.console.stopThread();
        }
        getScheduler().runTaskLater(() -> {
            if(this.console != null) this.console.info("Shutting down...");
            if(!fromHook) System.exit(0);
        }, 2, TimeUnit.SECONDS);
    }

    @Override
    public void updateApplicationProperties(CloudNode object) {
        if(!object.getNetworkComponentInfo().equals(this.networkComponentInfo)) return;
        //TODO: Update application properties
    }

    @Override
    public IRedisConnection getRedisConnection() {
        return this.redisConnection;
    }

    @Override
    public ICloudPlayerManager getPlayerManager() {
        return null;
    }

    @Override
    public ICloudServiceFactory getServiceFactory() {
        return null;
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
