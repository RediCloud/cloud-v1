package net.suqatri.redicloud.limbo.api;

import lombok.Getter;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.console.ICommandManager;
import net.suqatri.redicloud.api.impl.CloudDefaultAPIImpl;
import net.suqatri.redicloud.api.impl.redis.RedisConnection;
import net.suqatri.redicloud.api.impl.service.CloudService;
import net.suqatri.redicloud.api.impl.service.factory.CloudServiceFactory;
import net.suqatri.redicloud.api.impl.service.version.CloudServiceVersionManager;
import net.suqatri.redicloud.api.impl.template.CloudServiceTemplateManager;
import net.suqatri.redicloud.api.network.INetworkComponentInfo;
import net.suqatri.redicloud.api.node.ICloudNode;
import net.suqatri.redicloud.api.player.ICloudPlayer;
import net.suqatri.redicloud.api.redis.RedisCredentials;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.api.service.ICloudServiceManager;
import net.suqatri.redicloud.api.service.ServiceState;
import net.suqatri.redicloud.api.service.event.CloudServiceStartedEvent;
import net.suqatri.redicloud.api.service.factory.ICloudServiceFactory;
import net.suqatri.redicloud.api.service.version.ICloudServiceVersionManager;
import net.suqatri.redicloud.api.template.ICloudServiceTemplateManager;
import net.suqatri.redicloud.api.utils.ApplicationType;
import net.suqatri.redicloud.api.utils.Files;
import net.suqatri.redicloud.commons.file.FileWriter;
import net.suqatri.redicloud.limbo.api.console.LimboConsole;
import net.suqatri.redicloud.limbo.api.scheduler.RepeatSchedulerTask;
import net.suqatri.redicloud.limbo.api.scheduler.Scheduler;
import net.suqatri.redicloud.limbo.api.service.CloudLimboServiceManager;
import net.suqatri.redicloud.limbo.server.LimboServer;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Getter
public class LimboCloudAPI extends CloudDefaultAPIImpl<CloudService> {

    @Getter
    private static LimboCloudAPI instance;

    private final LimboServer server;
    private final LimboConsole console;
    private RedisConnection redisConnection;
    private CloudService service;
    private final Scheduler scheduler;
    private final ICloudServiceFactory serviceFactory;
    private final ICloudServiceManager serviceManager;
    private final ICloudServiceTemplateManager serviceTemplateManager;
    private final ICloudServiceVersionManager serviceVersionManager;
    private boolean isShutdownInitiated = false;
    private RepeatSchedulerTask updaterTask;

    public LimboCloudAPI(LimboServer server) {
        super(ApplicationType.SERVICE_MINECRAFT);
        instance = this;
        this.server = server;
        this.console = new LimboConsole();
        this.scheduler = new Scheduler();
        this.serviceManager = new CloudLimboServiceManager();
        this.serviceFactory = new CloudServiceFactory(this.serviceManager);
        this.serviceTemplateManager = new CloudServiceTemplateManager();
        this.serviceVersionManager = new CloudServiceVersionManager();

        this.initRedis();
        this.initService();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> this.shutdown(true)));
    }

    private void initService(){
        this.service = (CloudService) this.serviceManager.getService(UUID.fromString(System.getenv("redicloud_service_id")));
        this.service.setServiceState(ServiceState.RUNNING_UNDEFINED);
        this.service.setOnlineCount(this.service.getOnlineCount());
        this.service.update();
        getEventManager().postGlobalAsync(new CloudServiceStartedEvent(this.service));

        this.updaterTask = this.scheduler.scheduleTaskAsync(() -> {
            boolean update = false;
            if (this.service.getOnlineCount() != this.server.getConnections().getCount()) {
                this.service.setOnlineCount(this.server.getConnections().getCount());
                update = true;
            }
            if(this.service.getMaxPlayers() != this.server.getConfig().getMaxPlayers()){
                this.service.setMaxPlayers(this.server.getConfig().getMaxPlayers());
                update = true;
            }
            if(update) this.service.updateAsync();
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void initRedis() {

        RedisCredentials redisCredentials;
        try {
            redisCredentials = FileWriter.readObject(new File(System.getenv("redicloud_files_" + Files.REDIS_CONFIG.name().toLowerCase())), RedisCredentials.class);
        } catch (Exception e) {
            this.console.error("Failed to read redis.json file! Please check your credentials.", e);
            System.out.println(1);
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
            System.out.println(1);
            return;
        }
    }

    @Override
    public void updateApplicationProperties(CloudService object) {
        if(!object.getUniqueId().equals(this.service.getUniqueId())) return;
        this.server.getConfig().getPingData().setDescription(object.getMotd());
        this.service.setMaxPlayers(object.getMaxPlayers());
    }

    @Override
    public ICommandManager<?> getCommandManager() {
        return null;
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

            if(this.service.isFallback()){
                for (ICloudPlayer player : CloudAPI.getInstance().getPlayerManager().getConnectedPlayers().getBlockOrNull()) {
                    if(player.getLastConnectedServerId().equals(this.service.getUniqueId())) {
                        ICloudService fallback = CloudAPI.getInstance().getServiceManager().getFallbackService(player, this.service);
                        player.getBridge().connect(fallback);
                    }
                }
            }
        }

        if (this.updaterTask != null) this.updaterTask.cancel();

        if(this.redisConnection != null) this.redisConnection.disconnect();

        this.server.stop();
    }
}
