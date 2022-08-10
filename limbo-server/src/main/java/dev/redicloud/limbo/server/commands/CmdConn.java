package dev.redicloud.limbo.server.commands;

import dev.redicloud.limbo.server.Command;
import dev.redicloud.limbo.server.LimboServer;
import dev.redicloud.limbo.server.Logger;

public class CmdConn implements Command {

    private final LimboServer server;

    public CmdConn(LimboServer server) {
        this.server = server;
    }

    @Override
    public void execute() {
        Logger.info("Connections: %d", server.getConnections().getCount());
    }

    @Override
    public String description() {
        return "Display connections count";
    }
}
