package net.suqatri.cloud.node.console;

import lombok.Getter;
import net.suqatri.cloud.api.console.IConsoleLine;
import net.suqatri.cloud.api.console.LogLevel;

@Getter
public class ConsoleLine implements IConsoleLine, Cloneable{

    private final long time = System.currentTimeMillis();
    private final LogLevel logLevel;
    private final String prefix;
    private final String message;
    private boolean stored = true;
    private boolean printTimestamp = true;
    private boolean printPrefix = true;

    public ConsoleLine(LogLevel logLevel, String message){
        this.logLevel = logLevel;
        this.message = message;
        this.prefix = logLevel.name();
    }

    public ConsoleLine(String prefix, String message){
        this.logLevel = LogLevel.INFO;
        this.prefix = prefix;
        this.message = message;
    }

    @Override
    public boolean printPrefix() {
        return this.printPrefix;
    }

    @Override
    public IConsoleLine setPrintPrefix(boolean printPrefix) {
        this.printPrefix = printPrefix;
        return this;
    }

    @Override
    public void println() {
        CommandConsoleManager.getInstance().getNodeConsole().log(clone().setStored(false));
    }

    @Override
    public boolean printTimestamp() {
        return this.printTimestamp;
    }

    @Override
    public IConsoleLine setPrintTimestamp(boolean printTimestamp) {
        this.printTimestamp = printTimestamp;
        return this;
    }

    @Override
    public IConsoleLine setStored(boolean stored) {
        this.stored = stored;
        return this;
    }

    @Override
    public ConsoleLine clone() {
        try {
            ConsoleLine clone = (ConsoleLine) super.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
