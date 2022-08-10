package dev.redicloud.node.commands;

import dev.redicloud.commands.ConsoleCommand;
import dev.redicloud.commands.annotation.CommandAlias;
import dev.redicloud.commands.annotation.Default;
import dev.redicloud.node.NodeLauncher;

@CommandAlias("clear")
public class ClearCommand extends ConsoleCommand {

    @Default
    public void onClear() {
        NodeLauncher.getInstance().getConsole().getLineEntries().clear();
        NodeLauncher.getInstance().getConsole().clearScreen();
    }

}
