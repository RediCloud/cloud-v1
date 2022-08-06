package net.suqatri.redicloud.api;

import lombok.Getter;
import net.suqatri.redicloud.api.console.ICommandManager;
import net.suqatri.redicloud.api.console.IConsole;
import net.suqatri.redicloud.api.event.ICloudEventManager;
import net.suqatri.redicloud.api.group.ICloudGroupManager;
import net.suqatri.redicloud.api.network.INetworkComponentInfo;
import net.suqatri.redicloud.api.network.INetworkComponentManager;
import net.suqatri.redicloud.api.node.ICloudNodeManager;
import net.suqatri.redicloud.api.packet.ICloudPacketManager;
import net.suqatri.redicloud.api.player.ICloudPlayer;
import net.suqatri.redicloud.api.player.ICloudPlayerManager;
import net.suqatri.redicloud.api.player.IPlayerBridge;
import net.suqatri.redicloud.api.scheduler.IScheduler;
import net.suqatri.redicloud.api.service.ICloudServiceManager;
import net.suqatri.redicloud.api.service.factory.ICloudServiceFactory;
import net.suqatri.redicloud.api.service.version.ICloudServiceVersionManager;
import net.suqatri.redicloud.api.template.ICloudServiceTemplateManager;
import net.suqatri.redicloud.api.utils.ApplicationType;
import net.suqatri.redicloud.api.utils.ICloudProperties;

import java.util.UUID;
import java.util.concurrent.ExecutorService;

@Getter
public abstract class CloudAPI {

    @Getter
    private static CloudAPI instance;

    private final ApplicationType applicationType;
    private boolean shutdownHookAdded = false;

    public CloudAPI(ApplicationType type) {
        instance = this;
        initShutdownHook();
        this.applicationType = type;
    }

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
