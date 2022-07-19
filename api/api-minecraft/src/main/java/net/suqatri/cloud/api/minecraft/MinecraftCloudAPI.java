package net.suqatri.cloud.api.minecraft;

import lombok.Getter;
import net.suqatri.cloud.api.console.ICommandManager;
import net.suqatri.cloud.api.console.IConsole;
import net.suqatri.cloud.api.event.ICloudEventManager;
import net.suqatri.cloud.api.group.ICloudGroupManager;
import net.suqatri.cloud.api.impl.CloudDefaultAPIImpl;
import net.suqatri.cloud.api.impl.node.CloudNode;
import net.suqatri.cloud.api.impl.redis.RedisConnection;
import net.suqatri.cloud.api.impl.redis.bucket.RBucketObject;
import net.suqatri.cloud.api.impl.service.CloudService;
import net.suqatri.cloud.api.impl.service.CloudServiceManager;
import net.suqatri.cloud.api.impl.service.factory.CloudServiceFactory;
import net.suqatri.cloud.api.minecraft.console.BukkitConsole;
import net.suqatri.cloud.api.minecraft.scheduler.BukkitScheduler;
import net.suqatri.cloud.api.network.INetworkComponentInfo;
import net.suqatri.cloud.api.node.ICloudNodeManager;
import net.suqatri.cloud.api.packet.ICloudPacketManager;
import net.suqatri.cloud.api.player.ICloudPlayerManager;
import net.suqatri.cloud.api.redis.IRedisConnection;
import net.suqatri.cloud.api.redis.RedisCredentials;
import net.suqatri.cloud.api.scheduler.IScheduler;
import net.suqatri.cloud.api.service.ICloudServiceManager;
import net.suqatri.cloud.api.service.factory.ICloudServiceFactory;
import net.suqatri.cloud.api.service.version.ICloudServiceVersionManager;
import net.suqatri.cloud.api.template.ICloudServiceTemplateManager;
import net.suqatri.cloud.api.utils.ApplicationType;
import net.suqatri.cloud.api.utils.Files;
import net.suqatri.cloud.commons.file.FileWriter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

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

    public MinecraftCloudAPI(JavaPlugin javaPlugin) {
        super(ApplicationType.SERVICE_MINECRAFT);
        instance = this;
        this.javaPlugin = javaPlugin;
        this.console = new BukkitConsole(this.javaPlugin.getLogger());
        this.scheduler = new BukkitScheduler(this.javaPlugin);
        this.serviceManager = new CloudServiceManager();
        this.serviceFactory = new CloudServiceFactory(this.serviceManager);

        init();
    }

    private void init(){
        initRedis();
    }

    private void initRedis() {
        RedisCredentials redisCredentials;
        try {
            System.out.println(Files.REDIS_CONFIG.getFile().getAbsolutePath());
            redisCredentials = FileWriter.readObject(Files.REDIS_CONFIG.getFile(), RedisCredentials.class);
        } catch (Exception e) {
            this.console.error("Failed to read redis.json file! Please check your credentials.");
            Bukkit.getPluginManager().disablePlugin(this.javaPlugin);
            return;
        }
        this.redisConnection = new RedisConnection(redisCredentials);
        try {
            this.redisConnection.connect();
            this.console.info("Redis connection established!");
        } catch (Exception e) {
            this.console.error("§cFailed to connect to redis server. Please check your credentials.", e);
            Bukkit.getPluginManager().disablePlugin(this.javaPlugin);
        }
    }

    @Override
    public void updateApplicationProperties(CloudService object) {

    }

    @Override
    public ICommandManager<?> getCommandManager() {
        return null;
    }

    @Override
    public ICloudPlayerManager getPlayerManager() {
        return null;
    }

    @Override
    public ICloudServiceTemplateManager getServiceTemplateManager() {
        return null;
    }

    @Override
    public INetworkComponentInfo getNetworkComponentInfo() {
        return null;
    }

    @Override
    public ICloudServiceVersionManager getServiceVersionManager() {
        return null;
    }

    @Override
    public void shutdown(boolean fromHook) {

    }
}
