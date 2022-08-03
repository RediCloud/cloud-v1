package net.suqatri.redicloud.plugin.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import com.velocitypowered.api.scheduler.ScheduledTask;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.impl.CloudDefaultAPIImpl;
import net.suqatri.redicloud.api.impl.group.CloudGroup;
import net.suqatri.redicloud.api.impl.player.CloudPlayerManager;
import net.suqatri.redicloud.api.impl.redis.RedisConnection;
import net.suqatri.redicloud.api.impl.redis.bucket.RedissonBucketManager;
import net.suqatri.redicloud.api.impl.service.CloudService;
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
import net.suqatri.redicloud.api.service.event.CloudServiceStoppedEvent;
import net.suqatri.redicloud.api.utils.ApplicationType;
import net.suqatri.redicloud.api.utils.Files;
import net.suqatri.redicloud.commons.file.FileWriter;
import net.suqatri.redicloud.plugin.velocity.command.VelocityCloudCommandManager;
import net.suqatri.redicloud.plugin.velocity.console.VelocityConsole;
import net.suqatri.redicloud.plugin.velocity.listener.*;
import net.suqatri.redicloud.plugin.velocity.scheduler.VelocityScheduler;
import net.suqatri.redicloud.plugin.velocity.service.CloudVelocityServiceManager;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Getter
public class VelocityCloudAPI extends CloudDefaultAPIImpl<CloudService> {

    @Getter
    private static VelocityCloudAPI instance;

    private final VelocityCloudPlugin plugin;
    private final ProxyServer proxyServer;
    private final CloudServiceFactory serviceFactory;
    private final CloudVelocityServiceManager serviceManager;
    private final CloudServiceVersionManager serviceVersionManager;
    private final CloudServiceTemplateManager serviceTemplateManager;
    private final VelocityConsole console;
    private final VelocityCloudCommandManager commandManager;
    private final VelocityScheduler scheduler;
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

