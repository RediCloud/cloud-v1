package net.suqatri.cloud.node.commands;

import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.group.GroupProperty;
import net.suqatri.cloud.api.group.ICloudGroup;
import net.suqatri.cloud.api.impl.group.CloudGroup;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.commons.ConditionChecks;
import net.suqatri.cloud.node.console.setup.SetupControlState;
import net.suqatri.cloud.node.setup.group.GroupSetup;
import net.suqatri.commands.CommandHelp;
import net.suqatri.commands.CommandSender;
import net.suqatri.commands.ConsoleCommand;
import net.suqatri.commands.annotation.*;
import net.suqatri.commands.annotation.HelpCommand;

import java.util.Arrays;
import java.util.UUID;

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
    @Default
    @Description("Show help for group command")
    @Syntax("[Page]")
    @HelpCommand
    public void onHelp(CommandHelp commandHelp){
        commandHelp.showHelp();
    }

    @Subcommand("create")
    @Syntax("<Group>")
    @CommandCompletion("@groups")
    @Description("Create a new group")
    public void onCreate(CommandSender commandSender, String name){
        CloudAPI.getInstance().getGroupManager().existsGroupAsync(name)
                .onFailure(e -> commandSender.sendMessage("§cFailed to create group " + name))
                .onSuccess(exists -> {
                    if(exists){
                        commandSender.sendMessage("§cGroup " + name + " already exists");
                    }else{
                        new GroupSetup().start((groupSetup, setupControlState) -> {
                            if(setupControlState == SetupControlState.FINISHED){

                                CloudGroup cloudGroup = new CloudGroup();
                                cloudGroup.setUniqueId(UUID.randomUUID());
                                cloudGroup.setName(name);
                                cloudGroup.setMinServices(groupSetup.getMinServices());
                                cloudGroup.setMaxServices(groupSetup.getMaxServices());
                                cloudGroup.setStaticGroup(groupSetup.isStaticGroup());
                                cloudGroup.setMaintenance(true);
                                cloudGroup.setMaxMemory(groupSetup.getMaxMemory());
                                cloudGroup.setStartPriority(groupSetup.getStartPriority());

                                CloudAPI.getInstance().getGroupManager().createGroupAsync(cloudGroup)
                                        .onFailure(e2 -> commandSender.sendMessage("§cFailed to create group " + name))
                                        .onSuccess(holder -> commandSender.sendMessage("Group %hc" + name + "%tc created"));
                            }
                        });
                    }
                });
    }

    @Subcommand("delete")
    @Syntax("<Group>")
    @CommandCompletion("@groups")
    @Description("Delete a group")
    public void onDelete(CommandSender commandSender, String name){
        CloudAPI.getInstance().getGroupManager().existsGroupAsync(name)
                .onFailure(e -> commandSender.sendMessage("§cFailed to delete group " + name))
                .onSuccess(exists -> {
                    if(!exists){
                        commandSender.sendMessage("Group %hc" + name + "%tc does not exist");
                    }else{
                        CloudAPI.getInstance().getGroupManager().getGroupAsync(name)
                            .onFailure(e2 -> commandSender.sendMessage("§cFailed to delete group " + name))
                            .onSuccess(holder -> {
                                CloudAPI.getInstance().getGroupManager().deleteGroupAsync(holder.get().getUniqueId())
                                    .onFailure(e3 -> commandSender.sendMessage("§cFailed to delete group " + name))
                                    .onSuccess(t -> commandSender.sendMessage("Group %hc" + name + "%tc deleted!"));
                            });
                    }
                });
    }

    @Subcommand("list")
    @Description("List all groups")
    public void onList(CommandSender commandSender){
        CloudAPI.getInstance().getGroupManager().getGroupsAsync()
                .onFailure(e -> commandSender.sendMessage("§cFailed to get groups"))
                .onSuccess(holders -> {
                    commandSender.sendMessage("");
                    commandSender.sendMessage("§aGroups:");
                    for (IRBucketHolder<ICloudGroup> holder : holders) {
                        commandSender.sendMessage("§8 » %tc" + holder.get().getName() + " | Services: " + holder.get().getOnlineServiceCount() + "/" + holder.get().getMaxServices());
                    }
                    commandSender.sendMessage("");
                });
    }

    @Subcommand("edit")
    @Syntax("<Group> <Key> <Value>")
    @CommandCompletion("@groups @group_keys @group_values")
    @Description("Edit a group property")
    public void onEdit(CommandSender commandSender, String name, String key, String value){
        CloudAPI.getInstance().getGroupManager().existsGroupAsync(name)
                .onFailure(e -> commandSender.sendMessage("§cFailed to edit group " + name))
                .onSuccess(exists -> {
                    if(!exists){
                        commandSender.sendMessage("Group %hc" + name + "%tc does not exist");
                        return;
                    }
                    CloudAPI.getInstance().getGroupManager().getGroupAsync(name)
                            .onFailure(e2 -> commandSender.sendMessage("§cFailed to edit group " + name))
                            .onSuccess(holder -> {
                                try {
                                    GroupProperty property = GroupProperty.valueOf(key);
                                    switch (property){
                                        case MAX_MEMORY:
                                            if(ConditionChecks.isInteger(value)){
                                                commandSender.sendMessage("Value must be an integer");
                                                return;
                                            }
                                            int intValue = Integer.parseInt(value);
                                            if(intValue < 400){
                                                commandSender.sendMessage("Value must be greater than 400");
                                                return;
                                            }
                                            holder.get().setMaxMemory(intValue);
                                            commandSender.sendMessage("Group %hc" + name + "%tc max memory set to %hc" + intValue);
                                            break;
                                        case MAINTENANCE:
                                            if(ConditionChecks.isBoolean(value)){
                                                commandSender.sendMessage("Value must be a boolean");
                                                return;
                                            }
                                            boolean boolValue = Boolean.parseBoolean(value);
                                            holder.get().setMaintenance(boolValue);
                                            commandSender.sendMessage("Group %hc" + name + "%tc maintenance set to %hc" + boolValue);
                                            break;
                                        case MAX_SERVICES:
                                            if(ConditionChecks.isInteger(value)){
                                                commandSender.sendMessage("Value must be an integer");
                                                return;
                                            }
                                            intValue = Integer.parseInt(value);
                                            if(intValue < -1){
                                                commandSender.sendMessage("Value must be greater than -1");
                                                return;
                                            }
                                            holder.get().setMaxServices(intValue);
                                            commandSender.sendMessage("Group %hc" + name + "%tc max services set to %hc" + intValue);
                                            break;
                                        case MIN_SERVICES:
                                            if(ConditionChecks.isInteger(value)){
                                                commandSender.sendMessage("Value must be an integer");
                                                return;
                                            }
                                            intValue = Integer.parseInt(value);
                                            if(intValue <= 0){
                                                commandSender.sendMessage("Value must be greater than 0 or 0");
                                                return;
                                            }
                                            holder.get().setMinServices(intValue);
                                            commandSender.sendMessage("Group %hc" + name + "%tc min services set to %hc" + intValue);
                                            break;
                                        case START_PRIORITY:
                                            if(ConditionChecks.isInteger(value)){
                                                commandSender.sendMessage("Value must be an integer");
                                                return;
                                            }
                                            intValue = Integer.parseInt(value);
                                            holder.get().setStartPriority(intValue);
                                            commandSender.sendMessage("Group %hc" + name + "%tc start priority set to %hc" + intValue);
                                            break;
                                    }
                                    holder.get().updateAsync();
                                }catch (Exception e){
                                    commandSender.sendMessage("§cInvalid property! Properties: " + Arrays.stream(GroupProperty.values()).parallel().map(GroupProperty::name).reduce("", (a, b) -> a + ", " + b));
                                }
                            });
                });
    }
}
