package net.suqatri.cloud.node.console;

import lombok.Data;
import net.suqatri.cloud.api.console.IConsoleInput;
import net.suqatri.commands.ConsoleCommandManager;

@Data
public class ConsoleInput implements IConsoleInput {

    private final String input;
    private final long time;
    private final String prefix;

    @Override
    public void logAsFake() {
        CommandConsoleManager.getInstance().getNodeConsole().log(new ConsoleLine("", prefix + input).setPrintTimestamp(false).setPrintPrefix(false));
    }
}
