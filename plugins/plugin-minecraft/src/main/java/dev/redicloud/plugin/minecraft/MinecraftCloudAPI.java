package dev.redicloud.plugin.minecraft;

import dev.redicloud.dependency.DependencyLoader;
import dev.redicloud.module.ModuleHandler;
import dev.redicloud.plugin.minecraft.listener.PlayerLoginListener;
import dev.redicloud.plugin.minecraft.listener.ServerListPingListener;
import dev.redicloud.plugin.minecraft.scheduler.BukkitScheduler;
import dev.redicloud.plugin.minecraft.service.CloudMinecraftServiceManager;
import lombok.Getter;
import lombok.Setter;
import dev.redicloud.api.impl.player.CloudPlayerManager;
import dev.redicloud.api.impl.redis.RedisConnection;
import dev.redicloud.api.impl.service.CloudService;
import dev.redicloud.api.impl.service.factory.CloudServiceFactory;
import dev.redicloud.api.impl.service.version.CloudServiceVersionManager;
import dev.redicloud.api.impl.template.CloudServiceTemplateManager;
import dev.redicloud.api.minecraft.MinecraftDefaultCloudAPI;
import dev.redicloud.api.network.INetworkComponentInfo;
import dev.redicloud.api.redis.RedisCredentials;
import dev.redicloud.api.service.ServiceState;
import dev.redicloud.api.service.event.CloudServiceStartedEvent;
import dev.redicloud.api.service.factory.ICloudServiceFactory;
import dev.redicloud.api.utils.Files;
import dev.redicloud.commons.file.FileWriter;
import dev.redicloud.plugin.minecraft.command.BukkitCloudCommandManager;
import dev.redicloud.plugin.minecraft.console.BukkitConsole;
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

    public MinecraftCloudAPI(DependencyLoader dependencyLoader, JavaPlugin javaPlugin) {
        super(dependencyLoader);
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

        ModuleHandler.setParentClassLoader(this.javaPlugin.getClass().getClassLoader());
        getModuleHandler().loadModules();

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

        this.console.debug("ServiceId: " + System.getenv("redicloud_service_id"));

        getEventManager().postGlobalAsync(new CloudServiceStartedEvent(this.service));

        this.updaterTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this.javaPlugin, () -> {
            boolean update = false;
            if (this.service.getOnlineCount() != Bukkit.getOnlinePlayers().size()) {
                this.service.setOnlineCount(Bukkit.getOnlinePlayers().size());
                this.service.setLastPlayerAction(System.currentTimeMillis());
                update = true;
            }
            if(this.service.getMaxPlayers() != Bukkit.getMaxPlayers()){
                this.service.setMaxPlayers(Bukkit.getMaxPlayers());
                update = true;
            }
            if(update) {
                this.console.trace("Service " + this.service.getId() + " updated");
                this.service.updateAsync();
            }
        }, 0, 20);
    }

    private void initRedis() {
        RedisCredentials redisCredentials;
        try {
            redisCredentials = FileWriter.readObject(Files.REDIS_CONFIG.getFile(), RedisCredentials.class);
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
        if(this.service == null) return;
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

            if(this.getModuleHandler() != null){
                this.getModuleHandler().unloadModules();
            }

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
