package dev.redicloud.limbo.server;

public interface Command {

    void execute();

    String description();

}
