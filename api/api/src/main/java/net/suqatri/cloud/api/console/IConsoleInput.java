package net.suqatri.cloud.api.console;

public interface IConsoleInput extends IConsoleLineEntry{

    String getInput();
    String getPrefix();

    void logAsFake();

}
