package net.suqatri.redicloud.node.commands;

import net.suqatri.commands.CommandHelp;
import net.suqatri.commands.CommandSender;
import net.suqatri.commands.ConsoleCommand;
import net.suqatri.commands.annotation.*;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.api.service.ServiceState;
import net.suqatri.redicloud.api.service.configuration.IServiceStartConfiguration;
import net.suqatri.redicloud.commons.ConditionChecks;
import net.suqatri.redicloud.commons.function.future.FutureActionCollection;
import net.suqatri.redicloud.node.NodeLauncher;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@CommandAlias("ser|service|services")
public class ServiceCommand extends ConsoleCommand {

    @HelpCommand
    @Subcommand("help")
    @Default
    @Syntax("[Page]")
    public void onHelp(CommandHelp commandHelp) {
        commandHelp.showHelp();
    }

    @Subcommand("start")
    @Description("Start a amount of services")
    @Syntax("<Gruppe> [Amount]")
    @CommandCompletion("@groups")
    public void onStart(CommandSender commandSender, String name, @Optional String amountString) {
        AtomicInteger amount = new AtomicInteger(1);
        if (amountString != null) {
            if (!ConditionChecks.isInteger(amountString)) {
                commandSender.sendMessage("Amount must be an integer");
                return;
            } else {
                int i = Integer.parseInt(amountString);
                if (i < 1) {
                    commandSender.sendMessage("Amount must be greater than 0");
                    return;
                }
                amount.set(i);
            }
        }
        commandSender.sendMessage("Starting " + amount + " services...");
        CloudAPI.getInstance().getGroupManager().getGroupAsync(name)
                .onFailure(t -> CloudAPI.getInstance().getConsole().error("Failed to get groups", t))
                .onSuccess(groupHolder -> {
                    CloudAPI.getInstance().getConsole().trace("Got group");
                    String groupName = groupHolder.getName();
                    FutureActionCollection<IServiceStartConfiguration, ICloudService> futureActionCollection = new FutureActionCollection();
                    for (int i = 0; i < amount.get(); i++) {
                        IServiceStartConfiguration configuration = groupHolder.createServiceConfiguration();
                        futureActionCollection.addToProcess(configuration, CloudAPI.getInstance().getServiceFactory().queueService(configuration));
                    }
                    CloudAPI.getInstance().getConsole().trace("Processing services");
                    futureActionCollection.process()
                            .onFailure(t -> CloudAPI.getInstance().getConsole().error("Failed to start services", t))
                            .onSuccess(services -> commandSender.sendMessage("Started " + amount + " services of group " + groupName));
                });
    }

    @Subcommand("stop")
    @Description("Stop a service")
    @Syntax("<Service> --force")
    @CommandCompletion("@running_services --force")
    public void onStop(CommandSender commandSender, String serviceName, @Optional String arg) {
        boolean force = arg != null && arg.equalsIgnoreCase("--force");
        if (serviceName.endsWith("-*")) {
            CloudAPI.getInstance().getGroupManager().getGroupAsync(serviceName.split("-")[0])
                    .onFailure(t -> CloudAPI.getInstance().getConsole().error("Failed to get group", t))
                    .onSuccess(groupHolder -> {
                        if (groupHolder == null) {
                            commandSender.sendMessage("Group not found");
                            return;
                        }
                        commandSender.sendMessage((force ? "Force stopping" : "Stopping") + " all service of group " + groupHolder.getName() + "...");
                        groupHolder.getConnectedServices()
                                .onFailure(t -> CloudAPI.getInstance().getConsole().error("Failed to get services", t))
                                .onSuccess(services -> {
                                    FutureActionCollection<IServiceStartConfiguration, Boolean> futureActionCollection = new FutureActionCollection();
                                    for (ICloudService serviceHolder : services) {
                                        futureActionCollection.addToProcess(serviceHolder.getConfiguration(), CloudAPI.getInstance().getServiceManager().stopServiceAsync(serviceHolder.getUniqueId(), force));
                                    }
                                    futureActionCollection.process()
                                            .onFailure(t -> CloudAPI.getInstance().getConsole().error("Failed to stop services", t))
                                            .onSuccess(b -> commandSender.sendMessage("Stopped all services of group " + groupHolder.getName()));
                                });
                    });
        } else {
            CloudAPI.getInstance().getServiceManager().getServiceAsync(serviceName)
                    .onFailure(t -> CloudAPI.getInstance().getConsole().error("Failed to get service", t))
                    .onSuccess(serviceHolder -> {
                        if (serviceHolder == null) {
                            commandSender.sendMessage("Service not found");
                            return;
                        }
                        commandSender.sendMessage((force ? "Force stopping" : "Stopping") + " service " + serviceHolder.getServiceName() + "...");
                        NodeLauncher.getInstance().getServiceManager().stopServiceAsync(serviceHolder.getUniqueId(), force)
                                .onFailure(t -> CloudAPI.getInstance().getConsole().error("Failed to stop service", t))
                                .onSuccess(b -> commandSender.sendMessage("Stopped service " + serviceHolder.getServiceName()));
                    });
        }
    }

