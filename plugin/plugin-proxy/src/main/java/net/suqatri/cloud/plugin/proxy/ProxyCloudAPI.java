package net.suqatri.cloud.plugin.proxy;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.suqatri.cloud.api.console.ICommandManager;
import net.suqatri.cloud.api.console.IConsole;
import net.suqatri.cloud.api.impl.CloudDefaultAPIImpl;
import net.suqatri.cloud.api.impl.listener.service.CloudServiceStartedListener;
import net.suqatri.cloud.api.impl.listener.service.CloudServiceStoppedListener;
import net.suqatri.cloud.api.impl.redis.RedisConnection;
import net.suqatri.cloud.api.impl.service.CloudService;
import net.suqatri.cloud.api.impl.service.CloudServiceManager;
import net.suqatri.cloud.api.impl.service.factory.CloudServiceFactory;
import net.suqatri.cloud.api.impl.service.version.CloudServiceVersionManager;
import net.suqatri.cloud.api.impl.template.CloudServiceTemplateManager;
import net.suqatri.cloud.api.network.INetworkComponentInfo;
import net.suqatri.cloud.api.player.ICloudPlayerManager;
import net.suqatri.cloud.api.redis.IRedisConnection;
import net.suqatri.cloud.api.redis.RedisCredentials;
import net.suqatri.cloud.api.scheduler.IScheduler;
import net.suqatri.cloud.api.service.ICloudServiceManager;
import net.suqatri.cloud.api.service.ServiceState;
import net.suqatri.cloud.api.service.factory.ICloudServiceFactory;
import net.suqatri.cloud.api.service.version.ICloudServiceVersionManager;
import net.suqatri.cloud.api.template.ICloudServiceTemplateManager;
import net.suqatri.cloud.api.utils.ApplicationType;
import net.suqatri.cloud.api.utils.Files;
import net.suqatri.cloud.commons.ByteUtils;
import net.suqatri.cloud.commons.file.FileWriter;
import net.suqatri.cloud.plugin.proxy.command.BungeeCloudCommandManager;
import net.suqatri.cloud.plugin.proxy.console.ProxyConsole;
import net.suqatri.cloud.plugin.proxy.listener.LoginListener;
import net.suqatri.cloud.plugin.proxy.listener.PlayerDisconnectListener;
import net.suqatri.cloud.plugin.proxy.listener.ProxyPingListener;
import net.suqatri.cloud.plugin.proxy.listener.ServerSwitchListener;
import net.suqatri.cloud.plugin.proxy.scheduler.BungeeScheduler;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Getter
public class ProxyCloudAPI extends CloudDefaultAPIImpl<CloudService> {

    @Getter
    private static ProxyCloudAPI instance;

    private final Plugin plugin;
    private CloudService service;
    private RedisConnection redisConnection;
    private final CloudServiceFactory serviceFactory;
    private final CloudServiceManager serviceManager;
    private final CloudServiceVersionManager serviceVersionManager;
    private final CloudServiceTemplateManager serviceTemplateManager;
    private ScheduledTask updaterTask;
    private final ProxyConsole console;
    private final BungeeCloudCommandManager commandManager;
    private final BungeeScheduler scheduler;

    public ProxyCloudAPI(Plugin plugin) {
        super(ApplicationType.SERVICE_PROXY);
        instance = this;
        this.plugin = plugin;
        this.scheduler = new BungeeScheduler(this.plugin);
        this.console = new ProxyConsole(this.plugin.getLogger());
        this.serviceManager = new CloudServiceManager();
        this.serviceFactory = new CloudServiceFactory(this.serviceManager);
        this.serviceVersionManager = new CloudServiceVersionManager();
        this.serviceTemplateManager = new CloudServiceTemplateManager();
        this.commandManager = new BungeeCloudCommandManager(this.plugin);

        init();
        initListeners();
    }

    private void initListeners(){
        ProxyServer.getInstance().getPluginManager().registerListener(this.plugin, new LoginListener());
        ProxyServer.getInstance().getPluginManager().registerListener(this.plugin, new ProxyPingListener());
        ProxyServer.getInstance().getPluginManager().registerListener(this.plugin, new ServerSwitchListener());
        ProxyServer.getInstance().getPluginManager().registerListener(this.plugin, new PlayerDisconnectListener());

        getEventManager().register(new CloudServiceStoppedListener());
        getEventManager().register(new CloudServiceStartedListener());
    }

    private void init(){
        initRedis();
        initThisService();
        startUpdater();
    }

    private void initThisService(){
        this.service = this.serviceManager.getService(UUID.fromString(System.getenv("redicloud_serviceId"))).getImpl(CloudService.class);
        this.service.setServiceState(ServiceState.RUNNING_UNDEFINED);
        this.service.update();
    }

    private void initRedis() {
        RedisCredentials redisCredentials;
        try {
            System.out.println(Files.REDIS_CONFIG.getFile().getAbsolutePath());
            redisCredentials = FileWriter.readObject(new File(System.getenv("redicloud_redis_path")), RedisCredentials.class);
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

    private void startUpdater(){
        this.updaterTask = ProxyServer.getInstance().getScheduler().schedule(this.plugin, () -> {
            this.service.setOnlineCount(ProxyServer.getInstance().getOnlineCount());
            long usedRam = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
            this.service.setRamUsage(ByteUtils.bytesToMb(usedRam));
            this.service.update();
        }, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    public void updateApplicationProperties(CloudService object) {
        if(!object.getUniqueId().equals(service.getUniqueId())) return;

    }

    @Override
    public ICloudPlayerManager getPlayerManager() {
        return null;
    }

    @Override
    public INetworkComponentInfo getNetworkComponentInfo() {
        return this.service.getNetworkComponentInfo();
    }

    @Override
    public void shutdown(boolean fromHook) {
        //TODO use cloud player
        for (ProxiedPlayer onlinePlayer : ProxyServer.getInstance().getPlayers()) {
            onlinePlayer.disconnect("§cServer is shutting down.");
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(this.updaterTask != null) this.updaterTask.cancel();

        if(this.redisConnection != null) this.redisConnection.getClient().shutdown();
    }
}
