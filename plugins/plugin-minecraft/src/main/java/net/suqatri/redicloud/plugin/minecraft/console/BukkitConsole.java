package net.suqatri.redicloud.plugin.minecraft.console;

import lombok.Data;
import net.suqatri.redicloud.api.console.IConsole;
import net.suqatri.redicloud.api.console.IConsoleLine;
import net.suqatri.redicloud.api.console.IConsoleLineEntry;
import net.suqatri.redicloud.api.console.LogLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

@Data
public class BukkitConsole implements IConsole {

    private final Logger logger;

    private final List<IConsoleLineEntry> lineEntries;
    private LogLevel logLevel = LogLevel.INFO;

    public BukkitConsole(Logger logger) {
        this.logger = logger;
        this.lineEntries = new ArrayList<>();
        this.logLevel = LogLevel.valueOf(System.getenv("redicloud_log_level"));
    }

    @Override
    public void log(IConsoleLine consoleLine) {
        this.logger.log(consoleLine.getLogLevel().getLevel(), consoleLine.getMessage());
    }

    @Override
    public void error(String message, Throwable throwable) {
        if (!canLog(LogLevel.ERROR)) return;
        if (throwable != null) {
            this.logger.log(LogLevel.ERROR.getLevel(), message, throwable);
        }else{
            this.logger.log(LogLevel.ERROR.getLevel(), message);
        }
    }

    @Override
    public void error(String message) {
        if (!canLog(LogLevel.ERROR)) return;
        this.logger.log(LogLevel.ERROR.getLevel(), message);
    }

    @Override
    public void warn(String message) {
        if (!canLog(LogLevel.WARN)) return;
        this.logger.log(LogLevel.WARN.getLevel(), message);
    }

    @Override
    public void info(String message) {
        if (!canLog(LogLevel.INFO)) return;
        this.logger.log(LogLevel.INFO.getLevel(), message);
    }

    @Override
    public void debug(String message) {
        if (!canLog(LogLevel.DEBUG)) return;
        this.logger.log(LogLevel.DEBUG.getLevel(), message);
    }

    @Override
    public void trace(String message) {
        if (!canLog(LogLevel.TRACE)) return;
        this.logger.log(LogLevel.TRACE.getLevel(), message);
    }

    @Override
    public void fatal(String message, Throwable throwable) {
        if (!canLog(LogLevel.FATAL)) return;
        if (throwable != null) {
            this.logger.log(LogLevel.FATAL.getLevel(), message, throwable);
        }else{
            this.logger.log(LogLevel.FATAL.getLevel(), message);
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
