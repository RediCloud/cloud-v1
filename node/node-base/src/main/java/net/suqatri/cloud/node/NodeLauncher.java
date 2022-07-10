package net.suqatri.cloud.node;

import lombok.Getter;
import net.suqatri.cloud.api.console.LogLevel;
import net.suqatri.cloud.api.node.NodeCloudDefaultAPI;
import net.suqatri.cloud.api.impl.redis.bucket.RBucketHolder;
import net.suqatri.cloud.api.node.event.CloudNodeConnectedEvent;
import net.suqatri.cloud.api.group.ICloudGroupManager;
import net.suqatri.cloud.api.impl.network.NetworkComponentInfo;
import net.suqatri.cloud.api.impl.node.CloudNode;
import net.suqatri.cloud.api.impl.node.CloudNodeManager;
import net.suqatri.cloud.api.impl.redis.RedisConnection;
import net.suqatri.cloud.api.network.NetworkComponentType;
import net.suqatri.cloud.api.node.event.CloudNodeDisconnectEvent;
import net.suqatri.cloud.api.player.ICloudPlayerManager;
import net.suqatri.cloud.api.redis.IRedisConnection;
import net.suqatri.cloud.api.redis.RedisCredentials;
import net.suqatri.cloud.api.redis.event.RedisConnectedEvent;
import net.suqatri.cloud.api.service.ICloudServiceManager;
import net.suqatri.cloud.api.service.factory.ICloudServiceFactory;
import net.suqatri.cloud.api.template.ICloudServiceTemplateManager;
import net.suqatri.cloud.commons.connection.IPUtils;
import net.suqatri.cloud.commons.file.FileWriter;
import net.suqatri.cloud.node.commands.ClearCommand;
import net.suqatri.cloud.node.commands.ClusterCommand;
import net.suqatri.cloud.node.commands.StopCommand;
import net.suqatri.cloud.node.console.CommandConsoleManager;
import net.suqatri.cloud.node.console.NodeConsole;
import net.suqatri.cloud.node.console.setup.SetupControlState;
import net.suqatri.cloud.node.listener.CloudNodeConnectedListener;
import net.suqatri.cloud.node.listener.CloudNodeDisconnectListener;
import net.suqatri.cloud.node.node.ClusterConnectionInformation;
import net.suqatri.cloud.node.scheduler.Scheduler;
import net.suqatri.cloud.node.setup.node.NodeConnectionDataSetup;
import net.suqatri.cloud.node.setup.redis.RedisSetup;
import net.suqatri.cloud.commons.file.Files;

