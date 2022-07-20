package net.suqatri.cloud.plugin.minecraft;

import lombok.Getter;
import net.suqatri.cloud.api.impl.CloudDefaultAPIImpl;
import net.suqatri.cloud.api.impl.redis.RedisConnection;
import net.suqatri.cloud.api.impl.service.CloudService;
import net.suqatri.cloud.api.impl.service.CloudServiceManager;
import net.suqatri.cloud.api.impl.service.factory.CloudServiceFactory;
import net.suqatri.cloud.api.impl.service.version.CloudServiceVersionManager;
import net.suqatri.cloud.api.impl.template.CloudServiceTemplateManager;
import net.suqatri.cloud.api.player.ICloudPlayer;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.service.event.CloudServiceStartedEvent;
import net.suqatri.cloud.commons.ByteUtils;
import net.suqatri.cloud.plugin.minecraft.command.BukkitCloudCommandManager;
import net.suqatri.cloud.plugin.minecraft.console.BukkitConsole;
import net.suqatri.cloud.plugin.minecraft.listener.ServerListPingListener;
import net.suqatri.cloud.plugin.minecraft.scheduler.BukkitScheduler;
import net.suqatri.cloud.api.network.INetworkComponentInfo;
import net.suqatri.cloud.api.player.ICloudPlayerManager;
import net.suqatri.cloud.api.redis.RedisCredentials;
import net.suqatri.cloud.api.service.ICloudServiceManager;
import net.suqatri.cloud.api.service.ServiceState;
import net.suqatri.cloud.api.service.factory.ICloudServiceFactory;
import net.suqatri.cloud.api.utils.ApplicationType;
import net.suqatri.cloud.api.utils.Files;
import net.suqatri.cloud.commons.file.FileWriter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.UUID;

@Getter
public class MinecraftCloudAPI extends CloudDefaultAPIImpl<CloudService> {

    @Getter
    private static MinecraftCloudAPI instance;

    private CloudService service;
    private BukkitConsole console;
    private RedisConnection redisConnection;
    private final JavaPlugin javaPlugin;
    private final BukkitScheduler scheduler;
    private final ICloudServiceManager serviceManager;
    private final ICloudServiceFactory serviceFactory;
    private final BukkitCloudCommandManager commandManager;
    private final CloudServiceTemplateManager serviceTemplateManager;
    private final CloudServiceVersionManager serviceVersionManager;
    private BukkitTask updaterTask;

    public MinecraftCloudAPI(JavaPlugin javaPlugin) {
        super(ApplicationType.SERVICE_MINECRAFT);
        instance = this;
        this.javaPlugin = javaPlugin;
        this.console = new BukkitConsole(this.javaPlugin.getLogger());
        this.scheduler = new BukkitScheduler(this.javaPlugin);
        this.serviceManager = new CloudServiceManager();
        this.serviceFactory = new CloudServiceFactory(this.serviceManager);
        this.commandManager = new BukkitCloudCommandManager(this.javaPlugin);
        this.serviceTemplateManager = new CloudServiceTemplateManager();
        this.serviceVersionManager = new CloudServiceVersionManager();

        init();
        registerInternalListeners();
        registerInternalPackets();
        initListeners();
    }

    private void initListeners(){
        Bukkit.getPluginManager().registerEvents(new ServerListPingListener(), this.javaPlugin);
    }

    private void init(){
        initRedis();
        initThisService();
        startUpdater();
    }

    private void startUpdater(){
        this.updaterTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this.javaPlugin, () -> {
            this.service.setOnlineCount(Bukkit.getOnlinePlayers().size());
            long usedRam = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
            this.service.setRamUsage(ByteUtils.bytesToMb(usedRam));
            this.service.update();
        }, 0, 20);
    }

    private void initThisService(){
        this.service = this.serviceManager.getService(UUID.fromString(System.getenv("redicloud_service_id"))).getImpl(CloudService.class);
        this.service.setServiceState(ServiceState.RUNNING_UNDEFINED);
        this.service.update();

        getEventManager().postLocal(new CloudServiceStartedEvent(this.service.getHolder()));
    }

    private void initRedis() {
        RedisCredentials redisCredentials;
        try {
            redisCredentials = FileWriter.readObject(new File(System.getenv("redicloud_files_" + Files.REDIS_CONFIG.name().toLowerCase())), RedisCredentials.class);
        } catch (Exception e) {
            this.console.error("Failed to read redis.json file! Please check your credentials.");
            Bukkit.getPluginManager().disablePlugin(this.javaPlugin);
            return;
        }
        this.redisConnection = new RedisConnection(redisCredentials);
        this.redisConnection.setConnectionPoolSize(this.redisConnection.getConnectionPoolSize() / 4);
        this.redisConnection.setConnectionMinimumIdleSize(this.redisConnection.getSubscriptionConnectionMinimumIdleSize() / 5);
        this.redisConnection.setSubscriptionConnectionPoolSize(this.redisConnection.getSubscriptionConnectionMinimumIdleSize() / 6);
        try {
            this.redisConnection.connect();
            this.console.info("Redis connection established!");
        } catch (Exception e) {
            this.console.error("§cFailed to connect to redis server. Please check your credentials.", e);
            Bukkit.getPluginManager().disablePlugin(this.javaPlugin);
        }
    }

    @Override
    public void updateApplicationProperties(CloudService o) {
        if(!o.getUniqueId().equals(this.service.getUniqueId())) return;
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

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            IRBucketHolder<ICloudPlayer> cloudPlayer = getPlayerManager().getPlayer(onlinePlayer.getUniqueId());
            cloudPlayer.get().connect(getServiceManager().getFallbackService());
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}

        if(this.updaterTask != null) this.updaterTask.cancel();

        if(this.redisConnection != null) this.redisConnection.getClient().shutdown();
    }
}
