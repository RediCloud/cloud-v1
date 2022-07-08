package net.suqatri.cloud.api.console;

public interface ICommandManager<C> {

    void registerCommand(C command);
    void unregisterCommand(C command);

}
