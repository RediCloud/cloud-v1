package net.suqatri.redicloud.plugin.proxy;

import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.CloudDefaultAPIImpl;
import net.suqatri.redicloud.api.impl.group.CloudGroup;
import net.suqatri.redicloud.api.impl.player.CloudPlayerManager;
import net.suqatri.redicloud.api.impl.redis.RedisConnection;
import net.suqatri.redicloud.api.impl.service.CloudService;
import net.suqatri.redicloud.api.impl.service.CloudServiceManager;
import net.suqatri.redicloud.api.impl.service.factory.CloudServiceFactory;
import net.suqatri.redicloud.api.impl.service.version.CloudServiceVersionManager;
import net.suqatri.redicloud.api.impl.template.CloudServiceTemplateManager;
import net.suqatri.redicloud.api.network.INetworkComponentInfo;
import net.suqatri.redicloud.api.redis.RedisCredentials;
import net.suqatri.redicloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.api.service.ServiceEnvironment;
import net.suqatri.redicloud.api.service.ServiceState;
import net.suqatri.redicloud.api.service.configuration.IServiceStartConfiguration;
import net.suqatri.redicloud.api.service.event.CloudServiceStartedEvent;
import net.suqatri.redicloud.api.utils.ApplicationType;
import net.suqatri.redicloud.api.utils.Files;
import net.suqatri.redicloud.commons.file.FileWriter;
import net.suqatri.redicloud.plugin.proxy.command.BungeeCloudCommandManager;
import net.suqatri.redicloud.plugin.proxy.console.ProxyConsole;
import net.suqatri.redicloud.plugin.proxy.listener.*;
import net.suqatri.redicloud.plugin.proxy.scheduler.BungeeScheduler;
import net.suqatri.redicloud.plugin.proxy.service.CloudProxyServiceManager;
import org.checkerframework.checker.units.qual.C;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Getter
public class ProxyCloudAPI extends CloudDefaultAPIImpl<CloudService> {

    @Getter
    private static ProxyCloudAPI instance;

    private final Plugin plugin;
    private final CloudServiceFactory serviceFactory;
    private final CloudProxyServiceManager serviceManager;
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

    public ProxyCloudAPI(Plugin plugin) {
        super(ApplicationType.SERVICE_PROXY);
        instance = this;
        this.plugin = plugin;
        this.scheduler = new BungeeScheduler(this.plugin);
        this.console = new ProxyConsole(this.plugin.getLogger());
        this.serviceManager = new CloudProxyServiceManager();
        this.serviceFactory = new CloudServiceFactory(this.serviceManager);
        this.serviceVersionManager = new CloudServiceVersionManager();
        this.serviceTemplateManager = new CloudServiceTemplateManager();
        this.commandManager = new BungeeCloudCommandManager(this.plugin);
        this.playerManager = new CloudPlayerManager();

        if(!hasRedisFilePath()){
            this.console.fatal("Redis file path is not set as environment variable!", null);
            this.shutdown(false);
            return;
        }
        if(!hasGroupId()){
            this.console.fatal("Group id is not set as environment variable!", null);
            this.shutdown(false);
            return;
        }

        initRedis();
        registerInternalPackets();
        registerInternalListeners();
        initListeners();
        initThisService();
    }

    private void initListeners() {
        ProxyServer.getInstance().getPluginManager().registerListener(this.plugin, new LoginListener());
        ProxyServer.getInstance().getPluginManager().registerListener(this.plugin, new ProxyPingListener());
        ProxyServer.getInstance().getPluginManager().registerListener(this.plugin, new ServerSwitchListener());
        ProxyServer.getInstance().getPluginManager().registerListener(this.plugin, new PlayerDisconnectListener());
        ProxyServer.getInstance().getPluginManager().registerListener(this.plugin, new ServerKickListener());
        ProxyServer.getInstance().getPluginManager().registerListener(this.plugin, new ServerConnectListener());
        ProxyServer.getInstance().getPluginManager().registerListener(this.plugin, new PostLoginListener());

        getEventManager().register(new CloudServiceStartedListener());
        getEventManager().register(new CloudServiceStoppedListener());
    }

