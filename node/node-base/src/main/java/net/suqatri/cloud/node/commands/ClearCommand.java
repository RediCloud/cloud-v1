package net.suqatri.cloud.node.commands;

import net.suqatri.cloud.node.NodeLauncher;
import net.suqatri.commands.ConsoleCommand;
import net.suqatri.commands.annotation.CommandAlias;
import net.suqatri.commands.annotation.Default;

@CommandAlias("clear")
public class ClearCommand extends ConsoleCommand {

    @Default
    public void onClear(){
        NodeLauncher.getInstance().getConsole().getLineEntries().clear();
        NodeLauncher.getInstance().getConsole().clearScreen();
    }

}
