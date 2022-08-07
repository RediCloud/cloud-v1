package net.suqatri.redicloud.limbo.server.commands;

import net.suqatri.redicloud.limbo.server.Command;
import net.suqatri.redicloud.limbo.server.LimboServer;
import net.suqatri.redicloud.limbo.server.Logger;

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
