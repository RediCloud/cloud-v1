package dev.redicloud.node.console;

import lombok.Data;
import dev.redicloud.api.console.IConsoleInput;

@Data
public class ConsoleInput implements IConsoleInput {

    private final String input;
    private final long time;
    private final String prefix;

    @Override
    public void logAsFake() {
        CommandConsoleManager.getInstance().getNodeConsole().log(new ConsoleLine("", prefix + input).setStored(false).setPrintTimestamp(false).setPrintPrefix(false));
    }
}
