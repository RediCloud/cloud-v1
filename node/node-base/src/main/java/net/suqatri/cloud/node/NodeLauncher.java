package net.suqatri.cloud.node;

import lombok.Getter;
import net.suqatri.api.node.NodeCloudAPI;
import net.suqatri.cloud.api.console.ICommandManager;
import net.suqatri.cloud.api.console.IConsole;
import net.suqatri.cloud.api.event.CloudGlobalEvent;
import net.suqatri.cloud.api.event.ICloudEventManager;
import net.suqatri.cloud.api.group.ICloudGroupManager;
import net.suqatri.cloud.api.node.ICloudNode;
import net.suqatri.cloud.api.node.ICloudNodeManager;
import net.suqatri.cloud.api.packet.ICloudPacketManager;
import net.suqatri.cloud.api.player.ICloudPlayerManager;
import net.suqatri.cloud.api.service.ICloudServiceManager;
import net.suqatri.cloud.api.service.factory.ICloudServiceFactory;
import net.suqatri.cloud.api.template.ICloudServiceTemplateManager;
import net.suqatri.cloud.node.commands.ClearCommand;
import net.suqatri.cloud.node.console.CommandConsoleManager;
import net.suqatri.cloud.node.console.NodeConsole;

public class NodeLauncher extends NodeCloudAPI{

    @Getter
    private static NodeLauncher instance;
    private final NodeConsole console;
    private final CommandConsoleManager commandManager;

    public NodeLauncher(String[] args) throws Exception{
        instance = this;
        this.commandManager = new CommandConsoleManager();
        this.console = new NodeConsole(this.commandManager);

        this.registerCommands();
        this.connectToCluster();
    }

    private void registerCommands(){
        this.commandManager.registerCommand(new ClearCommand());
    }

    private void connectToCluster(){

    }

    @Override
    public void shutdown() {
        this.console.stopThread();
    }

    @Override
    protected ICloudNode getCloudNode() {
        return null;
    }

    @Override
    public IConsole getConsole() {
        return this.console;
    }

    @Override
    public ICommandManager<?> getCommandManager() {
        return this.commandManager;
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
