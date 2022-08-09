package net.suqatri.redicloud.limbo.server;

public interface Command {

    void execute();

    String description();

}
