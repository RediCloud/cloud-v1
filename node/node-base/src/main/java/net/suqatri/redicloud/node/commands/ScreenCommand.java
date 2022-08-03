package net.suqatri.redicloud.node.commands;

import net.suqatri.commands.CommandHelp;
import net.suqatri.commands.CommandSender;
import net.suqatri.commands.ConsoleCommand;
import net.suqatri.commands.annotation.*;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.node.service.screen.IServiceScreen;
import net.suqatri.redicloud.node.NodeLauncher;

@CommandAlias("screen|scr")
public class ScreenCommand extends ConsoleCommand {

    /*
    /screen join <Service>
    /screen leave <Service>
    /screen actives
    /screen leaveall
    /screen toggle <service>
     */

    @Default
    @Subcommand("help")
    @HelpCommand
    @Description("Show help for this command")
    @Syntax("[Page]")
    public void onHelp(CommandHelp commandHelp) {
        commandHelp.showHelp();
    }

    @Subcommand("toggle")
    @Description("Toggle the screen of a service")
    @Syntax("<Service>")
    @CommandCompletion("@running_services")
    public void onToggle(CommandSender commandSender, String serviceName) {
        CloudAPI.getInstance().getConsole().trace("Check existences of screen " + serviceName);
        CloudAPI.getInstance().getServiceManager().existsServiceAsync(serviceName)
                .onFailure(e -> CloudAPI.getInstance().getConsole().error("Error while checking if service exists", e))
                .onSuccess(exists -> {
                    if (!exists) {
                        commandSender.sendMessage("Service " + serviceName + " doesn't exist");
                        return;
                    }
                    CloudAPI.getInstance().getConsole().trace("Get service of screen " + serviceName);
                    CloudAPI.getInstance().getServiceManager().getServiceAsync(serviceName)
                            .onFailure(e -> CloudAPI.getInstance().getConsole().error("Error while getting service", e))
                            .onSuccess(serviceHolder -> {
                                if(serviceHolder.get().isExternal()) {
                                    commandSender.sendMessage("Joining external screens is not supported!");
                                    return;
                                }
                                IServiceScreen serviceScreen = NodeLauncher.getInstance().getScreenManager().getServiceScreen(serviceHolder);
                                if (NodeLauncher.getInstance().getScreenManager().isActive(serviceScreen)) {
                                    NodeLauncher.getInstance().getScreenManager().leave(serviceScreen);
                                    commandSender.sendMessage("Screen " + serviceName + " left!");
                                } else {
                                    commandSender.sendMessage("Joining screen " + serviceName + "...");
                                    NodeLauncher.getInstance().getScreenManager().join(serviceScreen)
                                            .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to join screen " + serviceName + "!", e))
                                            .onSuccess(s -> commandSender.sendMessage("Screen " + serviceName + " joined!"));
                                }
                            });
                });
    }

    @Subcommand("write")
    @Description("Write a message to all active screens")
    @Syntax("<Message>")
    public void onWrite(CommandSender commandSender, String command){
        if(command.startsWith("/")) {
            CloudAPI.getInstance().getConsole().warn("§cYou can write commands without the / prefix!");
        }
        commandSender.sendMessage("Writing message \"" + command + "\" to all screens...");
        NodeLauncher.getInstance().getScreenManager().write(command);
    }


    @Subcommand("join")
    @Description("Join a screen")
    @Syntax("<Service>")
    @CommandCompletion("@running_services")
    public void onJoin(CommandSender commandSender, String serviceName) {
        CloudAPI.getInstance().getConsole().trace("Check existences of screen " + serviceName);
        CloudAPI.getInstance().getServiceManager().existsServiceAsync(serviceName)
                .onFailure(e -> CloudAPI.getInstance().getConsole().error("Error while checking service existence!", e))
                .onSuccess(exists -> {
                    if (!exists) {
                        commandSender.sendMessage("Screen " + serviceName + " doesn't exist!");
                        return;
                    }
                    CloudAPI.getInstance().getConsole().trace("Get service of screen " + serviceName);
                    CloudAPI.getInstance().getServiceManager().getServiceAsync(serviceName)
                            .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to join screen " + serviceName + "!", e))
                            .onSuccess(serviceHolder -> {
                                if(serviceHolder.get().isExternal()) {
                                    commandSender.sendMessage("Joining external screens is not supported!");
                                    return;
                                }
                                IServiceScreen serviceScreen = NodeLauncher.getInstance().getScreenManager().getServiceScreen(serviceHolder);
                                if (NodeLauncher.getInstance().getScreenManager().isActive(serviceScreen)) {
                                    commandSender.sendMessage("Screen " + serviceName + " is already active!");
                                    return;
                                }
                                commandSender.sendMessage("Joining screen " + serviceName + "...");
                                NodeLauncher.getInstance().getScreenManager().join(serviceScreen)
                                        .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to join screen " + serviceName + "!", e))
                                        .onSuccess(s -> commandSender.sendMessage("Screen " + serviceName + " joined!"));
                            });
                });
    }

    @Subcommand("leave")
    @Description("Leave a screen")
    @Syntax("<Service>")
    @CommandCompletion("@running_services")
    public void onLeave(CommandSender commandSender, String serviceName) {
        CloudAPI.getInstance().getConsole().trace("Check existences of screen " + serviceName);
        CloudAPI.getInstance().getServiceManager().existsServiceAsync(serviceName)
                .onFailure(e -> CloudAPI.getInstance().getConsole().error("Error while checking service existence!", e))
                .onSuccess(exists -> {
                    if (!exists) {
                        commandSender.sendMessage("Screen " + serviceName + " is not active!");
                        return;
                    }
                    CloudAPI.getInstance().getConsole().trace("Get service of screen " + serviceName);
                    CloudAPI.getInstance().getServiceManager().getServiceAsync(serviceName)
                            .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to leave screen " + serviceName + "!", e))
                            .onSuccess(serviceHolder -> {
                                IServiceScreen serviceScreen = NodeLauncher.getInstance().getScreenManager().getServiceScreen(serviceHolder);
                                if (!NodeLauncher.getInstance().getScreenManager().isActive(serviceScreen)) {
                                    commandSender.sendMessage("Screen " + serviceName + " is not active!");
                                    return;
                                }
                                NodeLauncher.getInstance().getScreenManager().leave(serviceScreen);
                                commandSender.sendMessage("Screen " + serviceName + " left!");
                            });
                });
    }

    @Subcommand("actives")
    @Description("Show active screens")
    public void onActives(CommandSender commandSender) {
        if (NodeLauncher.getInstance().getScreenManager().getActiveScreens().isEmpty()) {
            commandSender.sendMessage("No active screens!");
            return;
        }
        commandSender.sendMessage("Active screens §8(%hc" + NodeLauncher.getInstance().getScreenManager().getActiveScreens().size() + "§8)%tc:");
        for (IServiceScreen serviceScreen : NodeLauncher.getInstance().getScreenManager().getActiveScreens()) {
            commandSender.sendMessage("§8 » %tc" + serviceScreen.getService().get().getServiceName());
        }
    }

    @Subcommand("leaveall")
    @Description("Leave all screens")
    public void onLeaveAll(CommandSender commandSender) {
        for (IServiceScreen activeScreen : NodeLauncher.getInstance().getScreenManager().getActiveScreens()) {
            NodeLauncher.getInstance().getScreenManager().leave(activeScreen);
        }
        commandSender.sendMessage("All screens left!");
    }

}
