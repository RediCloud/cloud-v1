package net.suqatri.redicloud.api.console;

public interface IConsoleLine extends IConsoleLineEntry {

    String getPrefix();
    boolean printPrefix();
    IConsoleLine setPrintPrefix(boolean printPrefix);

    String getMessage();

    LogLevel getLogLevel();

    void println();

    long getTime();

    boolean printTimestamp();
    IConsoleLine setPrintTimestamp(boolean printTimestamp);

    IConsoleLine setStored(boolean stored);
    boolean isStored();

}
