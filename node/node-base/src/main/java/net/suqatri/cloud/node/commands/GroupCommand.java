package net.suqatri.cloud.node.commands;

import net.suqatri.commands.CommandHelp;
import net.suqatri.commands.CommandSender;
import net.suqatri.commands.ConsoleCommand;
import net.suqatri.commands.annotation.*;

@CommandAlias("group")
public class GroupCommand extends ConsoleCommand {

    /*
        * /group create <name>
        * /group delete <name>
        * /group list
        * /group info <name>
        * /group edit <name> <property> <value>
     */

    @Subcommand("help")
    @HelpCommand
    @Default
    public void onHelp(CommandHelp commandHelp){
        commandHelp.showHelp();
    }

    @Subcommand("create")
    @Syntax("<Group>")
    @CommandCompletion("@groups")
    public void onCreate(CommandSender commandSender, String name){

    }

    @Subcommand("delete")
    @Syntax("<Group>")
    @CommandCompletion("@groups")
    public void onDelete(CommandSender commandSender, String name){

    }

    @Subcommand("list")
    public void onList(CommandSender commandSender){

    }

    @Subcommand("edit")
    @Syntax("<Group> <Key> <Value>")
    @CommandCompletion("@groups @group_keys @group_values")
    public void onEdit(CommandSender commandSender, String group, String key, String value){

    }
}
