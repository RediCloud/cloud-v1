package net.suqatri.redicloud.limbo.api.console;

import lombok.Getter;
import lombok.Setter;
import net.suqatri.redicloud.api.console.IConsole;
import net.suqatri.redicloud.api.console.IConsoleLine;
import net.suqatri.redicloud.api.console.IConsoleLineEntry;
import net.suqatri.redicloud.api.console.LogLevel;
import net.suqatri.redicloud.limbo.server.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class LimboConsole implements IConsole {

    private final List<IConsoleLineEntry> lineEntries;
    private LogLevel logLevel = LogLevel.INFO;

    public LimboConsole() {
        this.lineEntries = new ArrayList<>();
        this.logLevel = LogLevel.valueOf(System.getenv("redicloud_log_level"));
    }

    private Logger.Level translateLogLevel(LogLevel logLevel) {
        switch (logLevel) {
            case DEBUG:
            case TRACE:
                return Logger.Level.DEBUG;
            case INFO:
                return Logger.Level.INFO;
            case WARN:
                return Logger.Level.WARNING;
            case ERROR:
            case FATAL:
                return Logger.Level.ERROR;
        }
        return Logger.Level.INFO;
    }

    @Override
    public void log(IConsoleLine consoleLine) {
        Logger.log(translateLogLevel(consoleLine.getLogLevel()), consoleLine.getMessage());
    }

    @Override
    public void error(String message, Throwable throwable) {
        if (!canLog(LogLevel.ERROR)) return;
        if (throwable != null) {
            Logger.log(translateLogLevel(LogLevel.ERROR), message, throwable);
        }else{
            Logger.log(translateLogLevel(LogLevel.ERROR), message);
        }
    }

    @Override
    public void error(String message) {
        if (!canLog(LogLevel.ERROR)) return;
        Logger.log(translateLogLevel(LogLevel.ERROR), message);
    }

    @Override
    public void warn(String message) {
        if (!canLog(LogLevel.WARN)) return;
        Logger.log(translateLogLevel(LogLevel.WARN), message);
    }

    @Override
    public void info(String message) {
        if (!canLog(LogLevel.INFO)) return;
        Logger.log(translateLogLevel(LogLevel.INFO), message);
    }

    @Override
    public void debug(String message) {
        if (!canLog(LogLevel.DEBUG)) return;
        Logger.log(translateLogLevel(LogLevel.DEBUG), message);
    }

    @Override
    public void trace(String message) {
        if (!canLog(LogLevel.TRACE)) return;
        Logger.log(translateLogLevel(LogLevel.DEBUG), message);
    }

    @Override
    public void fatal(String message, Throwable throwable) {
        if (!canLog(LogLevel.FATAL)) return;
        if (throwable != null) {
            Logger.log(translateLogLevel(LogLevel.FATAL), message, throwable);
        }else{
            Logger.log(translateLogLevel(LogLevel.FATAL), message);
        }
    }

    @Override
    public void clearScreen() {
        throw new UnsupportedOperationException("Not supported for minecraft service instance!");
    }

    @Override
    public String translateColorCodes(String message) {
        return message;
    }

}
