package dev.redicloud.api;

import dev.redicloud.api.configuration.IConfigurationManager;
import dev.redicloud.api.console.ICommandManager;
import dev.redicloud.api.console.IConsole;
import dev.redicloud.api.event.ICloudEventManager;
import dev.redicloud.api.group.ICloudGroupManager;
import dev.redicloud.api.network.INetworkComponentInfo;
import dev.redicloud.api.network.INetworkComponentManager;
import dev.redicloud.api.node.ICloudNodeManager;
import dev.redicloud.api.packet.ICloudPacketManager;
import dev.redicloud.api.player.ICloudPlayer;
import dev.redicloud.api.player.ICloudPlayerManager;
import dev.redicloud.api.player.IPlayerBridge;
import dev.redicloud.api.scheduler.IScheduler;
import dev.redicloud.api.service.ICloudServiceManager;
import dev.redicloud.api.service.factory.ICloudServiceFactory;
import dev.redicloud.api.service.version.ICloudServiceVersionManager;
import dev.redicloud.api.template.ICloudServiceTemplateManager;
import dev.redicloud.api.utils.ApplicationType;
import dev.redicloud.api.utils.ICloudProperties;
import dev.redicloud.dependency.DependencyLoader;
import lombok.Getter;

import java.util.concurrent.ExecutorService;

@Getter
public abstract class CloudAPI {

    @Getter
    private static CloudAPI instance;

    private final ApplicationType applicationType;
    private final DependencyLoader dependencyLoader;

    private boolean shutdownHookAdded = false;

    public CloudAPI(ApplicationType type, DependencyLoader dependencyLoader) {
        instance = this;
        this.dependencyLoader = dependencyLoader;
        initShutdownHook();
        this.applicationType = type;
    }

    public abstract IConfigurationManager getConfigurationManager();

    public abstract IPlayerBridge createBridge(ICloudPlayer player);

    public abstract ExecutorService getExecutorService();

    public abstract IScheduler getScheduler();

    public abstract ICloudProperties getProperties();

    public abstract IConsole getConsole();

    public abstract ICommandManager<?> getCommandManager();

    public abstract ICloudGroupManager getGroupManager();

    public abstract ICloudNodeManager getNodeManager();

    public abstract ICloudPacketManager getPacketManager();

    public abstract ICloudPlayerManager getPlayerManager();

    public abstract ICloudServiceFactory getServiceFactory();

    public abstract ICloudServiceManager getServiceManager();

    public abstract ICloudServiceTemplateManager getServiceTemplateManager();

    public abstract ICloudEventManager getEventManager();

    public abstract INetworkComponentManager getNetworkComponentManager();

    public abstract INetworkComponentInfo getNetworkComponentInfo();

    public abstract ICloudServiceVersionManager getServiceVersionManager();

    public abstract void initShutdownHook();

    public abstract void shutdown(boolean fromHook);
}
