package net.suqatri.cloud.api.proxy;

import net.suqatri.cloud.api.console.ICommandManager;
import net.suqatri.cloud.api.console.IConsole;
import net.suqatri.cloud.api.event.ICloudEventManager;
import net.suqatri.cloud.api.group.ICloudGroupManager;
import net.suqatri.cloud.api.impl.CloudAPIImpl;
import net.suqatri.cloud.api.node.ICloudNodeManager;
import net.suqatri.cloud.api.packet.ICloudPacketManager;
import net.suqatri.cloud.api.player.ICloudPlayerManager;
import net.suqatri.cloud.api.service.ICloudServiceManager;
import net.suqatri.cloud.api.service.factory.ICloudServiceFactory;
import net.suqatri.cloud.api.template.ICloudServiceTemplateManager;
import net.suqatri.cloud.api.utils.ApplicationType;

public class ProxyCloudAPI extends CloudAPIImpl {

    public ProxyCloudAPI() {
        super(ApplicationType.SERVICE_PROXY);
    }

    @Override
    public IConsole getConsole() {
        return null;
    }

    @Override
    public ICommandManager<?> getCommandManager() {
        return null;
    }

    @Override
    public ICloudGroupManager getGroupManager() {
        return null;
    }

    @Override
    public ICloudNodeManager getNodeManager() {
        return null;
    }

    @Override
    public ICloudPacketManager getPacketManager() {
        return null;
    }

    @Override
    public ICloudPlayerManager getPlayerManager() {
        return null;
    }

    @Override
    public ICloudServiceFactory getServiceFactory() {
        return null;
    }

    @Override
    public ICloudServiceManager getServiceManager() {
        return null;
    }

    @Override
    public ICloudServiceTemplateManager getServiceTemplateManager() {
        return null;
    }

    @Override
    public ICloudEventManager getEventManager() {
        return null;
    }
}
