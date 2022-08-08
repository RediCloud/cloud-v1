package net.suqatri.redicloud.limbo.server.commands;

import net.suqatri.redicloud.limbo.server.Command;
import net.suqatri.redicloud.limbo.server.LimboServer;
import net.suqatri.redicloud.limbo.server.Logger;

import java.util.Map;

public class CmdHelp implements Command {

    private final LimboServer server;

    public CmdHelp(LimboServer server) {
        this.server = server;
    }

    @Override
    public void execute() {
        Map<String, Command> commands = server.getCommandManager().getCommands();

        Logger.info("Available commands:");

        for (Map.Entry<String, Command> entry : commands.entrySet()) {
            Logger.info("%s - %s", entry.getKey(), entry.getValue().description());
        }
    }

    @Override
    public String description() {
        return "Show this message";
    }
}
