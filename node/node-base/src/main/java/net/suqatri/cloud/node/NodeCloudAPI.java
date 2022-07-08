package net.suqatri.cloud.node;

import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.console.ICommandManager;
import net.suqatri.cloud.api.console.IConsole;
import net.suqatri.cloud.node.console.CommandConsoleManager;
import net.suqatri.cloud.node.console.NodeConsole;

public class NodeCloudAPI extends CloudAPI {

    private final NodeConsole nodeConsole;
    private final CommandConsoleManager commandConsoleManager;

    public NodeCloudAPI() throws Exception {
        this.commandConsoleManager = new CommandConsoleManager();
        this.nodeConsole = this.commandConsoleManager.createNodeConsole();
    }

    @Override
    public IConsole getConsole() {
        return this.nodeConsole;
    }

    @Override
    public ICommandManager<?> getCommandManager() {
        return this.commandConsoleManager;
    }
}
