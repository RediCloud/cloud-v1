package net.suqatri.cloud.node;

import lombok.Getter;
import net.suqatri.api.node.NodeCloudDefaultAPI;
import net.suqatri.cloud.api.group.ICloudGroupManager;
import net.suqatri.cloud.api.impl.node.CloudNode;
import net.suqatri.cloud.api.impl.node.CloudNodeManager;
import net.suqatri.cloud.api.impl.redis.RedisConnection;
import net.suqatri.cloud.api.packet.ICloudPacketManager;
import net.suqatri.cloud.api.player.ICloudPlayerManager;
import net.suqatri.cloud.api.redis.IRedisConnection;
import net.suqatri.cloud.api.redis.RedisCredentials;
import net.suqatri.cloud.api.service.ICloudServiceManager;
import net.suqatri.cloud.api.service.factory.ICloudServiceFactory;
import net.suqatri.cloud.api.template.ICloudServiceTemplateManager;
import net.suqatri.cloud.commons.connection.IPUtils;
import net.suqatri.cloud.commons.file.FileWriter;
import net.suqatri.cloud.node.commands.ClearCommand;
import net.suqatri.cloud.node.console.CommandConsoleManager;
import net.suqatri.cloud.node.console.NodeConsole;
import net.suqatri.cloud.node.console.setup.SetupControlState;
import net.suqatri.cloud.node.node.ClusterConnectionInformation;
import net.suqatri.cloud.node.scheduler.Scheduler;
import net.suqatri.cloud.node.setup.node.NodeConnectionDataSetup;
import net.suqatri.cloud.node.setup.redis.RedisSetup;
import net.suqatri.cloud.commons.file.Files;

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
    private CloudNode cloudNode;
    private final CloudNodeManager nodeManager;

    public NodeLauncher(String[] args) throws Exception{
        instance = this;
        this.scheduler = new Scheduler();
        this.commandManager = new CommandConsoleManager();
        this.console = this.commandManager.getNodeConsole();
        this.nodeManager = new CloudNodeManager();

        this.init();

        this.registerCommands();
    }

    private void registerCommands(){
        this.commandManager.registerCommand(new ClearCommand());
    }

    private void init(){
        this.createDefaultFiles();
        this.initRedis(redisConnection -> {
            this.initClusterConnection(node -> {
                this.cloudNode = node;
                this.cloudNode.setConnected(true);
                this.cloudNode.setCpuUsage(0);
                this.cloudNode.setStartedServiceUniqueIds(new ArrayList<>());
                this.cloudNode.setMemoryUsage(0);
                this.cloudNode.setHostname(IPUtils.getPublicIP());
                this.cloudNode.update();

                this.console.info(this.cloudNode.getName() + " is now connected to the cluster!");
            });
        });
    }

    private void initClusterConnection(Consumer<CloudNode> consumer) {
        String publicIp = IPUtils.getPublicIP(); //Load public ip
        boolean firstClusterConnection = !Files.NODE_JSON.exists();
        if(firstClusterConnection){
            try {
                Files.NODE_JSON.getFile().createNewFile();
                new NodeConnectionDataSetup(this.console).start((setup, state) -> {
                    if(state == SetupControlState.FINISHED){
                        ClusterConnectionInformation connectionInformation = new ClusterConnectionInformation();
                        connectionInformation.setMaxMemory(setup.getMaxMemory());
                        connectionInformation.setMaxParallelServiceCount(setup.getMaxParallelServiceCount());
                        connectionInformation.setName(setup.getName());
                        connectionInformation.setUniqueId(setup.getUniqueId());
                        connectionInformation.setHostName(publicIp);
                        connectionInformation.setMaxServiceCount(setup.getMaxServiceCount());

                        if(this.nodeManager.existsNode(connectionInformation.getUniqueId())){
                            this.console.error("A Node with the same name already exists!");
                            this.console.error("Please choose a different name!");
                            this.console.info("Restarting cluster connection setup in 5 seconds...");
                            this.scheduler.runTaskLater(() -> {
                                this.initClusterConnection(consumer);
                            }, 5, TimeUnit.SECONDS);
                            return;
                        }

                        connectionInformation.applyToNode(this.cloudNode);
                        this.cloudNode.setConnected(true);
                        this.nodeManager.createNode(this.cloudNode);
                        consumer.accept(this.cloudNode);
                    }
                });
            }catch (Exception e){
                this.console.fatal("Failed to create node.json file", e);
                this.console.info("Stopping node in 5 seconds...");
                this.scheduler.runTaskLater(() -> {
                    this.console.info("Stopping node...");
                    this.shutdown();
                }, 5, TimeUnit.SECONDS);
            }
            return;
        }

        this.cloudNode = new CloudNode();
        ClusterConnectionInformation connectionInformation = FileWriter.readObject(Files.NODE_JSON.getFile(), ClusterConnectionInformation.class);
        connectionInformation.applyToNode(this.cloudNode);
        this.cloudNode.setConnected(true);

        if(!this.nodeManager.existsNode(connectionInformation.getUniqueId())){
            this.nodeManager.createNode(this.cloudNode);
        }else{
            this.cloudNode.update();
        }

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
                        consumer.accept(this.redisConnection);
                    }catch (Exception e){
                        this.console.error("§cFailed to connect to redis server. Please check your credentials.", e);
                        this.console.info("Restarting redis setup in 3 seconds...");
                        this.scheduler.runTaskLater(() -> {
                            initRedis(consumer);
                        }, 3, TimeUnit.SECONDS);
                    }
                }
            }));
        }else{
            RedisCredentials credentials = FileWriter.readObject(Files.REDIS_CONFIG.getFile(), RedisCredentials.class);
            this.redisConnection = new RedisConnection(credentials);
            try {
                this.redisConnection.connect();
                this.console.info("§aRedis connection established!");
                consumer.accept(this.redisConnection);
            }catch (Exception e){
                this.console.error("§cFailed to connect to redis server. Please check your credentials.", e);
                this.console.info("Trying to reconnect in 5 seconds...");
                this.scheduler.runTaskLater(() -> {
                    initRedis(consumer);
                }, 5, TimeUnit.SECONDS);
            }
        }
    }

    private void createDefaultFiles(){
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
    public ICloudPacketManager getPacketManager() {
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
    public IRedisConnection getRedisConnection() {
        return this.redisConnection;
    }

    @Override
    public Scheduler getScheduler() {
        return this.scheduler;
    }

    @Override
    public void shutdown() {
        if(this.redisConnection !=  null) this.redisConnection.disconnect();
        if(this.console != null) this.console.stopThread();
    }
}
