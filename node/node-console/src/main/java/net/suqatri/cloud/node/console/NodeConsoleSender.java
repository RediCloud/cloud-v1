package net.suqatri.cloud.node.console;

import net.suqatri.cloud.api.console.LogLevel;
import net.suqatri.commands.console.CommandSender;

public class NodeConsoleSender extends CommandSender {

    private CommandConsoleManager consoleManager;

    public NodeConsoleSender(CommandConsoleManager consoleManager) {
        super(consoleManager.getNodeConsole().getPrefix());
        this.consoleManager = consoleManager;
    }

    @Override
    public void sendMessage(String message) {
        this.consoleManager.getNodeConsole().log(LogLevel.INFO, message);
    }
}