    public VelocityCloudAPI(ProxyServer proxyServer, VelocityCloudPlugin plugin) {
        super(ApplicationType.SERVICE_PROXY);
        instance = this;
        this.plugin = plugin;
        this.proxyServer = proxyServer;
        this.scheduler = new VelocityScheduler(this.plugin);
        this.console = new VelocityConsole();
        this.serviceManager = new CloudVelocityServiceManager(this.proxyServer);
        this.serviceFactory = new CloudServiceFactory(this.serviceManager);
        this.serviceVersionManager = new CloudServiceVersionManager();
        this.serviceTemplateManager = new CloudServiceTemplateManager();
        this.commandManager = new VelocityCloudCommandManager(this.proxyServer, this.plugin);
        this.playerManager = new CloudPlayerManager();

        if (!hasRedisFilePath()) {
            this.console.fatal("Redis file path is not set as environment variable!", null);
            this.shutdown(false);
            return;
        }
        if (!hasServiceId() && !hasGroupId()) {
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

        this.proxyServer.getEventManager().register(this.plugin, new LoginListener());
        this.proxyServer.getEventManager().register(this.plugin, new PlayerDisconnectListener());
        this.proxyServer.getEventManager().register(this.plugin, new ProxyPingListener());
        this.proxyServer.getEventManager().register(this.plugin, new ServerPreConnectListener());
        this.proxyServer.getEventManager().register(this.plugin, new KickedFromServerListener());
        this.proxyServer.getEventManager().register(this.plugin, new ServerPostConnectListener());

        getEventManager().register(new CloudServiceStartedListener());
        getEventManager().register(new CloudServiceStoppedListener());
    }

    void registerStartedService() {
        this.serviceManager.getServicesAsync()
                .onFailure(e -> this.console.error("Failed to register started service", e))
                .onSuccess(serviceHolders -> {
                    for (IRBucketHolder<ICloudService> serviceHolder : serviceHolders) {
                        Optional<RegisteredServer> serverInfo = this.proxyServer.getServer(serviceHolder.get().getServiceName());
                        if(!serverInfo.isPresent()) continue;
                        this.proxyServer.unregisterServer(serverInfo.get().getServerInfo());
                    }

                    for (IRBucketHolder<ICloudService> serviceHolder : serviceHolders) {
                        if (serviceHolder.get().getEnvironment() == ServiceEnvironment.BUNGEECORD) continue;
                        if(serviceHolder.get().getEnvironment() == ServiceEnvironment.VELOCITY) continue;
                        ServerInfo serverInfo = new ServerInfo(serviceHolder.get().getServiceName(),
                                new InetSocketAddress(serviceHolder.get().getHostName(), serviceHolder.get().getPort()));
                        this.proxyServer.registerServer(serverInfo);
                        CloudAPI.getInstance().getConsole().debug("Registered service: " + serverInfo.getName());
                    }

                    ServerInfo fallback = new ServerInfo("fallback", new InetSocketAddress("127.0.0.1", 0));
                    proxyServer.registerServer(fallback);
                    CloudAPI.getInstance().getConsole().debug("Registered service: " + fallback.getName());

                    ServerInfo lobby = new ServerInfo("lobby", new InetSocketAddress("127.0.0.1", 0));
                    proxyServer.registerServer(lobby);
                    CloudAPI.getInstance().getConsole().debug("Registered service: " + lobby.getName());
                });
    }

    private List<String> getStartArguments() {
        List<String> list = new ArrayList<>();
        for (String s : System.getProperty("sun.java.command").split(" ")) {
            if (s.startsWith("--")) {
                list.add(s.replaceFirst("--", ""));
            }
        }
        return list;
    }

    private UUID getServiceId() {
        return UUID.fromString(System.getenv("redicloud_service_id"));
    }

    private boolean hasServiceId() {
        return System.getenv().containsKey("redicloud_service_id");
    }

    private String getRedisFilePath() {
        if (System.getenv().containsKey("redicloud_files_" + Files.REDIS_CONFIG.name().toLowerCase())) {
            return System.getenv("redicloud_files_" + Files.REDIS_CONFIG.name().toLowerCase());
        }
        for (String inputArgument : getStartArguments()) {
            if (inputArgument.startsWith("redicloud_files_" + Files.REDIS_CONFIG.name().toLowerCase())) {
                String[] split = inputArgument.split("=");
                if (split.length == 2) {
                    return split[1];
                }
            }
        }
        return null;
    }

    private boolean hasRedisFilePath() {
        return System.getenv().containsKey("redicloud_files_" + Files.REDIS_CONFIG.name().toLowerCase())
                || getStartArguments().parallelStream()
                .anyMatch(s -> s.startsWith("redicloud_files_" + Files.REDIS_CONFIG.name().toLowerCase()));
    }

    private boolean isRunningExternal() {
        return (System.getenv().containsKey("redicloud_external")
                && System.getenv("redicloud_external").equals("true"))
                ||
                getStartArguments().parallelStream()
                        .anyMatch(s -> s.startsWith("redicloud_external=true"));
    }

    private UUID getGroupId() {
        if (System.getenv().containsKey("redicloud_group_id")) {
            return UUID.fromString(System.getenv("redicloud_group_id"));
        }
        for (String inputArgument : getStartArguments()) {
            if (inputArgument.startsWith("redicloud_group_id")) {
                String[] split = inputArgument.split("=");
                if (split.length == 2) {
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

        if (CloudAPI.getInstance().getNodeManager().getNodes().parallelStream().noneMatch(holder -> holder.get().isConnected())) {
            this.console.fatal("Cluster seems to be offline! There are no connected nodes!", null);
            this.shutdown(false);
            return;
        }

        if (!hasServiceId()) {
            this.runningExternal = true;
            if (this.getGroupManager().existsGroup(getGroupId())) {

                IServiceStartConfiguration startConfiguration = this.getGroupManager()
                        .getGroup(this.getGroupId()).getImpl(CloudGroup.class).createServiceConfiguration();

                int id = 1;
                List<Integer> ids = new ArrayList<>();
                for (IRBucketHolder<ICloudService> serviceHolder : this.serviceManager.getServices()) {
                    if (serviceHolder.get().getServiceState() == ServiceState.OFFLINE && startConfiguration.isStatic())
                        continue;
                    if (serviceHolder.get().getGroupName().equalsIgnoreCase(startConfiguration.getGroupName())) {
                        ids.add(serviceHolder.get().getId());
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
                this.service = this.serviceManager.createBucket(this.service.getUniqueId().toString(), this.service).getImpl(CloudService.class);
            } else {
                this.console.fatal("No target group found for external service " + getServiceId(), null);
                shutdown(false);
                return;
            }
        } else {
            service = this.serviceManager.getService(getServiceId()).getImpl(CloudService.class);
            if (service.getServiceState() == ServiceState.RUNNING_DEFINED || service.getServiceState() == ServiceState.RUNNING_UNDEFINED) {
                this.runningExternal = true;
                this.console.fatal("Can´t run external service because " + getServiceId() + " is already running!", null);
                shutdown(false);
                return;
            }
        }

        getEventManager().postGlobalAsync(new CloudServiceStartedEvent(this.service.getHolder()));

        this.updaterTask = this.proxyServer.getScheduler().buildTask(this.plugin, () -> {
            if (this.service.getOnlineCount() != this.onlineCount) {
                this.service.setOnlineCount(this.onlineCount);
                this.service.updateAsync();
            }
        }).repeat(1500, TimeUnit.MILLISECONDS).schedule();
    }

    private void initRedis() {
        RedisCredentials redisCredentials;
        try {
            String path = getRedisFilePath();
            this.console.debug("Using redis config file: " + path);
            redisCredentials = FileWriter.readObject(new File(path), RedisCredentials.class);
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
        if (this.isShutdownInitiated) return;
        this.isShutdownInitiated = true;

        if (this.service != null) {
            this.service.setServiceState(ServiceState.STOPPING);
            this.service.update();
        }

        for (Player onlinePlayer : this.proxyServer.getAllPlayers()) {
            onlinePlayer.disconnect(LegacyComponentSerializer.legacySection()
                    .deserialize("§cServer is shutting down."));
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }

        if (this.service != null) {
            if (this.service.isExternal()) {
                CloudAPI.getInstance().getEventManager().postGlobalAsync(new CloudServiceStoppedEvent(this.service.getHolder()));
            }
        }

        if (this.updaterTask != null) this.updaterTask.cancel();

        if (this.service != null) {
            this.service.setServiceState(ServiceState.OFFLINE);
            this.service.update();
            ((RedissonBucketManager) CloudAPI.getInstance().getServiceManager()).deleteBucket(this.service.getUniqueId().toString());
            CloudAPI.getInstance().getServiceManager().removeFromFetcher(this.service.getServiceName(), this.service.getUniqueId());
        }

        if (this.redisConnection != null) this.redisConnection.getClient().shutdown();

        proxyServer.shutdown();
    }
}
