package dev.redicloud.api.console;

public interface IConsoleInput extends IConsoleLineEntry {

    String getInput();

    String getPrefix();

    void logAsFake();

}
