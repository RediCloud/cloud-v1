package net.suqatri.cloud.node.console;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.suqatri.cloud.api.console.IConsoleLine;
import net.suqatri.cloud.api.console.LogLevel;

@Getter
public class ConsoleLine implements IConsoleLine {

    private final long time = System.currentTimeMillis();
    private final LogLevel logLevel;
    private final String prefix;
    private final String message;
    private boolean timestamp = true;

    public ConsoleLine(LogLevel logLevel, String message){
        this.logLevel = logLevel;
        this.message = message;
        this.prefix = logLevel.name();
    }

    public ConsoleLine(String prefix, String message){
        this.logLevel = null;
        this.prefix = prefix;
        this.message = message;
    }

    public void disableTimestamp(){
        this.timestamp = false;
    }

    @Override
    public void println() {
        //TODO rewrite NodeConsole to use this method
    }
}