    void registerStartedService() {
        this.serviceManager.getServicesAsync()
                .onFailure(e -> this.console.error("Failed to register started service", e))
                .onSuccess(serviceHolders -> {
                    ProxyServer.getInstance().getServers().clear();

                    for (IRBucketHolder<ICloudService> serviceHolder : serviceHolders) {
                        if (serviceHolder.get().getEnvironment() == ServiceEnvironment.PROXY) continue;
                        ServerInfo serverInfo = ProxyServer.getInstance().constructServerInfo(
                                serviceHolder.get().getServiceName(),
                                new InetSocketAddress(serviceHolder.get().getHostName(), serviceHolder.get().getPort()),
                                serviceHolder.get().getMotd(),
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

    private UUID getServiceId(){
        return UUID.fromString(System.getenv("redicloud_service_id"));
    }

    private boolean hasServiceId(){
        return System.getenv().containsKey("redicloud_service_id");
    }

    private String getRedisFilePath(){
        return System.getenv("redicloud_files_" + Files.REDIS_CONFIG.name().toLowerCase());
    }

    private boolean hasRedisFilePath(){
        return System.getenv().containsKey("redicloud_files_" + Files.REDIS_CONFIG.name().toLowerCase());
    }

    private boolean isRunningExternal(){
        return System.getenv().containsKey("redicloud_external") && System.getenv("redicloud_external").equals("true");
    }

    private UUID getGroupId() {
        return UUID.fromString(System.getenv("redicloud_group_id"));
    }

    private boolean hasGroupId() {
        return System.getenv().containsKey("redicloud_group_id");
    }

    private void initThisService() {

        this.service = null;

        if(!hasServiceId()){
            this.runningExternal = true;
            if(this.getGroupManager().existsGroup(getGroupId())){
                IServiceStartConfiguration startConfiguration = this.getGroupManager()
                        .getGroup(this.getGroupId()).getImpl(CloudGroup.class).createServiceConfiguration();
                this.service = new CloudService();
                this.service.setExternal(true);
                this.service.setServiceState(ServiceState.RUNNING_UNDEFINED);
                this.service.setMaxPlayers(50);
                this.service.setMotd("§7•§8● §bRedi§3Cloud §8» §fA §bredis §fbased §bcluster §fcloud system§r\n    §b§l§8× §fDiscord §8➜ §3https://discord.gg/g2HV52VV4G");
                this.service.setNodeId(null);
                this.service.setConfiguration(startConfiguration);
                this.service = this.serviceManager.createBucket(this.service.getUniqueId().toString(), this.service).getImpl(CloudService.class);
            }else{
                this.console.fatal("No target group found for external service " + getServiceId(), null);
                shutdown(false);
                return;
            }
        }else{
            service = this.serviceManager.getService(getServiceId()).getImpl(CloudService.class);
            if(service.getServiceState() == ServiceState.RUNNING_DEFINED || service.getServiceState() == ServiceState.RUNNING_UNDEFINED){
                this.runningExternal = true;
                this.console.fatal("Can´t run external service because " + getServiceId() + " is already running!", null);
                shutdown(false);
                return;
            }
        }

        getEventManager().postGlobalAsync(new CloudServiceStartedEvent(this.service.getHolder()));

        this.updaterTask = ProxyServer.getInstance().getScheduler().schedule(this.plugin, () -> {
            if (this.service.getOnlineCount() != this.onlineCount) {
                this.service.setOnlineCount(this.onlineCount);
                this.service.updateAsync();
            }
        }, 1500, 1500, TimeUnit.MILLISECONDS);
    }

    private void initRedis() {
        RedisCredentials redisCredentials;
        try {
            redisCredentials = FileWriter.readObject(new File(getRedisFilePath()), RedisCredentials.class);
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
        if (!object.getUniqueId().equals(service.getUniqueId())) return;

    }

    @Override
    public INetworkComponentInfo getNetworkComponentInfo() {
        return this.service.getNetworkComponentInfo();
    }

    @Override
    public void shutdown(boolean fromHook) {
        if(this.isShutdownInitiated) return;
        this.isShutdownInitiated = true;

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

        if (this.updaterTask != null) this.updaterTask.cancel();

        if (this.redisConnection != null) this.redisConnection.getClient().shutdown();

        ProxyServer.getInstance().stop();
    }
}
