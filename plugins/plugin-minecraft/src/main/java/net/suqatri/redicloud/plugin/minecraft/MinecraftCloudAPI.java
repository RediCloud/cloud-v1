package net.suqatri.redicloud.plugin.minecraft;

import lombok.Getter;
import net.suqatri.redicloud.api.impl.CloudDefaultAPIImpl;
import net.suqatri.redicloud.api.impl.player.CloudPlayerManager;
import net.suqatri.redicloud.api.impl.redis.RedisConnection;
import net.suqatri.redicloud.api.impl.service.CloudService;
import net.suqatri.redicloud.api.impl.service.CloudServiceManager;
import net.suqatri.redicloud.api.impl.service.factory.CloudServiceFactory;
import net.suqatri.redicloud.api.impl.service.version.CloudServiceVersionManager;
import net.suqatri.redicloud.api.impl.template.CloudServiceTemplateManager;
import net.suqatri.redicloud.api.network.INetworkComponentInfo;
import net.suqatri.redicloud.api.player.ICloudPlayer;
import net.suqatri.redicloud.api.redis.RedisCredentials;
import net.suqatri.redicloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.redicloud.api.service.ICloudServiceManager;
import net.suqatri.redicloud.api.service.ServiceState;
import net.suqatri.redicloud.api.service.event.CloudServiceStartedEvent;
import net.suqatri.redicloud.api.service.factory.ICloudServiceFactory;
import net.suqatri.redicloud.api.utils.ApplicationType;
import net.suqatri.redicloud.api.utils.Files;
import net.suqatri.redicloud.commons.file.FileWriter;
import net.suqatri.redicloud.plugin.minecraft.command.BukkitCloudCommandManager;
import net.suqatri.redicloud.plugin.minecraft.console.BukkitConsole;
import net.suqatri.redicloud.plugin.minecraft.listener.PlayerJoinListener;
import net.suqatri.redicloud.plugin.minecraft.listener.PlayerKickListener;
import net.suqatri.redicloud.plugin.minecraft.listener.PlayerQuitListener;
import net.suqatri.redicloud.plugin.minecraft.listener.ServerListPingListener;
import net.suqatri.redicloud.plugin.minecraft.scheduler.BukkitScheduler;
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
    private final BukkitConsole console;
    private RedisConnection redisConnection;
    private final JavaPlugin javaPlugin;
    private final BukkitScheduler scheduler;
    private final ICloudServiceManager serviceManager;
    private final ICloudServiceFactory serviceFactory;
    private final BukkitCloudCommandManager commandManager;
    private final CloudServiceTemplateManager serviceTemplateManager;
    private final CloudServiceVersionManager serviceVersionManager;
    private BukkitTask updaterTask;
    private final CloudPlayerManager playerManager;

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
        this.playerManager = new CloudPlayerManager();

        init();
        registerInternalListeners();
        registerInternalPackets();
        initListeners();
    }

    private void initListeners(){
        Bukkit.getPluginManager().registerEvents(new ServerListPingListener(), this.javaPlugin);
        Bukkit.getPluginManager().registerEvents(new PlayerKickListener(), this.javaPlugin);
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(), this.javaPlugin);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), this.javaPlugin);
    }

    private void init(){
        initRedis();
        initThisService();
    }

    private void initThisService(){
        this.service = this.serviceManager.getService(UUID.fromString(System.getenv("redicloud_service_id"))).getImpl(CloudService.class);
        this.service.setServiceState(ServiceState.RUNNING_UNDEFINED);
        this.service.setOnlineCount(Bukkit.getOnlinePlayers().size());
        this.service.update();

        getEventManager().postGlobalAsync(new CloudServiceStartedEvent(this.service.getHolder()));
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
        this.redisConnection.setConnectionPoolSize(24);
        this.redisConnection.setConnectionMinimumIdleSize(6);
        this.redisConnection.setSubscriptionConnectionPoolSize(10);
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
        } catch (InterruptedException ignored) {}

        if(this.updaterTask != null) this.updaterTask.cancel();

        if(this.redisConnection != null) this.redisConnection.getClient().shutdown();
    }
}
