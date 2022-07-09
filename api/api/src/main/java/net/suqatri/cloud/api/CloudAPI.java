package net.suqatri.cloud.api;

import lombok.Getter;
import net.suqatri.cloud.api.console.ICommandManager;
import net.suqatri.cloud.api.console.IConsole;
import net.suqatri.cloud.api.event.ICloudEventManager;
import net.suqatri.cloud.api.group.ICloudGroupManager;
import net.suqatri.cloud.api.node.ICloudNodeManager;
import net.suqatri.cloud.api.packet.ICloudPacketManager;
import net.suqatri.cloud.api.player.ICloudPlayerManager;
import net.suqatri.cloud.api.scheduler.IScheduler;
import net.suqatri.cloud.api.service.ICloudServiceManager;
import net.suqatri.cloud.api.service.factory.ICloudServiceFactory;
import net.suqatri.cloud.api.template.ICloudServiceTemplateManager;
import net.suqatri.cloud.api.utils.ApplicationType;

@Getter
public abstract class CloudAPI {

    @Getter
    private static CloudAPI instance;

    private final ApplicationType applicationType;

    public CloudAPI(ApplicationType type){
        instance = this;
        this.applicationType = type;
    }

    public abstract IScheduler getScheduler();
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

}
