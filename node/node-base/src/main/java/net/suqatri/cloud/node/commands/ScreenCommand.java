package net.suqatri.cloud.node.commands;

import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.node.service.screen.IServiceScreen;
import net.suqatri.cloud.node.NodeLauncher;
import net.suqatri.cloud.node.service.screen.ServiceScreen;
import net.suqatri.commands.CommandHelp;
import net.suqatri.commands.CommandSender;
import net.suqatri.commands.ConsoleCommand;
import net.suqatri.commands.annotation.*;

@CommandAlias("screen|scr")
public class ScreenCommand extends ConsoleCommand {

    /*
    /screen join <Service>
    /screen leave <Service>
    /screen actives
    /screen leaveall
     */

    @Default
    @Subcommand("help")
    @HelpCommand
    @Description("Show help for this command")
    @Syntax("[Page]")
    public void onHelp(CommandHelp commandHelp){
        commandHelp.showHelp();
    }

    @Subcommand("join")
    @Description("Join a screen")
    @Syntax("<Service>")
    public void onJoin(CommandSender commandSender, String serviceName){
        CloudAPI.getInstance().getServiceManager().existsServiceAsync(serviceName)
                .onFailure(e -> CloudAPI.getInstance().getConsole().error("Error while checking service existence!", e))
                .onSuccess(exists -> {
                    if(!exists){
                        commandSender.sendMessage("Screen " + serviceName + " does not exist!");
                        return;
                    }
                    CloudAPI.getInstance().getServiceManager().getServiceAsync(serviceName)
                        .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to join screen " + serviceName + "!", e))
                        .onSuccess(serviceHolder -> {
                            IServiceScreen serviceScreen = NodeLauncher.getInstance().getScreenManager().getServiceScreen(serviceHolder);
                            if(NodeLauncher.getInstance().getScreenManager().isActive(serviceScreen)) {
                                commandSender.sendMessage("Screen " + serviceName + " is already active!");
                                return;
                            }
                            NodeLauncher.getInstance().getScreenManager().join(serviceScreen)
                                .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to join screen " + serviceName + "!", e))
                                .onSuccess(s -> commandSender.sendMessage("Screen " + serviceName + " joined!"));
                        });
                });
    }

    @Subcommand("leave")
    @Description("Leave a screen")
    @Syntax("<Service>")
    public void onLeave(CommandSender commandSender, String serviceName){
        CloudAPI.getInstance().getServiceManager().existsServiceAsync(serviceName)
                .onFailure(e -> CloudAPI.getInstance().getConsole().error("Error while checking service existence!", e))
                .onSuccess(exists -> {
                    if(!exists){
                        commandSender.sendMessage("Screen " + serviceName + " is not active!");
                        return;
                    }
                    CloudAPI.getInstance().getServiceManager().getServiceAsync(serviceName)
                            .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to leave screen " + serviceName + "!", e))
                            .onSuccess(serviceHolder -> {
                                IServiceScreen serviceScreen = NodeLauncher.getInstance().getScreenManager().getServiceScreen(serviceHolder);
                                if(!NodeLauncher.getInstance().getScreenManager().isActive(serviceScreen)) {
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
    public void onActives(CommandSender commandSender){
        if(NodeLauncher.getInstance().getScreenManager().getActiveScreens().isEmpty()){
            commandSender.sendMessage("No active screens!");
            return;
        }
        commandSender.sendMessage("Active screens §8(%hc" + NodeLauncher.getInstance().getScreenManager().getActiveScreens().size() + "§8)%tc:");
        for(IServiceScreen serviceScreen : NodeLauncher.getInstance().getScreenManager().getActiveScreens()){
            commandSender.sendMessage("§8 » %tc" + serviceScreen.getService().get().getServiceName());
        }
    }

    @Subcommand("leaveall")
    @Description("Leave all screens")
    public void onLeaveAll(CommandSender commandSender){
        for (IServiceScreen activeScreen : NodeLauncher.getInstance().getScreenManager().getActiveScreens()) {
            NodeLauncher.getInstance().getScreenManager().leave(activeScreen);
        }
        commandSender.sendMessage("All screens left!");
    }

}
