package net.suqatri.cloud.api.console;

public interface IConsoleLine {

    String getPrefix();
    String getMessage();
    LogLevel getLogLevel();
    void println();

}
