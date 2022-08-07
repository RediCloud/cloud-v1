package net.suqatri.redicloud.limbo.server.commands;

import net.suqatri.redicloud.limbo.server.Command;

public class CmdStop implements Command {

    @Override
    public void execute() {
        System.exit(0);
    }

    @Override
    public String description() {
        return "Stop the server";
    }
}
