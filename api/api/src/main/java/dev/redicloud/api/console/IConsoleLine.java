package dev.redicloud.api.console;

public interface IConsoleLine extends IConsoleLineEntry {

    String getPrefix();

    boolean printPrefix();

    IConsoleLine setPrintPrefix(boolean printPrefix);

    String getMessage();

    LogLevel getLogLevel();

    boolean isLogToFile();

    IConsoleLine disableFileLogging();

    void println();

    long getTime();

    boolean printTimestamp();

    IConsoleLine setPrintTimestamp(boolean printTimestamp);

    boolean isStored();

    IConsoleLine setStored(boolean stored);

}
