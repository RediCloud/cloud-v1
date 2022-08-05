package net.suqatri.redicloud.node.commands;

import net.suqatri.commands.BaseCommand;
import net.suqatri.commands.CommandSender;
import net.suqatri.commands.ConsoleCommand;
import net.suqatri.commands.RootCommand;
import net.suqatri.commands.annotation.CommandAlias;
import net.suqatri.commands.annotation.Default;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.node.NodeLauncher;

import java.util.ArrayList;
import java.util.List;

@CommandAlias("help")
public class CloudHelpCommand extends ConsoleCommand {

    @Default
    public void onHelp(CommandSender commandSender) {
        commandSender.sendMessage("§8<------------|§7 %tcGeneral help §8|------------§8>");
        List<BaseCommand> commands = new ArrayList<>();
        for (RootCommand registeredRootCommand : NodeLauncher.getInstance().getCommandManager().getRegisteredRootCommands()) {
            commands.add(registeredRootCommand.getDefCommand());
        }
        if(commands.isEmpty()){
            commandSender.sendMessage("No commands registered");
            return;
        }
        for (BaseCommand command : commands) {
            commandSender.sendMessage("%hc" + command.getName() + " %tchelp §8| %tcHelp for the " + command.getName() + " command");
        }
    }

}
