package net.suqatri.redicloud.plugin.minecraft;

import lombok.Getter;
import lombok.Setter;
import net.suqatri.redicloud.api.impl.player.CloudPlayerManager;
import net.suqatri.redicloud.api.impl.redis.RedisConnection;
import net.suqatri.redicloud.api.impl.service.CloudService;
import net.suqatri.redicloud.api.impl.service.factory.CloudServiceFactory;
import net.suqatri.redicloud.api.impl.service.version.CloudServiceVersionManager;
import net.suqatri.redicloud.api.impl.template.CloudServiceTemplateManager;
import net.suqatri.redicloud.api.minecraft.MinecraftDefaultCloudAPI;
import net.suqatri.redicloud.api.network.INetworkComponentInfo;
import net.suqatri.redicloud.api.redis.RedisCredentials;
import net.suqatri.redicloud.api.service.ServiceState;
import net.suqatri.redicloud.api.service.event.CloudServiceStartedEvent;
import net.suqatri.redicloud.api.service.factory.ICloudServiceFactory;
import net.suqatri.redicloud.api.utils.Files;
import net.suqatri.redicloud.commons.file.FileWriter;
import net.suqatri.redicloud.plugin.minecraft.command.BukkitCloudCommandManager;
import net.suqatri.redicloud.plugin.minecraft.console.BukkitConsole;
import net.suqatri.redicloud.plugin.minecraft.listener.PlayerLoginListener;
import net.suqatri.redicloud.plugin.minecraft.listener.ServerListPingListener;
import net.suqatri.redicloud.plugin.minecraft.scheduler.BukkitScheduler;
import net.suqatri.redicloud.plugin.minecraft.service.CloudMinecraftServiceManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.UUID;

@Getter
public class MinecraftCloudAPI extends MinecraftDefaultCloudAPI<CloudService> {

    @Getter
    private static MinecraftCloudAPI instance;
    private final BukkitConsole console;
    private final JavaPlugin javaPlugin;
    private final BukkitScheduler scheduler;
    private final CloudMinecraftServiceManager serviceManager;
    private final ICloudServiceFactory serviceFactory;
    private final BukkitCloudCommandManager commandManager;
    private final CloudServiceTemplateManager serviceTemplateManager;
    private final CloudServiceVersionManager serviceVersionManager;
    private final CloudPlayerManager playerManager;
    private CloudService service;
    private RedisConnection redisConnection;
    private BukkitTask updaterTask;
    @Setter
    private String chatPrefix = "§bRedi§3Cloud §8» §f";
    private boolean isShutdownInitiated = false;

    public MinecraftCloudAPI(JavaPlugin javaPlugin) {
        super();
        instance = this;
        this.javaPlugin = javaPlugin;
        this.console = new BukkitConsole(this.javaPlugin.getLogger());
        this.scheduler = new BukkitScheduler(this.javaPlugin);
        this.serviceManager = new CloudMinecraftServiceManager();
        this.serviceFactory = new CloudServiceFactory(this.serviceManager);
        this.commandManager = new BukkitCloudCommandManager(this.javaPlugin);
        this.serviceTemplateManager = new CloudServiceTemplateManager();
        this.serviceVersionManager = new CloudServiceVersionManager();
        this.playerManager = new CloudPlayerManager();

        initRedis();
        registerInternalListeners();
        registerInternalPackets();
        initListeners();
        initThisService();
    }

    private void initListeners() {
        Bukkit.getPluginManager().registerEvents(new ServerListPingListener(), this.javaPlugin);
        Bukkit.getPluginManager().registerEvents(new PlayerLoginListener(), this.javaPlugin);
    }

    private void initThisService() {
        this.service = (CloudService) this.serviceManager.getService(UUID.fromString(System.getenv("redicloud_service_id")));
        this.service.setServiceState(ServiceState.RUNNING_UNDEFINED);
        this.service.setOnlineCount(Bukkit.getOnlinePlayers().size());
        this.service.update();

        getEventManager().postGlobalAsync(new CloudServiceStartedEvent(this.service));

        this.updaterTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this.javaPlugin, () -> {
            boolean update = false;
            if (this.service.getOnlineCount() != Bukkit.getOnlinePlayers().size()) {
                this.service.setOnlineCount(Bukkit.getOnlinePlayers().size());
                update = true;
            }
            if(this.service.getMaxPlayers() != Bukkit.getMaxPlayers()){
                this.service.setMaxPlayers(Bukkit.getMaxPlayers());
                update = true;
            }
            if(update) this.service.updateAsync();
        }, 0, 20);
    }

    private void initRedis() {
        RedisCredentials redisCredentials;
        try {
            redisCredentials = FileWriter.readObject(new File(System.getenv("redicloud_files_" + Files.REDIS_CONFIG.name().toLowerCase())), RedisCredentials.class);
        } catch (Exception e) {
            this.console.error("Failed to read redis.json file! Please check your credentials.", e);
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
        if (!o.getUniqueId().equals(this.service.getUniqueId())) return;
    }

    @Override
    public INetworkComponentInfo getNetworkComponentInfo() {
        return this.service.getNetworkComponentInfo();
    }

    @Override
    public void shutdown(boolean fromHook) {
        getScheduler().runTask(() -> {
            if(this.isShutdownInitiated) return;
            this.isShutdownInitiated = true;

            this.service.setServiceState(ServiceState.STOPPING);
            this.service.update();

            if(this.playerManager != null){
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    try {
                        if (this.serviceManager != null) {
                            onlinePlayer.kickPlayer("CloudService shutdown");
                        }
                    }catch (Exception e){
                        this.console.error("Failed to disconnect player " + onlinePlayer.getName() + " from service", e);
                    }
                }
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }

            if (this.updaterTask != null) this.updaterTask.cancel();

            if (this.redisConnection != null) this.redisConnection.disconnect();

            if(!fromHook) Bukkit.shutdown();
        });
    }
}
