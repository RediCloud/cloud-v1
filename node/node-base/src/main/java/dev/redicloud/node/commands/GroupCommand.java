package dev.redicloud.node.commands;

import dev.redicloud.commands.CommandHelp;
import dev.redicloud.commands.CommandSender;
import dev.redicloud.commands.ConsoleCommand;
import dev.redicloud.commands.annotation.*;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.group.GroupProperty;
import dev.redicloud.api.group.ICloudGroup;
import dev.redicloud.api.impl.group.CloudGroup;
import dev.redicloud.api.service.ICloudService;
import dev.redicloud.api.service.ServiceEnvironment;
import dev.redicloud.commons.ConditionChecks;
import dev.redicloud.commons.function.future.FutureActionCollection;
import dev.redicloud.node.console.setup.SetupControlState;
import dev.redicloud.node.setup.group.GroupSetup;
import dev.redicloud.node.setup.group.MinecraftSetup;
import dev.redicloud.node.setup.group.ProxySetup;

import java.util.Arrays;
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
    @CommandCompletion("@groups @service_templates")
    public void onTemplateAdd(CommandSender commandSender, String groupName, String templateName){
        CloudAPI.getInstance().getConsole().trace("Checking existence of group " + groupName + "...");
        CloudAPI.getInstance().getGroupManager().existsGroupAsync(groupName)
                .onFailure(e -> CloudAPI.getInstance().getConsole().error("Error while checking existence of group " + groupName, e))
                .onSuccess(existsGroup -> {
                    if(!existsGroup){
                        commandSender.sendMessage("Group doesn't exist");
                        return;
                    }
                    CloudAPI.getInstance().getGroupManager().getGroupAsync(groupName)
                            .onFailure(e -> CloudAPI.getInstance().getConsole().error("Error while getting group " + groupName, e))
                            .onSuccess(group -> {
                                if(group.getTemplateNames().contains(templateName)){
                                    commandSender.sendMessage("Template is already added to this group!");
                                    return;
                                }
                                CloudAPI.getInstance().getServiceTemplateManager().existsTemplateAsync(templateName)
                                        .onFailure(e -> CloudAPI.getInstance().getConsole().error("Error while checking existence of template " + templateName, e))
                                        .onSuccess(existsTemplate -> {
                                            if(!existsTemplate){
                                                commandSender.sendMessage("Template doesn't exist");
                                                return;
                                            }
                                            CloudAPI.getInstance().getServiceTemplateManager().getTemplateAsync(templateName)
                                                    .onFailure(e -> CloudAPI.getInstance().getConsole().error("Error while getting template " + templateName, e))
                                                    .onSuccess(template -> {
                                                        group.addTemplate(template);
                                                        group.updateAsync();
                                                        commandSender.sendMessage("Template added to group");
                                                    });
                                        });
                            });
                });
    }

    @Subcommand("template remove")
    @Syntax("<Group> <Template>")
    @Description("Remove a template to a group")
    @CommandCompletion("@groups @service_templates")
    public void onTemplateRemove(CommandSender commandSender, String groupName, String templateName){
        CloudAPI.getInstance().getConsole().trace("Checking existence of group " + groupName + "...");
        CloudAPI.getInstance().getGroupManager().existsGroupAsync(groupName)
                .onFailure(e -> CloudAPI.getInstance().getConsole().error("Error while checking existence of group " + groupName, e))
                .onSuccess(existsGroup -> {
                    if(!existsGroup){
                        commandSender.sendMessage("Group doesn't exist");
                        return;
                    }
                    CloudAPI.getInstance().getGroupManager().getGroupAsync(groupName)
                            .onFailure(e -> CloudAPI.getInstance().getConsole().error("Error while getting group " + groupName, e))
                            .onSuccess(group -> {
                                if(!group.getTemplateNames().contains(templateName)){
                                    commandSender.sendMessage("Template is not added to this group!");
                                    return;
                                }
                                CloudAPI.getInstance().getServiceTemplateManager().existsTemplateAsync(templateName)
                                        .onFailure(e -> CloudAPI.getInstance().getConsole().error("Error while checking existence of template " + templateName, e))
                                        .onSuccess(existsTemplate -> {
                                            if(!existsTemplate){
                                                commandSender.sendMessage("Template doesn't exist");
                                                return;
                                            }
                                            CloudAPI.getInstance().getServiceTemplateManager().getTemplateAsync(templateName)
                                                    .onFailure(e -> CloudAPI.getInstance().getConsole().error("Error while getting template " + templateName, e))
                                                    .onSuccess(template -> {
                                                        group.removeTemplate(template);
                                                        group.updateAsync();
                                                        commandSender.sendMessage("Template added to group");
                                                    });
                                        });
                            });
                });
    }

    @Subcommand("info")
    @Description("Show information about a group")
    @Syntax("<Name>")
    @CommandCompletion("@groups")
    public void onInfo(CommandSender commandSender, String groupName) {
        CloudAPI.getInstance().getConsole().trace("Checking existence of group " + groupName + "... ");
        CloudAPI.getInstance().getGroupManager().existsGroupAsync(groupName)
                .onFailure(t -> CloudAPI.getInstance().getConsole().error("Failed to get group info", t))
                .onSuccess(exists -> {
                    if (!exists) {
                        commandSender.sendMessage("Group " + groupName + " doesn't exist");
                        return;
                    }

                    CloudAPI.getInstance().getGroupManager().getGroupAsync(groupName)
                            .onFailure(t -> CloudAPI.getInstance().getConsole().error("Failed to get group info", t))
                            .onSuccess(group -> {
                                group.getConnectedServices()
                                        .onFailure(t -> CloudAPI.getInstance().getConsole().error("Failed to get group info", t))
                                        .onSuccess(services -> {
                                            StringBuilder builder = new StringBuilder();
                                            if (services.isEmpty()) builder.append("No services");
                                            for (ICloudService service : services) {
                                                if (!builder.toString().isEmpty()) builder.append("§8, ");
                                                builder.append("%hc");
                                                builder.append(service.getServiceName());
                                            }
                                            commandSender.sendMessage("%tcGroup info of %hc" + group.getName() + "§8:");
                                            commandSender.sendMessage("§8 »%tc JVM-Flags: %hc" + Arrays.toString(group.getJvmArguments()));
                                            commandSender.sendMessage("§8 »%tc Process-Arguments: %hc" + Arrays.toString(group.getProcessParameters()));
                                            commandSender.sendMessage("§8 »%tc Environment: %hc" + group.getServiceEnvironment().name());
                                            commandSender.sendMessage("§8 »%tc Services: %hc" + builder.toString());
                                            commandSender.sendMessage("§8 »%tc Min. Services: %hc" + group.getMinServices());
                                            commandSender.sendMessage("§8 »%tc Max. Services: %hc" + group.getMaxServices());
                                            commandSender.sendMessage("§8 »%tc Service-Version: %hc" + group.getServiceVersionName());
                                            commandSender.sendMessage("§8 »%tc Maintenance: %hc" + group.isMaintenance());
                                            StringBuilder templateBuilder = new StringBuilder();
                                            for (String templateName : group.getTemplateNames()) {
                                                if (!templateBuilder.toString().isEmpty())
                                                    templateBuilder.append("§8, %hc");
                                                templateBuilder.append("%hc");
                                                templateBuilder.append(templateName);
                                            }
                                            commandSender.sendMessage("§8 »%tc Templates: %hc" + templateBuilder);
                                            if(group.getServiceEnvironment() == ServiceEnvironment.MINECRAFT)
                                                commandSender.sendMessage("§8 »%tc Fallback: %hc" + group.isFallback());
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
                        new GroupSetup().start(((groupSetup, groupSetupControlState) -> {
                            if (groupSetupControlState == SetupControlState.FINISHED) {
                                CloudAPI.getInstance().getServiceVersionManager().getServiceVersionsAsync()
                                        .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to create group!", e))
                                        .onSuccess(versionHolders -> {
                                            switch (groupSetup.getEnvironment()){
                                                case MINECRAFT:
                                                    new MinecraftSetup().start((mineCraftSetup, mineCraftSetupControlState) -> {
                                                        if (mineCraftSetupControlState == SetupControlState.FINISHED) {
                                                            CloudGroup cloudGroup = new CloudGroup();
                                                            cloudGroup.setUniqueId(UUID.randomUUID());
                                                            cloudGroup.setName(name.replaceAll(" ", ""));
                                                            cloudGroup.setPercentToStartNewService(mineCraftSetup.getPercentToStartNewService());
                                                            cloudGroup.setStartPort(49152);
                                                            cloudGroup.setMinServices(mineCraftSetup.getMinServices());
                                                            cloudGroup.setMaxServices(mineCraftSetup.getMaxServices());
                                                            cloudGroup.setStaticGroup(mineCraftSetup.isStaticGroup());
                                                            cloudGroup.setMaintenance(true);
                                                            cloudGroup.setFallback(mineCraftSetup.isFallback());
                                                            cloudGroup.setMaxMemory(mineCraftSetup.getMaxMemory());
                                                            cloudGroup.setStartPriority(mineCraftSetup.getStartPriority());
                                                            cloudGroup.setServiceVersionName(mineCraftSetup.getServiceVersionName().replaceAll(" ", ""));
                                                            cloudGroup.setServiceEnvironment(ServiceEnvironment.MINECRAFT);

                                                            CloudAPI.getInstance().getGroupManager().createGroupAsync(cloudGroup)
                                                                    .onFailure(e2 -> CloudAPI.getInstance().getConsole().error("Failed to create group " + name, e2))
                                                                    .onSuccess(holder -> {
                                                                        CloudAPI.getInstance().getGroupManager().addDefaultTemplates(cloudGroup);
                                                                        commandSender.sendMessage("Group %hc" + name + "%tc created");
                                                                    });
                                                        } else if (mineCraftSetupControlState == SetupControlState.CANCELLED) {
                                                            commandSender.sendMessage("§cMinecraft Group creation cancelled");
                                                        }
                                                    });
                                                    break;
                                                case VELOCITY:
                                                case BUNGEECORD:
                                                    new ProxySetup().start((proxySetup, proxySetupControlState) -> {
                                                        if (proxySetupControlState == SetupControlState.FINISHED) {

                                                            CloudGroup cloudGroup = new CloudGroup();
                                                            cloudGroup.setUniqueId(UUID.randomUUID());
                                                            cloudGroup.setName(name.replaceAll(" ", ""));
                                                            cloudGroup.setPercentToStartNewService(proxySetup.getPercentToStartNewService());
                                                            cloudGroup.setStartPort(proxySetup.getStartPort());
                                                            cloudGroup.setMinServices(proxySetup.getMinServices());
                                                            cloudGroup.setMaxServices(proxySetup.getMaxServices());
                                                            cloudGroup.setStaticGroup(proxySetup.isStaticGroup());
                                                            cloudGroup.setMaintenance(true);
                                                            cloudGroup.setFallback(false);
                                                            cloudGroup.setMaxMemory(proxySetup.getMaxMemory());
                                                            cloudGroup.setStartPriority(proxySetup.getStartPriority());
                                                            cloudGroup.setServiceVersionName(proxySetup.getServiceVersionName().replaceAll(" ", ""));
                                                            cloudGroup.setServiceEnvironment(groupSetup.getEnvironment());

                                                            CloudAPI.getInstance().getGroupManager().createGroupAsync(cloudGroup)
                                                                    .onFailure(e2 -> commandSender.sendMessage("§cFailed to create group " + name))
                                                                    .onSuccess(holder -> commandSender.sendMessage("Group %hc" + name + "%tc created"));
                                                        } else if (proxySetupControlState == SetupControlState.CANCELLED) {
                                                            commandSender.sendMessage("§cProxy Group creation cancelled");
                                                        }
                                                    });
                                                    break;
                                                case LIMBO:
                                                    //TODO: Create Limbo setup
                                                    commandSender.sendMessage("Its currently not possible to create a Limbo Group");
                                                    break;
                                            }
                                        });
                            } else if (groupSetupControlState == SetupControlState.CANCELLED) {
                                commandSender.sendMessage("§cGroup creation cancelled");
                            }
                        }));
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
                        commandSender.sendMessage("Group %hc" + name + "%tc doesn't exist");
                    } else {
                        CloudAPI.getInstance().getGroupManager().getGroupAsync(name)
                                .onFailure(e2 -> commandSender.sendMessage("§cFailed to delete group " + name))
                                .onSuccess(holder -> {
                                    CloudAPI.getInstance().getGroupManager().deleteGroupAsync(holder.getUniqueId())
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
                    for (ICloudGroup holder : holders) {
                        futureActionCollection.addToProcess(holder.getUniqueId(), holder.getOnlineServiceCount());
                    }
                    CloudAPI.getInstance().getConsole().trace("Processing online count task");
                    futureActionCollection.process()
                            .onFailure(e2 -> commandSender.sendMessage("§cFailed to get groups"))
                            .onSuccess(map -> {
                                commandSender.sendMessage("");
                                commandSender.sendMessage("Groups §8(%hc" + holders.size() + "§8):");
                                for (ICloudGroup group : holders) {
                                    commandSender.sendMessage("   " + group.getName() + " §7(" + map.get(group.getUniqueId()) + "/" + group.getMaxServices() + ")");
                                }
                                commandSender.sendMessage("");
                            });
                });
    }

    @Subcommand("edit")
    @Syntax("<Group> <Property> <Value>")
    @CommandCompletion("@groups @group_properties")
    @Description("Edit a group property")
    public void onEdit(CommandSender commandSender, String name, String key, String value) {
        CloudAPI.getInstance().getGroupManager().existsGroupAsync(name)
                .onFailure(e -> commandSender.sendMessage("§cFailed to edit group " + name))
                .onSuccess(exists -> {
                    if (!exists) {
                        commandSender.sendMessage("Group %hc" + name + "%tc doesn't exist");
                        return;
                    }
                    CloudAPI.getInstance().getGroupManager().getGroupAsync(name)
                            .onFailure(e2 -> commandSender.sendMessage("§cFailed to edit group " + name))
                            .onSuccess(group -> {
                                try {
                                    switch (key.toUpperCase()) {
                                        case "START_PORT":
                                            if (!ConditionChecks.isInteger(value)) {
                                                commandSender.sendMessage("Value must be an integer");
                                                return;
                                            }
                                            int intValue = Integer.parseInt(value);
                                            if(intValue < 0 || intValue > 65535) {
                                                commandSender.sendMessage("Value must be between 0 and 65535");
                                                return;
                                            }
                                            commandSender.sendMessage("Setting start port to " + intValue);
                                            break;
                                        case "PERCENT_TO_START_NEW_SERVICE":
                                            if (!ConditionChecks.isInteger(value)) {
                                                commandSender.sendMessage("Value must be an integer");
                                                return;
                                            }
                                            intValue = Integer.parseInt(value);
                                            if(intValue != -1 && (intValue < 0 || intValue > 100)) {
                                                commandSender.sendMessage("Value must be greater than 400");
                                                return;
                                            }
                                            group.setPercentToStartNewService(intValue);
                                            group.updateAsync();
                                            commandSender.sendMessage("Percent to start new service set to " + intValue);
                                            break;
                                        case "MAX_MEMORY":
                                        case "MEMORY":
                                            if (!ConditionChecks.isInteger(value)) {
                                                commandSender.sendMessage("Value must be an integer");
                                                return;
                                            }
                                            intValue = Integer.parseInt(value);
                                            if (intValue < 400) {
                                                commandSender.sendMessage("Value must be greater than 400");
                                                return;
                                            }
                                            group.setMaxMemory(intValue);
                                            group.updateAsync();
                                            commandSender.sendMessage("Group %hc" + name + "%tc max memory set to %hc" + intValue);
                                            break;
                                        case "FALLBACK":
                                        case "LOBBY_SERVICE":
                                            if (!ConditionChecks.isBoolean(value)) {
                                                commandSender.sendMessage("Value must be a boolean");
                                                return;
                                            }
                                            boolean boolValue = Boolean.parseBoolean(value);
                                            group.setFallback(boolValue);
                                            group.updateAsync();
                                            commandSender.sendMessage("Group %hc" + name + "%tc fallback set to %hc" + boolValue);
                                            break;
                                        case "MAINTENANCE":
                                            if (!ConditionChecks.isBoolean(value)) {
                                                commandSender.sendMessage("Value must be a boolean");
                                                return;
                                            }
                                            boolValue = Boolean.parseBoolean(value);
                                            group.setMaintenance(boolValue);
                                            group.updateAsync();
                                            group.getServices()
                                                    .onFailure(e3 -> CloudAPI.getInstance().getConsole().error("Failed to get services of group " + group.getName(), e3))
                                                    .onSuccess(services -> {
                                                        for (ICloudService service : services) {
                                                            service.setMaintenance(boolValue);
                                                            service.updateAsync();
                                                        }
                                                        commandSender.sendMessage("Group %hc" + name + "%tc maintenance set to %hc" + boolValue);
                                                    });
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
                                            group.setMaxServices(intValue);
                                            group.updateAsync();
                                            commandSender.sendMessage("Group %hc" + name + "%tc max services set to %hc" + intValue);
                                            break;
                                        case "MIN_SERVICES":
                                        case "MIN_SERVICE":
                                            if (!ConditionChecks.isInteger(value)) {
                                                commandSender.sendMessage("Value must be an integer");
                                                return;
                                            }
                                            intValue = Integer.parseInt(value);
                                            if (intValue < 0) {
                                                commandSender.sendMessage("Value must be greater than 0 or 0");
                                                return;
                                            }
                                            group.setMinServices(intValue);
                                            group.updateAsync();
                                            commandSender.sendMessage("Group %hc" + name + "%tc min services set to %hc" + intValue);
                                            break;
                                        case "START_PRIORITY":
                                            if (!ConditionChecks.isInteger(value)) {
                                                commandSender.sendMessage("Value must be an integer");
                                                return;
                                            }
                                            intValue = Integer.parseInt(value);
                                            group.setStartPriority(intValue);
                                            group.updateAsync();
                                            commandSender.sendMessage("Group %hc" + name + "%tc start priority set to %hc" + intValue);
                                            break;
                                        case "SERVICE_VERSION":
                                            CloudAPI.getInstance().getServiceVersionManager().existsServiceVersionAsync(value)
                                                .onFailure(e3 -> CloudAPI.getInstance().getConsole().error("Failed to check existence of service version " + value, e3))
                                                .onSuccess(existsVersion -> {
                                                    if(!existsVersion) {
                                                        commandSender.sendMessage("Service version %hc" + value + "%tc doesn't exist");
                                                        return;
                                                    }
                                                    CloudAPI.getInstance().getServiceVersionManager().getServiceVersionAsync(value)
                                                        .onFailure(e4 -> CloudAPI.getInstance().getConsole().error("Failed to get service version " + value, e4))
                                                        .onSuccess(serviceVersion -> {
                                                            if(serviceVersion.getEnvironmentType() != group.getServiceEnvironment()){
                                                                commandSender.sendMessage("Service version %hc" + value + "%tc is not compatible with group %hc" + name + "%tc");
                                                                return;
                                                            }
                                                            group.setServiceVersion(serviceVersion);
                                                            group.updateAsync();
                                                            commandSender.sendMessage("Group %hc" + name + "%tc service version set to %hc" + value);
                                                        });
                                                });
                                            break;
                                        case "STATIC":
                                            if(!ConditionChecks.isBoolean(value)){
                                                commandSender.sendMessage("Value must be a boolean");
                                                return;
                                            }
                                            group.getOnlineServiceCount()
                                                    .onFailure(e3 -> CloudAPI.getInstance().getConsole().error("Failed to edit group " + name, e3))
                                                    .onSuccess(count -> {
                                                        if (count > 0) {
                                                            commandSender.sendMessage("§cCannot edit static property of group %hc" + name + "%tc while it has a connected services");
                                                            return;
                                                        }
                                                        group.setStatic(Boolean.parseBoolean(value));
                                                        group.updateAsync();
                                                        commandSender.sendMessage("Group %hc" + name + "%tc static set to %hc" + Boolean.parseBoolean(value));
                                                    });
                                            break;
                                        default:
                                            StringBuilder builder = new StringBuilder();
                                            for (GroupProperty property : GroupProperty.values()) {
                                                if (!builder.toString().isEmpty()) builder.append("§8, ");
                                                builder.append("%hc");
                                                builder.append(property.name());
                                            }
                                            commandSender.sendMessage("§cInvalid property! Properties: " + builder);
                                            break;
                                    }
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
