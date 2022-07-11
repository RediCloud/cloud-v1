package net.suqatri.cloud.api;

import lombok.Getter;
import net.suqatri.cloud.api.console.ICommandManager;
import net.suqatri.cloud.api.console.IConsole;
import net.suqatri.cloud.api.event.ICloudEventManager;
import net.suqatri.cloud.api.group.ICloudGroup;
import net.suqatri.cloud.api.group.ICloudGroupManager;
import net.suqatri.cloud.api.network.INetworkComponentInfo;
import net.suqatri.cloud.api.network.INetworkComponentManager;
import net.suqatri.cloud.api.node.ICloudNode;
import net.suqatri.cloud.api.node.ICloudNodeManager;
import net.suqatri.cloud.api.packet.ICloudPacketManager;
import net.suqatri.cloud.api.player.ICloudPlayerManager;
import net.suqatri.cloud.api.scheduler.IScheduler;
import net.suqatri.cloud.api.service.ICloudService;
import net.suqatri.cloud.api.service.ICloudServiceManager;
import net.suqatri.cloud.api.service.factory.ICloudServiceFactory;
import net.suqatri.cloud.api.template.ICloudServiceTemplate;
import net.suqatri.cloud.api.template.ICloudServiceTemplateManager;
import net.suqatri.cloud.api.utils.ApplicationType;

@Getter
public abstract class CloudAPI {

    @Getter
    private static final String version = "1.0-SNAPSHOT";

    @Getter
    private static CloudAPI instance;

    private final ApplicationType applicationType;
    private boolean shutdownHookAdded = false;

    public CloudAPI(ApplicationType type){
        instance = this;
        initShutdownHook();
        this.applicationType = type;
    }

    public abstract IScheduler getScheduler();
    public abstract IConsole getConsole();
    public abstract ICommandManager<?> getCommandManager();
    public abstract ICloudGroupManager<? extends ICloudGroup> getGroupManager();
    public abstract ICloudNodeManager<? extends ICloudNode> getNodeManager();
    public abstract ICloudPacketManager getPacketManager();
    public abstract ICloudPlayerManager getPlayerManager();
    public abstract ICloudServiceFactory<? extends ICloudService> getServiceFactory();
    public abstract ICloudServiceManager<? extends ICloudService> getServiceManager();
    public abstract ICloudServiceTemplateManager<? extends ICloudServiceTemplate> getServiceTemplateManager();
    public abstract ICloudEventManager getEventManager();
    public abstract INetworkComponentManager<? extends INetworkComponentInfo> getNetworkComponentManager();
    public abstract INetworkComponentInfo getNetworkComponentInfo();

    public abstract void initShutdownHook();
    public abstract void shutdown(boolean fromHook);
}
