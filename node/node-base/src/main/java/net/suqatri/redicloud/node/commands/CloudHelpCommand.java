package net.suqatri.redicloud.node.commands;

import net.suqatri.commands.CommandSender;
import net.suqatri.commands.ConsoleCommand;
import net.suqatri.commands.RootCommand;
import net.suqatri.commands.annotation.CommandAlias;
import net.suqatri.commands.annotation.Default;
import net.suqatri.redicloud.node.NodeLauncher;

@CommandAlias("help")
public class CloudHelpCommand extends ConsoleCommand {

    @Default
    public void onHelp(CommandSender commandSender) {
        commandSender.sendMessage("§8<------------|§7 %tcGeneral help §8|------------§8>");
        for (RootCommand registeredRootCommand : NodeLauncher.getInstance().getCommandManager().getRegisteredRootCommands()) {
            commandSender.sendMessage("%hc" + registeredRootCommand.getCommandName() + " %tchelp §8| %tcHelp for the " + registeredRootCommand.getCommandName() + " command");
        }
    }

}
