package dev.redicloud.node.commands;

import dev.redicloud.commands.CommandHelp;
import dev.redicloud.commands.CommandSender;
import dev.redicloud.commands.ConsoleCommand;
import dev.redicloud.commands.annotation.*;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.service.ICloudService;
import dev.redicloud.api.service.ServiceState;
import dev.redicloud.api.service.configuration.IServiceStartConfiguration;
import dev.redicloud.commons.ConditionChecks;
import dev.redicloud.commons.function.future.FutureActionCollection;
import dev.redicloud.node.NodeLauncher;

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
                .onSuccess(group -> {
                    CloudAPI.getInstance().getConsole().trace("Got group");
                    String groupName = group.getName();
                    FutureActionCollection<IServiceStartConfiguration, ICloudService> futureActionCollection = new FutureActionCollection();
                    for (int i = 0; i < amount.get(); i++) {
                        IServiceStartConfiguration configuration = group.createServiceConfiguration();
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
                    .onSuccess(group -> {
                        if (group == null) {
                            commandSender.sendMessage("Group not found");
                            return;
                        }
                        commandSender.sendMessage((force ? "Force stopping" : "Stopping") + " all service of group " + group.getName() + "...");
                        group.getConnectedServices()
                                .onFailure(t -> CloudAPI.getInstance().getConsole().error("Failed to get services", t))
                                .onSuccess(services -> {
                                    FutureActionCollection<IServiceStartConfiguration, Boolean> futureActionCollection = new FutureActionCollection();
                                    for (ICloudService service : services) {
                                        futureActionCollection.addToProcess(service.getConfiguration(), CloudAPI.getInstance().getServiceManager().stopServiceAsync(service.getUniqueId(), force));
                                    }
                                    futureActionCollection.process()
                                            .onFailure(t -> CloudAPI.getInstance().getConsole().error("Failed to stop services", t))
                                            .onSuccess(b -> commandSender.sendMessage("Stopped all services of group " + group.getName()));
                                });
                    });
        } else {
            CloudAPI.getInstance().getServiceManager().getServiceAsync(serviceName)
                    .onFailure(t -> CloudAPI.getInstance().getConsole().error("Failed to get service", t))
                    .onSuccess(service -> {
                        if (service == null) {
                            commandSender.sendMessage("Service not found");
                            return;
                        }
                        commandSender.sendMessage((force ? "Force stopping" : "Stopping") + " service " + service.getServiceName() + "...");
                        NodeLauncher.getInstance().getServiceManager().stopServiceAsync(service.getUniqueId(), force)
                                .onFailure(t -> CloudAPI.getInstance().getConsole().error("Failed to stop service", t))
                                .onSuccess(b -> commandSender.sendMessage("Stopped service " + service.getServiceName()));
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
                    for (ICloudService service : services) {
                        if(service.getServiceState() == ServiceState.OFFLINE) continue;
                        amount++;
                        lines.add("§8 » %tc" + service.getServiceName() + " §8| %hc" + service.getOnlineCount() + "§8/%tc" + service.getMaxPlayers());
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
                            .onSuccess(service -> {
                                service.getServiceVersion()
                                        .onFailure(t -> CloudAPI.getInstance().getConsole().error("Failed to get service version", t))
                                        .onSuccess(versionHolder -> {
                                            CloudAPI.getInstance().getNodeManager().getNodeAsync(service.getNodeId())
                                                .onFailure(t -> CloudAPI.getInstance().getConsole().error("Failed to get node", t))
                                                .onSuccess(node -> {
                                                    commandSender.sendMessage("");
                                                    commandSender.sendMessage("%tcService info of %hc" + service.getServiceName() + "§8:");
                                                    commandSender.sendMessage("§8 »%tc Environment: %hc" + service.getEnvironment().name());
                                                    commandSender.sendMessage("§8 »%tc Group: %hc" + (service.isGroupBased() ? service.getGroupName() : "None"));
                                                    commandSender.sendMessage("§8 »%tc Version: %hc" + versionHolder.getName());
                                                    commandSender.sendMessage("§8 »%tc State: %hc" + service.getServiceState().name());
                                                    commandSender.sendMessage("§8 »%tc Players: %hc" + service.getOnlineCount() + "§8/%hc" + service.getMaxPlayers());
                                                    commandSender.sendMessage("§8 »%tc RAM: %hc" + service.getMaxRam() + " MB");
                                                    commandSender.sendMessage("§8 »%tc External: %hc" + service.isExternal());
                                                    commandSender.sendMessage("§8 »%tc Node: %hc" + node.getName());
                                                    commandSender.sendMessage("§8 »%tc Maintenance: %hc" + service.isMaintenance());
                                                    StringBuilder templateBuilder = new StringBuilder();
                                                    for (String templateName : service.getConfiguration().getTemplateNames()) {
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
