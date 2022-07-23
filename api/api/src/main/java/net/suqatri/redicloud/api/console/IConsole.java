package net.suqatri.redicloud.api.console;

import java.util.Collection;

public interface IConsole {

    Collection<IConsoleLineEntry> getLineEntries();

    void log(IConsoleLine consoleLine);

    void error(String message, Throwable throwable);

    void error(String message);

    void warn(String message);

    void info(String message);

    void debug(String message);

    void trace(String message);

    void fatal(String message, Throwable throwable);

    LogLevel getLogLevel();

    void setLogLevel(LogLevel level);

    default boolean canLog(IConsoleLine consoleLine) {
        return consoleLine.getLogLevel().getId() >= getLogLevel().getId();
    }

    default boolean canLog(LogLevel level) {
        return level.getId() >= getLogLevel().getId();
    }

    void clearScreen();

    String translateColorCodes(String message);

}
