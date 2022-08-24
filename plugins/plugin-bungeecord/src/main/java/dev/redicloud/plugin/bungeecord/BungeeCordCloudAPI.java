package dev.redicloud.plugin.bungeecord;

import dev.redicloud.dependency.DependencyLoader;
import dev.redicloud.plugin.bungeecord.command.BungeeCloudCommandManager;
import dev.redicloud.plugin.bungeecord.command.LoginCommand;
import dev.redicloud.plugin.bungeecord.command.LogoutCommand;
import dev.redicloud.plugin.bungeecord.command.RegisterCommand;
import dev.redicloud.plugin.bungeecord.console.ProxyConsole;
import dev.redicloud.plugin.bungeecord.listener.*;
import dev.redicloud.plugin.bungeecord.service.CloudBungeeServiceManager;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.impl.player.CloudPlayerManager;
import dev.redicloud.api.impl.redis.RedisConnection;
import dev.redicloud.api.impl.service.CloudService;
import dev.redicloud.api.impl.service.factory.CloudServiceFactory;
import dev.redicloud.api.impl.service.version.CloudServiceVersionManager;
import dev.redicloud.api.impl.template.CloudServiceTemplateManager;
import dev.redicloud.api.network.INetworkComponentInfo;
import dev.redicloud.api.bungeecord.ProxyDefaultCloudAPI;
import dev.redicloud.api.node.ICloudNode;
import dev.redicloud.api.redis.RedisCredentials;
import dev.redicloud.api.service.ICloudService;
import dev.redicloud.api.service.ServiceEnvironment;
import dev.redicloud.api.service.ServiceState;
import dev.redicloud.api.service.configuration.IServiceStartConfiguration;
import dev.redicloud.api.service.event.CloudServiceStartedEvent;
import dev.redicloud.api.service.event.CloudServiceStoppedEvent;
import dev.redicloud.api.utils.Files;
import dev.redicloud.commons.file.FileWriter;
import dev.redicloud.plugin.bungeecord.scheduler.BungeeScheduler;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Getter
public class BungeeCordCloudAPI extends ProxyDefaultCloudAPI {

    @Getter
    private static BungeeCordCloudAPI instance;

    private final Plugin plugin;
    private final CloudServiceFactory serviceFactory;
    private final CloudBungeeServiceManager serviceManager;
    private final CloudServiceVersionManager serviceVersionManager;
    private final CloudServiceTemplateManager serviceTemplateManager;
    private final ProxyConsole console;
    private final BungeeCloudCommandManager commandManager;
    private final BungeeScheduler scheduler;
    private final CloudPlayerManager playerManager;
    private CloudService service;
    private RedisConnection redisConnection;
    private ScheduledTask updaterTask;
    @Setter
    private String chatPrefix = "§bRedi§3Cloud §8» §f";
    @Setter
    private int onlineCount = 0;
    private boolean isShutdownInitiated = false;
    private boolean runningExternal = false;

    public BungeeCordCloudAPI(DependencyLoader dependencyLoader, Plugin plugin) {
        super(dependencyLoader);
        instance = this;
        this.plugin = plugin;
        this.scheduler = new BungeeScheduler(this.plugin);
        this.console = new ProxyConsole(this.plugin.getLogger());
        this.serviceManager = new CloudBungeeServiceManager();
        this.serviceFactory = new CloudServiceFactory(this.serviceManager);
        this.serviceVersionManager = new CloudServiceVersionManager();
        this.serviceTemplateManager = new CloudServiceTemplateManager();
        this.commandManager = new BungeeCloudCommandManager(this.plugin);
        this.playerManager = new CloudPlayerManager();

        if(!hasServiceId() && !hasGroupId()){
            this.console.fatal("Group id is not set as environment variable!", null);
            this.shutdown(false);
            return;
        }

        initRedis();
        registerInternalPackets();
        registerInternalListeners();
        initListeners();
        initCommands();
        initThisService();
        registerStartedService();
    }

    private void initCommands(){
        if(this.getPlayerManager().getConfiguration().isAllowCracked()){
            this.commandManager.registerCommand(new LoginCommand());
            this.commandManager.registerCommand(new RegisterCommand());
            this.commandManager.registerCommand(new LogoutCommand());
        }
    }

