package net.suqatri.cloud.api.console;

import java.util.Collection;
import java.util.List;

public interface IConsole {

    Collection<IConsoleLineEntry> getLineEntries();

    void log(IConsoleLine consoleLine);

    void error(String message, Throwable throwable);
    void error(String message);

    void warn(String message);

    void info(String message);

    void debug(String message);

    void fatal(String message, Throwable throwable);

    void setLogLevel(LogLevel level);
    LogLevel getLogLevel();

    default boolean canLog(IConsoleLine consoleLine) {
        return consoleLine.getLogLevel().getId() >= getLogLevel().getId();
    }

    default boolean canLog(LogLevel level) {
        return level.getId() >= getLogLevel().getId();
    }

    void clearScreen();

    String translateColorCodes(String message);

}
