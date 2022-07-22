package net.suqatri.redicloud.node.commands;

import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.network.INetworkComponentInfo;
import net.suqatri.redicloud.api.node.service.screen.IScreenLine;
import net.suqatri.redicloud.api.node.service.screen.IServiceScreen;
import net.suqatri.redicloud.node.NodeLauncher;
import net.suqatri.commands.CommandSender;
import net.suqatri.commands.ConsoleCommand;
import net.suqatri.commands.annotation.CommandAlias;
import net.suqatri.commands.annotation.Description;
import net.suqatri.commands.annotation.Subcommand;
import net.suqatri.commands.annotation.Syntax;

import java.util.UUID;

@CommandAlias("debug")
public class DebugCommand extends ConsoleCommand {

    @Subcommand("facatory queue")
    @Description("Prints the factory queue")
    public void onFactoryQueue(CommandSender commandSender){
        commandSender.sendMessage("Factory queue: " + NodeLauncher.getInstance().getServiceFactory().getThread().getQueue());
    }

    @Subcommand("facatory processes")
    @Description("Prints the factory processes")
    public void onFactoryProcesses(CommandSender commandSender){
        commandSender.sendMessage("Factory processes: ");
        for (UUID uuid : NodeLauncher.getInstance().getServiceFactory().getThread().getProcesses().keySet()) {
            commandSender.sendMessage(" - " + uuid);
        }
    }

    @Subcommand("file-transfer sent")
    @Description("Show how many sent file transfers are currently queued")
    public void onFileTransferSent(CommandSender commandSender){
        commandSender.sendMessage("Sent process queue size: " + NodeLauncher.getInstance().getFileTransferManager().getThread().getSentProcesses().size());
    }

    @Subcommand("restart")
    @Syntax("<Start-Delay in seconds>")
    @Description("Restarts the node")
    public void onRestart(CommandSender commandSender, long delay){
        NodeLauncher.getInstance().getNode().setTimeOut(System.currentTimeMillis()+(1000 * delay));
        NodeLauncher.getInstance().getNode().updateAsync();
        NodeLauncher.getInstance().restartNode();
    }

    @Subcommand("file-transfer pull")
    @Description("Show the pull complete listeners")
    public void onFilePull(CommandSender commandSender){
        commandSender.sendMessage((NodeLauncher.getInstance().getFileTransferManager().getPullingRequest() != null) + " future action");
    }

    @Subcommand("file-transfer received")
    @Description("Show how many read file transfers are currently queued")
    public void onFileTransferReceived(CommandSender commandSender){
        commandSender.sendMessage("Received process queue size: " + NodeLauncher.getInstance().getFileTransferManager().getThread().getReceiveProcesses().size());
    }

    @Subcommand("redis keys")
    @Description("Show all redis keys")
    public void onRedisKeys(CommandSender commandSender){
        commandSender.sendMessage("Keys:");
        for (String key : NodeLauncher.getInstance().getRedisConnection().getClient().getKeys().getKeys()) {
            commandSender.sendMessage(key);
        }
        commandSender.sendMessage("------");
    }

    @Syntax("<Pattern>")
    @Subcommand("redis keys pattern")
    @Description("Show all redis keys matching a pattern")
    public void onRedisKeysPattern(CommandSender commandSender, String pattern){
        commandSender.sendMessage("Pattern: " + pattern);
        commandSender.sendMessage("Keys:");
        for (String key : NodeLauncher.getInstance().getRedisConnection().getClient().getKeys().getKeysByPattern(pattern)) {
            commandSender.sendMessage(key);
        }
        commandSender.sendMessage("------");
    }

    @Subcommand("impl servicefactory")
    @Description("Show impl of servicefactory")
    public void onImplServicefactory(CommandSender commandSender){
        commandSender.sendMessage("ServiceFactory: " + CloudAPI.getInstance().getServiceFactory().getClass().getName());
    }

    @Subcommand("impl eventmanager")
    @Description("Show impl of eventmanager")
    public void onImplEventmanager(CommandSender commandSender){
        commandSender.sendMessage("EventManager: " + CloudAPI.getInstance().getEventManager().getClass().getName());
    }

    @Subcommand("impl templatemanager")
    @Description("Show impl of templatemanager")
    public void onImplTemplatemanager(CommandSender commandSender){
        commandSender.sendMessage("TemplateManager: " + CloudAPI.getInstance().getServiceTemplateManager().getClass().getName());
    }

    @Subcommand("impl serviceversionmanager")
    @Description("Show impl of serviceversionmanager")
    public void onImplServiceversionmanager(CommandSender commandSender){
        commandSender.sendMessage("ServiceVersionManager: " + CloudAPI.getInstance().getServiceVersionManager().getClass().getName());
    }

    @Subcommand("serviceversion patched")
    @Description("Show if serviceversion is patched")
    public void onServiceversionIspatched(CommandSender commandSender, String serviceVersionId){
        commandSender.sendMessage("ServiceVersion: " + serviceVersionId + " is patched: " + CloudAPI.getInstance().getServiceVersionManager().getServiceVersion(serviceVersionId).get().isPatched());
    }

    @Subcommand("serviceversion downloaded")
    @Description("Show if serviceversion is downloaded")
    public void onServiceversionIsdownloaded(CommandSender commandSender, String serviceVersionId){
        commandSender.sendMessage("ServiceVersion: " + serviceVersionId + " is downloaded: " + CloudAPI.getInstance().getServiceVersionManager().getServiceVersion(serviceVersionId).get().isDownloaded());
    }

    @Subcommand("screen removelines")
    @Description("Remove useless lines from screen")
    @Syntax("<Service>")
    public void onScreenRemovelines(CommandSender commandSender, String serviceName){
        IServiceScreen screen = NodeLauncher.getInstance().getScreenManager().getServiceScreen(CloudAPI.getInstance().getServiceManager().getService(serviceName));
        if(screen.getLines().size() <= 50) {
            commandSender.sendMessage("Screen is already small enough: " + screen.getLines().size());
            return;
        }
        for(IScreenLine line : screen.getLines().readAll()){
            if(screen.getLines().size() <= 50) break;
            screen.getLines().remove(line);
            commandSender.sendMessage("Removed line: " + line.getLine());
        }
    }

    @Subcommand("networkcommponents")
    @Description("Show network components")
    public void onNetworkcomponents(CommandSender commandSender) {
        commandSender.sendMessage("Network components:");
        for (INetworkComponentInfo componentInfo : CloudAPI.getInstance().getNetworkComponentManager().getAllComponentInfo()) {
            commandSender.sendMessage(componentInfo.getType() + ": " + componentInfo.getIdentifier());
        }
        commandSender.sendMessage("------");
    }

}