    @Subcommand("list")
    @Description("List all services")
    public void onList(CommandSender commandSender) {
        CloudAPI.getInstance().getConsole().trace("Getting services...");
        CloudAPI.getInstance().getServiceManager().getServicesAsync()
                .onFailure(t -> CloudAPI.getInstance().getConsole().error("Failed to get services", t))
                .onSuccess(services -> {
                    if (services.isEmpty()) {
                        commandSender.sendMessage("No services found");
                        return;
                    }
                    List<String> lines = new ArrayList<>();
                    int amount = 0;
                    for (ICloudService serviceHolder : services) {
                        if(serviceHolder.getServiceState() == ServiceState.OFFLINE) continue;
                        amount++;
                        lines.add("§8 » %tc" + serviceHolder.getServiceName() + " §8| %hc" + serviceHolder.getOnlineCount() + "§8/%tc" + serviceHolder.getMaxPlayers());
                    }

                    commandSender.sendMessage("");
                    commandSender.sendMessage("%tcServices §8(%hc" + amount + "§8)%tc:");
                    for (String s : lines.parallelStream().sorted().collect(Collectors.toList())) {
                        commandSender.sendMessage(s);
                    }
                    commandSender.sendMessage("");
                });
    }

    @Subcommand("info")
    @Description("Get info about a service")
    @Syntax("<Service>")
    @CommandCompletion("@services")
    public void onInfo(CommandSender commandSender, String serviceName) {
        CloudAPI.getInstance().getConsole().trace("Checking service existence...");
        CloudAPI.getInstance().getServiceManager().existsServiceAsync(serviceName)
                .onFailure(t -> CloudAPI.getInstance().getConsole().error("Failed to get service", t))
                .onSuccess(exists -> {
                    if (!exists) {
                        commandSender.sendMessage("Service not found");
                        return;
                    }
                    CloudAPI.getInstance().getServiceManager().getServiceAsync(serviceName)
                            .onFailure(t -> CloudAPI.getInstance().getConsole().error("Failed to get service", t))
                            .onSuccess(serviceHolder -> {
                                serviceHolder.getServiceVersion()
                                        .onFailure(t -> CloudAPI.getInstance().getConsole().error("Failed to get service version", t))
                                        .onSuccess(versionHolder -> {
                                            CloudAPI.getInstance().getNodeManager().getNodeAsync(serviceHolder.getNodeId())
                                                .onFailure(t -> CloudAPI.getInstance().getConsole().error("Failed to get node", t))
                                                .onSuccess(nodeHolder -> {
                                                    commandSender.sendMessage("");
                                                    commandSender.sendMessage("%tcService info of %hc" + serviceHolder.getServiceName() + "§8:");
                                                    commandSender.sendMessage("§8 »%tc Environment: %hc" + serviceHolder.getEnvironment().name());
                                                    commandSender.sendMessage("§8 »%tc Group: %hc" + (serviceHolder.isGroupBased() ? serviceHolder.getGroupName() : "None"));
                                                    commandSender.sendMessage("§8 »%tc Version: %hc" + versionHolder.getName());
                                                    commandSender.sendMessage("§8 »%tc State: %hc" + serviceHolder.getServiceState().name());
                                                    commandSender.sendMessage("§8 »%tc Players: %hc" + serviceHolder.getOnlineCount() + "§8/%hc" + serviceHolder.getMaxPlayers());
                                                    commandSender.sendMessage("§8 »%tc RAM: %hc" + serviceHolder.getMaxRam() + " MB");
                                                    commandSender.sendMessage("§8 »%tc External: %hc" + serviceHolder.isExternal());
                                                    commandSender.sendMessage("§8 »%tc Node: %hc" + nodeHolder.getName());
                                                    commandSender.sendMessage("§8 »%tc Maintenance: %hc" + serviceHolder.isMaintenance());
                                                    StringBuilder templateBuilder = new StringBuilder();
                                                    for (String templateName : serviceHolder.getConfiguration().getTemplateNames()) {
                                                        if (!templateBuilder.toString().isEmpty())
                                                            templateBuilder.append("§8, %hc");
                                                        templateBuilder.append("%hc");
                                                        templateBuilder.append(templateName);
                                                    }
                                                    commandSender.sendMessage("§8 »%tc Templates: %hc" + templateBuilder);
                                                    commandSender.sendMessage("");
                                                });
                                        });
                            });
                });
    }

}
