package net.suqatri.cloud.api.minecraft.console;

import lombok.Data;
import lombok.Getter;
import net.suqatri.cloud.api.console.IConsole;
import net.suqatri.cloud.api.console.IConsoleLine;
import net.suqatri.cloud.api.console.IConsoleLineEntry;
import net.suqatri.cloud.api.console.LogLevel;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;

@Data
public class BukkitConsole implements IConsole {

    //TODO: REWRITE THIS CLASS AND USE LOGGER
    private final Logger logger;

    private final List<IConsoleLineEntry> lineEntries = new ArrayList<>();
    private LogLevel logLevel = LogLevel.DEBUG;

    @Override
    public void log(IConsoleLine consoleLine) {
        System.out.println(consoleLine.getPrefix() + ": " + consoleLine.getMessage());
    }

    @Override
    public void error(String message, Throwable throwable) {
        if(!canLog(LogLevel.ERROR)) return;
        System.out.println("[ERROR] " + message);
        if (throwable != null) {
            throwable.printStackTrace();
        }
    }

    @Override
    public void error(String message) {
        if(!canLog(LogLevel.ERROR)) return;
        System.out.println("[ERROR] " + message);
    }

    @Override
    public void warn(String message) {
        if(!canLog(LogLevel.WARN)) return;
        System.out.println("[WARN] " + message);
    }

    @Override
    public void info(String message) {
        if(!canLog(LogLevel.INFO)) return;
        System.out.println("[INFO] " + message);
    }

    @Override
    public void debug(String message) {
        if(!canLog(LogLevel.DEBUG)) return;
        System.out.println("[DEBUG] " + message);
    }

    @Override
    public void fatal(String message, Throwable throwable) {
        if(!canLog(LogLevel.FATAL)) return;
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
