package net.suqatri.redicloud.node.commands;

import net.suqatri.commands.CommandSender;
import net.suqatri.commands.ConsoleCommand;
import net.suqatri.commands.annotation.CommandAlias;
import net.suqatri.commands.annotation.Default;

@CommandAlias("help")
public class CloudHelpCommand extends ConsoleCommand {

    @Default
    public void onHelp(CommandSender commandSender){
        commandSender.sendMessage("§8<------------|§7 %tcGeneral help §8|------------§8>");
        commandSender.sendMessage("%hccluster %tchelp §8| %tcHelp for the cluster command");
        commandSender.sendMessage("%hctemplate %tchelp §8| %tcHelp for the templates command");
        commandSender.sendMessage("%hcgroups %tchelp §8| %tcHelp for the groups command");
        commandSender.sendMessage("%hcstop §8| %tcStop the node");
        commandSender.sendMessage("%hcclear §8| %tcClear the console");
        commandSender.sendMessage("%hcserviceversions %tchelp §8| %tcHelp for the service versions command");
        commandSender.sendMessage("%hcservice %tchelp §8| %tcHelp for the service command");

    }

}