    private void initListeners() {
        ProxyServer.getInstance().getPluginManager().registerListener(this.plugin, new LoginListener());
        ProxyServer.getInstance().getPluginManager().registerListener(this.plugin, new ProxyPingListener());
        ProxyServer.getInstance().getPluginManager().registerListener(this.plugin, new ServerSwitchListener());
        ProxyServer.getInstance().getPluginManager().registerListener(this.plugin, new PlayerDisconnectListener());
        ProxyServer.getInstance().getPluginManager().registerListener(this.plugin, new ServerKickListener());
        ProxyServer.getInstance().getPluginManager().registerListener(this.plugin, new ServerConnectListener());
        ProxyServer.getInstance().getPluginManager().registerListener(this.plugin, new PostLoginListener());
        if(this.getPlayerManager().getConfiguration().isAllowCracked()){
            ProxyServer.getInstance().getPluginManager().registerListener(this.plugin, new PreLoginListener());
        }

        getEventManager().register(new CloudServiceStartedListener());
        getEventManager().register(new CloudServiceStoppedListener());
    }

    public void registerStartedService() {
        this.serviceManager.getServicesAsync()
                .onFailure(e -> this.console.error("Failed to register started service", e))
                .onSuccess(services -> {
                    ProxyServer.getInstance().getServers().clear();

                    for (ICloudService serviceHolder : services) {
                        if (serviceHolder.getEnvironment() == ServiceEnvironment.BUNGEECORD) continue;
                        if (serviceHolder.getEnvironment() == ServiceEnvironment.VELOCITY) continue;
                        ServerInfo serverInfo = ProxyServer.getInstance().constructServerInfo(
                                serviceHolder.getServiceName(),
                                new InetSocketAddress(serviceHolder.getHostName(), serviceHolder.getPort()),
                                serviceHolder.getMotd(),
                                false);
                        ProxyServer.getInstance().getServers().put(serverInfo.getName(), serverInfo);
                        CloudAPI.getInstance().getConsole().debug("Registered service: " + serverInfo.getName());
                    }

                    ServerInfo fallback = ProxyServer.getInstance().constructServerInfo(
                            "fallback",
                            new InetSocketAddress("127.0.0.1", 0),
                            "Fallback",
                            false);
                    ProxyServer.getInstance().getServers().put(fallback.getName(), fallback);
                    CloudAPI.getInstance().getConsole().debug("Registered service: " + fallback.getName());

                    ServerInfo lobby = ProxyServer.getInstance().constructServerInfo(
                            "lobby",
                            new InetSocketAddress("127.0.0.1", 0),
                            "lobby",
                            false);
                    ProxyServer.getInstance().getServers().put(lobby.getName(), lobby);
                    CloudAPI.getInstance().getConsole().debug("Registered service: " + lobby.getName());
                });
    }

    private List<String> getStartArguments(){
        List<String> list = new ArrayList<>();
        for (String s : System.getProperty("sun.java.command").split(" ")) {
            if(s.startsWith("--")){
                list.add(s.replaceFirst("--", ""));
            }
        }
        return list;
    }

    private UUID getServiceId(){
        return UUID.fromString(System.getenv("redicloud_service_id"));
    }

    private boolean hasServiceId(){
        return System.getenv().containsKey("redicloud_service_id");
    }

    private boolean isRunningExternal(){
        return (System.getenv().containsKey("redicloud_external")
                && System.getenv("redicloud_external").equals("true"))
                ||
                getStartArguments().parallelStream()
                        .anyMatch(s -> s.startsWith("redicloud_external=true"));
    }

    private UUID getGroupId() {
        if(System.getenv().containsKey("redicloud_group_id")) {
            return UUID.fromString(System.getenv("redicloud_group_id"));
        }
        for (String inputArgument : getStartArguments()) {
            if(inputArgument.startsWith("redicloud_group_id")){
                String[] split = inputArgument.split("=");
                if(split.length == 2){
                    return UUID.fromString(split[1]);
                }
            }
        }
        return null;
    }

    private boolean hasGroupId() {
        return System.getenv().containsKey("redicloud_group_id")
                || getStartArguments().parallelStream()
                .anyMatch(s -> s.startsWith("redicloud_group_id"));
    }