import java.io.IOException;
import java.util.ArrayList;
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
    private RBucketHolder<CloudNode> cloudNode;
    private final CloudNodeManager nodeManager;
    private NetworkComponentInfo networkComponentInfo;
    private boolean shutdownInitialized = false;

    public NodeLauncher(String[] args) throws Exception{
        instance = this;
        this.scheduler = new Scheduler();
        this.commandManager = new CommandConsoleManager();
        this.console = this.commandManager.getNodeConsole();
        this.nodeManager = new CloudNodeManager();

        this.init(() -> {
            this.registerListeners();
            this.registerCommands();
        });
    }

    private void registerCommands(){
        this.console.info("Registering cloud commands for the console...");
        this.commandManager.registerCommand(new ClearCommand());
        this.commandManager.registerCommand(new StopCommand());
        this.commandManager.registerCommand(new ClusterCommand());
    }

    private void registerListeners(){
        this.getEventManager().register(new CloudNodeConnectedListener());
        this.getEventManager().register(new CloudNodeDisconnectListener());
    }

    private void init(Runnable runnable){
        this.createDefaultFiles();
        this.initRedis(redisConnection -> {
            this.initClusterConnection(node -> {
                this.console.setMainPrefix(this.console.translateColorCodes("§b" + System.getProperty("user.name") + "§a@§c" + node.get().getName() + " §f=> "));
                this.console.resetPrefix();
                this.cloudNode = node;
                this.cloudNode.get().setLastConnection(System.currentTimeMillis());
                this.cloudNode.get().setLastStart(System.currentTimeMillis());
                this.cloudNode.get().setConnected(true);
                this.cloudNode.get().setCpuUsage(0);
                this.cloudNode.get().setStartedServiceUniqueIds(new ArrayList<>());
                this.cloudNode.get().setMemoryUsage(0);
                this.cloudNode.get().setHostname(IPUtils.getPublicIP());

                runnable.run();

                RedisConnectedEvent redisConnectedEvent = new RedisConnectedEvent(this.redisConnection);
                getEventManager().postLocal(redisConnectedEvent);

                this.cloudNode.get().update();

                CloudNodeConnectedEvent event = new CloudNodeConnectedEvent();
                event.setCloudNodeId(this.cloudNode.get().getUniqueId());
                getEventManager().postGlobal(event);

                this.console.info(this.cloudNode.get().getName() + " is now connected to the cluster!");
                this.console.setMainPrefix(this.console.translateColorCodes("§b" + System.getProperty("user.name") + "§a@" + this.console.getHighlightColor() + node.get().getName() + " §f=> "));
            });
        });
    }

    private void initClusterConnection(Consumer<RBucketHolder<CloudNode>> consumer) {
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
            new NodeConnectionDataSetup(this.console).start((setup, state) -> {
                if(state == SetupControlState.FINISHED){
                    ClusterConnectionInformation connectionInformation = new ClusterConnectionInformation();
                    connectionInformation.setUniqueId(setup.getUniqueId());

                    if(this.nodeManager.existsNode(connectionInformation.getUniqueId())){
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
                    FileWriter.writeObject(connectionInformation, Files.NODE_JSON.getFile());
                    consumer.accept((RBucketHolder<CloudNode>) this.nodeManager.createNode(cloudNode));
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

        if(!this.nodeManager.existsNode(connectionInformation.getUniqueId())){
            CloudNode cloudNode = new CloudNode();
            connectionInformation.applyToNode(cloudNode);
            cloudNode.setConnected(true);
            cloudNode.setLastConnection(System.currentTimeMillis());
            cloudNode.setLastStart(System.currentTimeMillis());
            this.cloudNode = (RBucketHolder<CloudNode>) this.nodeManager.createNode(cloudNode);
        }else {
            this.cloudNode = (RBucketHolder<CloudNode>) this.nodeManager.getNode(connectionInformation.getUniqueId());
            if(this.cloudNode.get().getUniqueId().equals(connectionInformation.getUniqueId()) && this.cloudNode.get().isConnected()){
                this.console.warn("A Node with the same uniqueId is already connected!");
                this.console.warn("Please check if this node is already running in the background!");
            }
            connectionInformation.applyToNode(this.cloudNode.get());
        }

        this.networkComponentInfo = (NetworkComponentInfo) getNetworkComponentManager().getComponentInfo(NetworkComponentType.NODE, this.cloudNode.get().getUniqueId().toString());

        consumer.accept(this.cloudNode);
    }

    private void initRedis(Consumer<RedisConnection> consumer){
        boolean redisConfigExits = Files.REDIS_CONFIG.exists();
        if(!redisConfigExits){
            new RedisSetup(this.console).start(((redisSetup, state) -> {
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
                        this.console.info("§aRedis connection established!");
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
                this.console.info("§aRedis connection established!");
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
    }

    @Override
    public ICloudGroupManager getGroupManager() {
        return null;
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
    public ICloudServiceManager getServiceManager() {
        return null;
    }

    @Override
    public ICloudServiceTemplateManager getServiceTemplateManager() {
        return null;
    }

    @Override
    public void shutdown(boolean fromHook) {
        if(this.shutdownInitialized) return;
        this.shutdownInitialized = true;
        if(this.cloudNode != null){
            if(this.console != null) this.console.info("Disconnecting from cluster...");
            CloudNodeDisconnectEvent event = new CloudNodeDisconnectEvent();
            event.setCloudNodeId(this.cloudNode.get().getUniqueId());
            getEventManager().postGlobal(event);
            this.cloudNode.get().setConnected(false);
            this.cloudNode.get().setStartedServiceUniqueIds(new ArrayList<>());
            this.cloudNode.get().setLastConnection(System.currentTimeMillis());
            this.cloudNode.get().update();
        }
        getScheduler().runTaskLater(() -> {
            if(this.console != null) this.console.info("Disconnecting from redis...");
            if(this.redisConnection !=  null) this.redisConnection.disconnect();
            getScheduler().runTaskLater(() -> {
                if(this.console != null) this.console.info("Stopping node console thread...");
                if(this.console != null) this.console.stopThread();
                getScheduler().runTaskLater(() -> {
                    if(this.console != null) this.console.info("Shutting down...");
                    if(!fromHook) System.exit(0);
                }, 1, TimeUnit.SECONDS);
            }, 1, TimeUnit.SECONDS);
        }, 1, TimeUnit.SECONDS);
    }

    @Override
    public IRedisConnection getRedisConnection() {
        return this.redisConnection;
    }

    @Override
    public Scheduler getScheduler() {
        return this.scheduler;
    }

}
