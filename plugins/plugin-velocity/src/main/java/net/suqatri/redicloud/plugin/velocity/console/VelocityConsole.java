package net.suqatri.redicloud.plugin.velocity.console;

import lombok.Data;
import net.suqatri.redicloud.api.console.IConsole;
import net.suqatri.redicloud.api.console.IConsoleLine;
import net.suqatri.redicloud.api.console.IConsoleLineEntry;
import net.suqatri.redicloud.api.console.LogLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Data
public class VelocityConsole implements IConsole {

    private final List<IConsoleLineEntry> lineEntries;
    private LogLevel logLevel = LogLevel.INFO;

    public VelocityConsole() {
        this.lineEntries = new ArrayList<>();
        this.logLevel = LogLevel.valueOf(System.getenv().containsKey("redicloud_log_level")
                ? System.getenv("redicloud_log_level") : "DEBUG");
    }

    @Override
    public void log(IConsoleLine consoleLine) {
        System.out.println(consoleLine.getPrefix() + ": " + consoleLine.getMessage());
    }

    @Override
    public void error(String message, Throwable throwable) {
        if (!canLog(LogLevel.ERROR)) return;
        System.out.println("[ERROR] " + message);
        if (throwable != null) {
            throwable.printStackTrace();
        }
    }

    @Override
    public void error(String message) {
        if (!canLog(LogLevel.ERROR)) return;
        System.out.println("[ERROR] " + message);
    }

    @Override
    public void warn(String message) {
        if (!canLog(LogLevel.WARN)) return;
        System.out.println("[WARN] " + message);
    }

    @Override
    public void info(String message) {
        if (!canLog(LogLevel.INFO)) return;
        System.out.println("[INFO] " + message);
    }

    @Override
    public void debug(String message) {
        if (!canLog(LogLevel.DEBUG)) return;
        System.out.println("[DEBUG] " + message);
    }

    @Override
    public void trace(String message) {
        if (!canLog(LogLevel.TRACE)) return;
        System.out.println("[TRACE] " + message);
    }

    @Override
    public void fatal(String message, Throwable throwable) {
        if (!canLog(LogLevel.FATAL)) return;
        System.out.println("[FATAL] " + message);
        if (throwable != null) {
            throwable.printStackTrace();
        }
    }

    @Override
    public void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    @Override
    public String translateColorCodes(String message) {
        return message;
    }
}