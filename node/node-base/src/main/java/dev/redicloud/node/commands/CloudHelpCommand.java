package dev.redicloud.node.commands;

import dev.redicloud.commands.CommandSender;
import dev.redicloud.commands.ConsoleCommand;
import dev.redicloud.commands.RootCommand;
import dev.redicloud.commands.annotation.CommandAlias;
import dev.redicloud.commands.annotation.Default;
import dev.redicloud.node.NodeLauncher;

import java.util.ArrayList;
import java.util.List;

@CommandAlias("help")
public class CloudHelpCommand extends ConsoleCommand {

    @Default
    public void onHelp(CommandSender commandSender) {
        commandSender.sendMessage("§8<------------|§7 %tcGeneral help §8|------------§8>");
        List<String> commands = new ArrayList<>();
        for (RootCommand registeredRootCommand : NodeLauncher.getInstance().getCommandManager().getRegisteredRootCommands()) {
            if(commands.contains(registeredRootCommand.getDefCommand().getName())) continue;
            commands.add(registeredRootCommand.getDefCommand().getName());
        }
        if(commands.isEmpty()){
            commandSender.sendMessage("No commands registered");
            return;
        }
        for (String command : commands) {
            commandSender.sendMessage("%hc" + command + " %tchelp §8| %tcHelp for the " + command + " command");
        }
    }

}