    private void initThisService() {

        this.service = null;

        if(CloudAPI.getInstance().getNodeManager().getNodes().parallelStream().noneMatch(ICloudNode::isConnected)) {
            this.console.fatal("Cluster seems to be offline! There are no connected nodes!", null);
            this.shutdown(false);
            return;
        }

        if(!hasServiceId()){
            this.runningExternal = true;
            if(this.getGroupManager().existsGroup(getGroupId())){

                IServiceStartConfiguration startConfiguration = this.getGroupManager()
                        .getGroup(this.getGroupId()).createServiceConfiguration();

                int id = 1;
                List<Integer> ids = new ArrayList<>();
                for (ICloudService serviceHolder : this.serviceManager.getServices()) {
                    if(serviceHolder.getServiceState() == ServiceState.OFFLINE && startConfiguration.isStatic()) continue;
                    if (serviceHolder.getGroupName().equalsIgnoreCase(startConfiguration.getGroupName())) {
                        ids.add(serviceHolder.getId());
                    }
                }
                while (ids.contains(id)) id++;

                this.service = new CloudService();
                this.service.setConfiguration(startConfiguration);
                this.service.getConfiguration().setId(id);
                this.service.setExternal(true);
                this.service.setServiceState(ServiceState.RUNNING_UNDEFINED);
                this.service.setMaxPlayers(50);
                this.service.setMotd("§7•§8● §bRedi§3Cloud §8» §fA §bredis §fbased §bcluster §fcloud system§r\n    §b§l§8× §fDiscord §8➜ §3https://discord.gg/g2HV52VV4G");
                this.service.setNodeId(null);
                this.service = (CloudService) this.serviceManager.createBucket(this.service.getUniqueId().toString(), this.service);
            }else{
                this.console.fatal("No target group found for external service " + getServiceId(), null);
                shutdown(false);
                return;
            }
        }else{
            service = this.serviceManager.getBucketImpl(getServiceId().toString());
            if(service.getServiceState() == ServiceState.RUNNING_DEFINED || service.getServiceState() == ServiceState.RUNNING_UNDEFINED){
                this.runningExternal = true;
                this.console.fatal("Can´t run external service because " + getServiceId() + " is already running!", null);
                shutdown(false);
                return;
            }
        }

        this.console.debug("ServiceId: " + (hasServiceId() ? getServiceId() : "not set"));

        getEventManager().postGlobalAsync(new CloudServiceStartedEvent(this.service));

        this.updaterTask = ProxyServer.getInstance().getScheduler().schedule(this.plugin, () -> {
            if (this.service.getOnlineCount() != ProxyServer.getInstance().getOnlineCount()) {
                this.service.setOnlineCount(ProxyServer.getInstance().getOnlineCount());
                this.service.setLastPlayerAction(System.currentTimeMillis());

                this.service.updateAsync();
                this.console.trace("Service " + this.service.getServiceName() + " updated");
            }
        }, 1500, 1500, TimeUnit.MILLISECONDS);
    }

    private void initRedis() {
        RedisCredentials redisCredentials;
        try {
            this.console.debug("Using redis config file: " + Files.REDIS_CONFIG.getFile().getAbsolutePath());
            redisCredentials = FileWriter.readObject(Files.REDIS_CONFIG.getFile(), RedisCredentials.class);
        } catch (Exception e) {
            this.console.error("Failed to read redis.json file! Please check your credentials.");
            return;
        }
        this.redisConnection = new RedisConnection(redisCredentials);
        try {
            this.redisConnection.connect();
            this.console.info("Redis connection established!");
        } catch (Exception e) {
            this.console.error("§cFailed to connect to redis server. Please check your credentials.", e);
        }
    }

    @Override
    public void updateApplicationProperties(CloudService object) {
        if(this.service == null) return;
        if (!object.getUniqueId().equals(this.service.getUniqueId())) return;
    }

    @Override
    public INetworkComponentInfo getNetworkComponentInfo() {
        return this.service.getNetworkComponentInfo();
    }

    @Override
    public void shutdown(boolean fromHook) {
        if(this.isShutdownInitiated) return;
        this.isShutdownInitiated = true;

        if(this.getModuleHandler() != null){
            this.getModuleHandler().unloadModules();
        }

        if(this.service != null){
            this.service.setServiceState(ServiceState.STOPPING);
            this.service.update();
        }

        for (ProxiedPlayer onlinePlayer : ProxyServer.getInstance().getPlayers()) {
            onlinePlayer.disconnect("§cServer is shutting down.");
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }

        if(this.service != null){
            if(this.service.isExternal()){
                CloudAPI.getInstance().getEventManager().postGlobalAsync(new CloudServiceStoppedEvent(this.service));
            }
        }

        if (this.updaterTask != null) this.updaterTask.cancel();

        if(this.service != null) {
            this.service.setServiceState(ServiceState.STOPPING);
            this.service.update();
        }

        if (this.redisConnection != null) this.redisConnection.disconnect();

        ProxyServer.getInstance().stop();
    }
}
