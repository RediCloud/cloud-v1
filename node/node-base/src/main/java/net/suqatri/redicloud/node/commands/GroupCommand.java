package net.suqatri.redicloud.node.commands;

import net.suqatri.commands.CommandHelp;
import net.suqatri.commands.CommandSender;
import net.suqatri.commands.ConsoleCommand;
import net.suqatri.commands.annotation.*;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.group.GroupProperty;
import net.suqatri.redicloud.api.group.ICloudGroup;
import net.suqatri.redicloud.api.impl.group.CloudGroup;
import net.suqatri.redicloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.api.service.ServiceEnvironment;
import net.suqatri.redicloud.commons.ConditionChecks;
import net.suqatri.redicloud.commons.function.future.FutureActionCollection;
import net.suqatri.redicloud.node.console.setup.SetupControlState;
import net.suqatri.redicloud.node.setup.group.GroupSetup;

import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;

@CommandAlias("group|groups")
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
    public void onHelp(CommandHelp commandHelp) {
        commandHelp.showHelp();
    }

    @Subcommand("template add")
    @Syntax("<Group> <Template>")
    @Description("Add a template to a group")
    public void onTemplateAdd(CommandSender commandSender, String groupName, String templateName){
        CloudAPI.getInstance().getConsole().trace("Checking existence of group " + groupName + "...");
        CloudAPI.getInstance().getGroupManager().existsGroupAsync(groupName)
                .onFailure(e -> CloudAPI.getInstance().getConsole().error("Error while checking existence of group " + groupName, e))
                .onSuccess(existsGroup -> {
                    if(!existsGroup){
                        commandSender.sendMessage("Group does not exist");
                        return;
                    }
                    CloudAPI.getInstance().getGroupManager().getGroupAsync(groupName)
                            .onFailure(e -> CloudAPI.getInstance().getConsole().error("Error while getting group " + groupName, e))
                            .onSuccess(groupHolder -> {
                                if(groupHolder.get().getTemplateNames().contains(templateName)){
                                    commandSender.sendMessage("Template is already added to this group!");
                                    return;
                                }
                                CloudAPI.getInstance().getServiceTemplateManager().existsTemplateAsync(templateName)
                                        .onFailure(e -> CloudAPI.getInstance().getConsole().error("Error while checking existence of template " + templateName, e))
                                        .onSuccess(existsTemplate -> {
                                            if(!existsTemplate){
                                                commandSender.sendMessage("Template does not exist");
                                                return;
                                            }
                                            CloudAPI.getInstance().getServiceTemplateManager().getTemplateAsync(templateName)
                                                    .onFailure(e -> CloudAPI.getInstance().getConsole().error("Error while getting template " + templateName, e))
                                                    .onSuccess(templateHolder -> {
                                                        groupHolder.get().addTemplate(templateHolder);
                                                        groupHolder.get().updateAsync();
                                                        commandSender.sendMessage("Template added to group");
                                                    });
                                        });
                            });
                });
    }

    @Subcommand("template remove")
    @Syntax("<Group> <Template>")
    @Description("Remove a template to a group")
    public void onTemplateRemove(CommandSender commandSender, String groupName, String templateName){
        CloudAPI.getInstance().getConsole().trace("Checking existence of group " + groupName + "...");
        CloudAPI.getInstance().getGroupManager().existsGroupAsync(groupName)
                .onFailure(e -> CloudAPI.getInstance().getConsole().error("Error while checking existence of group " + groupName, e))
                .onSuccess(existsGroup -> {
                    if(!existsGroup){
                        commandSender.sendMessage("Group does not exist");
                        return;
                    }
                    CloudAPI.getInstance().getGroupManager().getGroupAsync(groupName)
                            .onFailure(e -> CloudAPI.getInstance().getConsole().error("Error while getting group " + groupName, e))
                            .onSuccess(groupHolder -> {
                                if(!groupHolder.get().getTemplateNames().contains(templateName)){
                                    commandSender.sendMessage("Template is not added to this group!");
                                    return;
                                }
                                CloudAPI.getInstance().getServiceTemplateManager().existsTemplateAsync(templateName)
                                        .onFailure(e -> CloudAPI.getInstance().getConsole().error("Error while checking existence of template " + templateName, e))
                                        .onSuccess(existsTemplate -> {
                                            if(!existsTemplate){
                                                commandSender.sendMessage("Template does not exist");
                                                return;
                                            }
                                            CloudAPI.getInstance().getServiceTemplateManager().getTemplateAsync(templateName)
                                                    .onFailure(e -> CloudAPI.getInstance().getConsole().error("Error while getting template " + templateName, e))
                                                    .onSuccess(templateHolder -> {
                                                        groupHolder.get().removeTemplate(templateHolder);
                                                        groupHolder.get().updateAsync();
                                                        commandSender.sendMessage("Template added to group");
                                                    });
                                        });
                            });
                });
    }

    @Subcommand("info")
    @Description("Show information about a group")
    @Syntax("<Name>")
    public void onInfo(CommandSender commandSender, String groupName) {
        CloudAPI.getInstance().getConsole().trace("Checking existence of group " + groupName + "... ");
        CloudAPI.getInstance().getGroupManager().existsGroupAsync(groupName)
                .onFailure(t -> CloudAPI.getInstance().getConsole().error("Failed to get group info", t))
                .onSuccess(exists -> {
                    if (!exists) {
                        commandSender.sendMessage("Group " + groupName + " does not exist");
                        return;
                    }

                    CloudAPI.getInstance().getGroupManager().getGroupAsync(groupName)
                            .onFailure(t -> CloudAPI.getInstance().getConsole().error("Failed to get group info", t))
                            .onSuccess(groupHolder -> {
                                groupHolder.get().getOnlineServices()
                                        .onFailure(t -> CloudAPI.getInstance().getConsole().error("Failed to get group info", t))
                                        .onSuccess(services -> {
                                            StringBuilder builder = new StringBuilder();
                                            if (services.isEmpty()) builder.append("No services");
                                            for (IRBucketHolder<ICloudService> service : services) {
                                                if (!builder.toString().isEmpty()) builder.append("§8, ");
                                                builder.append("%hc");
                                                builder.append(service.get().getServiceName());
                                            }
                                            commandSender.sendMessage("%tcGroup info of %hc" + groupHolder.get().getName() + "§8:");
                                            commandSender.sendMessage("  JVM-Flags: %hc" + Arrays.toString(groupHolder.get().getJvmArguments()));
                                            commandSender.sendMessage("  Process-Arguments: %hc" + Arrays.toString(groupHolder.get().getProcessParameters()));
                                            commandSender.sendMessage("  Environment: %hc" + groupHolder.get().getServiceEnvironment().name());
                                            commandSender.sendMessage("  Services: %hc" + builder.toString());
                                            commandSender.sendMessage("  Min. Services: %hc" + groupHolder.get().getMinServices());
                                            commandSender.sendMessage("  Max. Services: %hc" + groupHolder.get().getMaxServices());
                                        });
                            });
                });
    }

    @Subcommand("create")
    @Syntax("<Group>")
    @CommandCompletion("@groups")
    @Description("Create a new group")
    public void onCreate(CommandSender commandSender, String name) {
        CloudAPI.getInstance().getGroupManager().existsGroupAsync(name)
                .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to create group!", e))
                .onSuccess(exists -> {
                    if (exists) {
                        commandSender.sendMessage("§cGroup " + name + " already exists");
                    } else {
                        new GroupSetup().start((groupSetup, setupControlState) -> {
                            if (setupControlState == SetupControlState.FINISHED) {

                                CloudGroup cloudGroup = new CloudGroup();
                                cloudGroup.setUniqueId(UUID.randomUUID());
                                cloudGroup.setName(name);
                                cloudGroup.setStartPort(groupSetup.getEnvironment() == ServiceEnvironment.MINECRAFT ? 49152 : 25565);
                                cloudGroup.setMinServices(groupSetup.getMinServices());
                                cloudGroup.setMaxServices(groupSetup.getMaxServices());
                                cloudGroup.setStaticGroup(groupSetup.isStaticGroup());
                                cloudGroup.setMaintenance(true);
                                cloudGroup.setFallback(groupSetup.isFallback());
                                cloudGroup.setMaxMemory(groupSetup.getMaxMemory());
                                cloudGroup.setStartPriority(groupSetup.getStartPriority());
                                cloudGroup.setServiceVersionName(groupSetup.getServiceVersionName());
                                cloudGroup.setServiceEnvironment(groupSetup.getEnvironment());

                                CloudAPI.getInstance().getGroupManager().createGroupAsync(cloudGroup)
                                        .onFailure(e2 -> commandSender.sendMessage("§cFailed to create group " + name))
                                        .onSuccess(holder -> commandSender.sendMessage("Group %hc" + name + "%tc created"));
                            } else if (setupControlState == SetupControlState.CANCELLED) {
                                commandSender.sendMessage("§cGroup creation cancelled");
                            }
                        });
                    }
                });
    }

    @Subcommand("delete")
    @Syntax("<Group>")
    @CommandCompletion("@groups")
    @Description("Delete a group")
    public void onDelete(CommandSender commandSender, String name) {
        CloudAPI.getInstance().getGroupManager().existsGroupAsync(name)
                .onFailure(e -> commandSender.sendMessage("§cFailed to delete group " + name))
                .onSuccess(exists -> {
                    if (!exists) {
                        commandSender.sendMessage("Group %hc" + name + "%tc does not exist");
                    } else {
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
    public void onList(CommandSender commandSender) {
        CloudAPI.getInstance().getConsole().trace("Gettings groups...");
        CloudAPI.getInstance().getGroupManager().getGroupsAsync()
                .onFailure(e -> commandSender.sendMessage("§cFailed to get groups"))
                .onSuccess(holders -> {
                    if (holders.isEmpty()) {
                        commandSender.sendMessage("No groups found!");
                        return;
                    }
                    FutureActionCollection<UUID, Integer> futureActionCollection = new FutureActionCollection<>();
                    for (IRBucketHolder<ICloudGroup> holder : holders) {
                        futureActionCollection.addToProcess(holder.get().getUniqueId(), holder.get().getOnlineServiceCount());
                    }
                    CloudAPI.getInstance().getConsole().trace("Processing online count task");
                    futureActionCollection.process()
                            .onFailure(e2 -> commandSender.sendMessage("§cFailed to get groups"))
                            .onSuccess(map -> {
                                commandSender.sendMessage("");
                                commandSender.sendMessage("Groups §8(%hc" + holders.size() + "§8):");
                                for (IRBucketHolder<ICloudGroup> holder : holders) {
                                    ICloudGroup group = holder.get();
                                    commandSender.sendMessage("   " + group.getName() + " §7(" + map.get(group.getUniqueId()) + "/" + group.getMaxServices() + ")");
                                }
                                commandSender.sendMessage("");
                            });
                });
    }

    @Subcommand("edit")
    @Syntax("<Group> <Key> <Value>")
    @CommandCompletion("@groups @group_keys @group_values")
    @Description("Edit a group property")
    public void onEdit(CommandSender commandSender, String name, String key, String value) {
        CloudAPI.getInstance().getGroupManager().existsGroupAsync(name)
                .onFailure(e -> commandSender.sendMessage("§cFailed to edit group " + name))
                .onSuccess(exists -> {
                    if (!exists) {
                        commandSender.sendMessage("Group %hc" + name + "%tc does not exist");
                        return;
                    }
                    CloudAPI.getInstance().getGroupManager().getGroupAsync(name)
                            .onFailure(e2 -> commandSender.sendMessage("§cFailed to edit group " + name))
                            .onSuccess(holder -> {
                                try {
                                    switch (value.toUpperCase()) {
                                        case "MAX_MEMORY":
                                        case "MEMORY":
                                            if (!ConditionChecks.isInteger(value)) {
                                                commandSender.sendMessage("Value must be an integer");
                                                return;
                                            }
                                            int intValue = Integer.parseInt(value);
                                            if (intValue < 400) {
                                                commandSender.sendMessage("Value must be greater than 400");
                                                return;
                                            }
                                            holder.get().setMaxMemory(intValue);
                                            commandSender.sendMessage("Group %hc" + name + "%tc max memory set to %hc" + intValue);
                                            break;
                                        case "FALLBACK":
                                        case "LOBBY_SERIVCE":
                                            if (!ConditionChecks.isBoolean(value)) {
                                                commandSender.sendMessage("Value must be a boolean");
                                                return;
                                            }
                                            boolean boolValue = Boolean.parseBoolean(value);
                                            holder.get().setFallback(boolValue);
                                            commandSender.sendMessage("Group %hc" + name + "%tc fallback set to %hc" + boolValue);
                                            break;
                                        case "MAINTENANCE":
                                            if (!ConditionChecks.isBoolean(value)) {
                                                commandSender.sendMessage("Value must be a boolean");
                                                return;
                                            }
                                            boolValue = Boolean.parseBoolean(value);
                                            holder.get().setMaintenance(boolValue);
                                            commandSender.sendMessage("Group %hc" + name + "%tc maintenance set to %hc" + boolValue);
                                            break;
                                        case "MAX_SERVICES":
                                        case "MAX_SERVICE":
                                            if (!ConditionChecks.isInteger(value)) {
                                                commandSender.sendMessage("Value must be an integer");
                                                return;
                                            }
                                            intValue = Integer.parseInt(value);
                                            if (intValue < -1) {
                                                commandSender.sendMessage("Value must be greater than -1");
                                                return;
                                            }
                                            holder.get().setMaxServices(intValue);
                                            commandSender.sendMessage("Group %hc" + name + "%tc max services set to %hc" + intValue);
                                            break;
                                        case "MIN_SERVICES":
                                        case "MIN_SERVICE":
                                            if (!ConditionChecks.isInteger(value)) {
                                                commandSender.sendMessage("Value must be an integer");
                                                return;
                                            }
                                            intValue = Integer.parseInt(value);
                                            if (intValue <= 0) {
                                                commandSender.sendMessage("Value must be greater than 0 or 0");
                                                return;
                                            }
                                            holder.get().setMinServices(intValue);
                                            commandSender.sendMessage("Group %hc" + name + "%tc min services set to %hc" + intValue);
                                            break;
                                        case "START_PRIORITY":
                                            if (!ConditionChecks.isInteger(value)) {
                                                commandSender.sendMessage("Value must be an integer");
                                                return;
                                            }
                                            intValue = Integer.parseInt(value);
                                            holder.get().setStartPriority(intValue);
                                            commandSender.sendMessage("Group %hc" + name + "%tc start priority set to %hc" + intValue);
                                            break;
                                        case "SERVICE_VERSION":
                                            CloudAPI.getInstance().getServiceVersionManager().existsServiceVersionAsync(value)
                                                .onFailure(e3 -> CloudAPI.getInstance().getConsole().error("Failed to check existence of service version " + value, e3))
                                                .onSuccess(existsVersion -> {
                                                    if(!existsVersion) {
                                                        commandSender.sendMessage("Service version %hc" + value + "%tc does not exist");
                                                        return;
                                                    }
                                                    CloudAPI.getInstance().getServiceVersionManager().getServiceVersionAsync(value)
                                                        .onFailure(e4 -> CloudAPI.getInstance().getConsole().error("Failed to get service version " + value, e4))
                                                        .onSuccess(serviceVersionHolder -> {
                                                            holder.get().setServiceVersion(serviceVersionHolder);
                                                            holder.get().updateAsync();
                                                            commandSender.sendMessage("Group %hc" + name + "%tc service version set to %hc" + value);
                                                        });
                                                });
                                            break;
                                        case "STATIC":
                                            if(!ConditionChecks.isBoolean(value)){
                                                commandSender.sendMessage("Value must be a boolean");
                                                return;
                                            }
                                            holder.get().getOnlineServiceCount()
                                                    .onFailure(e3 -> CloudAPI.getInstance().getConsole().error("Failed to edit group " + name, e3))
                                                    .onSuccess(count -> {
                                                        if (count > 0) {
                                                            commandSender.sendMessage("§cCannot edit static property of group %hc" + name + "%tc while it has a connected services");
                                                            return;
                                                        }
                                                        holder.get().setStatic(Boolean.parseBoolean(value));
                                                        holder.get().updateAsync();
                                                        commandSender.sendMessage("Group %hc" + name + "%tc static set to %hc" + Boolean.parseBoolean(value));
                                                    });
                                            break;
                                    }
                                    holder.get().updateAsync();
                                } catch (Exception e) {
                                    StringBuilder builder = new StringBuilder();
                                    for (GroupProperty property : GroupProperty.values()) {
                                        if (!builder.toString().isEmpty()) builder.append("§8, ");
                                        builder.append("%hc");
                                        builder.append(property.name());
                                    }
                                    commandSender.sendMessage("§cInvalid property! Properties: " + builder);
                                }
                            });
                });
    }
}
