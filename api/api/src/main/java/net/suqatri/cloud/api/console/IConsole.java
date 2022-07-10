package net.suqatri.cloud.api.console;

public interface IConsole {

    default void log(LogLevel logLevel, String message) {
        log(logLevel, message, true, true);
    }
    void log(LogLevel logLevel, String message, boolean translateColorCodes, boolean storeInHistory);

    void print(String message);
    void printRaw(String message, boolean translateColorCodes, boolean storeInHistory);

    default void error(String message, Throwable throwable) {
        log(LogLevel.FATAL, message);
        log(LogLevel.FATAL, throwable.getMessage());
        log(LogLevel.FATAL, throwable.getStackTrace());
    }

    default void error(String message){
        log(LogLevel.ERROR, message);
    }

    default void warn(String message, Throwable throwable) {
        log(LogLevel.FATAL, message);
        log(LogLevel.FATAL, throwable.getMessage());
        log(LogLevel.FATAL, throwable.getStackTrace());
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

    default void fatal(String message, Throwable throwable){
        log(LogLevel.FATAL, message);
        log(LogLevel.FATAL, throwable.getMessage());
        log(LogLevel.FATAL, throwable.getStackTrace());
    }

    default void log(LogLevel level, Object[] messages){
        for(Object message : messages) log(level, message.toString());
    }

    void setLogLevel(LogLevel level);
    LogLevel getLogLevel();

    default boolean canLog(LogLevel logLevel) {
        return  logLevel.getId() >= getLogLevel().getId();
    }

    void clearScreen();

    String translateColorCodes(String message);

}
