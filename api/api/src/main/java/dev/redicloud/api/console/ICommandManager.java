package dev.redicloud.api.console;

public interface ICommandManager<C> {

    void registerCommand(C command);

    void unregisterCommand(C command);

}
