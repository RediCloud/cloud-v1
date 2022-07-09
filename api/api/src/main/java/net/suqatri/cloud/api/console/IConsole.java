package net.suqatri.cloud.api.console;

public interface IConsole {

    void log(LogLevel logLevel, String message);

    void print(String message);

    default void error(String message, Throwable ... args) {
        log(LogLevel.ERROR, String.format(message, args));
    }

    default void warn(String message, Throwable ... args) {
        log(LogLevel.WARN, String.format(message, args));
    }

    default void warn(String message){
        log(LogLevel.WARN, message);
    }

    default void info(String message) {
        log(LogLevel.INFO, message);
    }

    default void debug(String message) {
        log(LogLevel.DEBUG, message);
    }

    default void fatal(String message){
        log(LogLevel.FATAL, message);
    }

    void setLogLevel(LogLevel level);
    LogLevel getLogLevel();

    default boolean canLog(LogLevel logLevel) {
        return  logLevel.getId() >= getLogLevel().getId();
    }

    void clearScreen();

    String translateColorCodes(String message);

}
