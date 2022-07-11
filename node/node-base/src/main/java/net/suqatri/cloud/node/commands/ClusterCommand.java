package net.suqatri.cloud.node.commands;

import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.impl.node.CloudNode;
import net.suqatri.cloud.api.node.ICloudNode;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.node.NodeLauncher;
import net.suqatri.commands.CommandHelp;
import net.suqatri.commands.CommandSender;
import net.suqatri.commands.ConsoleCommand;
import net.suqatri.commands.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@CommandAlias("cluster")
public class ClusterCommand extends ConsoleCommand {

    @HelpCommand
    @Default
    public void onHelp(CommandHelp commandHelp){
        commandHelp.showHelp();
    }

    @Subcommand("info")
    public void onInfo(CommandSender commandSender){
        CloudNode node = NodeLauncher.getInstance().getCloudNode().get();
        commandSender.sendMessage(NodeLauncher.getInstance().getConsole().getTextColor() + node.getName() + " §7[§f" + node.getUniqueId() + "§7]: " + NodeLauncher.getInstance().getConsole().getHighlightColor() + node.getName());
        commandSender.sendMessage("§8   » " + NodeLauncher.getInstance().getConsole().getTextColor() + "Last-IP: " + NodeLauncher.getInstance().getConsole().getHighlightColor() + node.getHostname());
        commandSender.sendMessage("§8   » " + NodeLauncher.getInstance().getConsole().getTextColor() + "Up-Time: " + NodeLauncher.getInstance().getConsole().getHighlightColor() + node.getUpTime());
        commandSender.sendMessage("§8   » " + NodeLauncher.getInstance().getConsole().getTextColor() + "Services: " + NodeLauncher.getInstance().getConsole().getHighlightColor() + node.getStartedServiceUniqueIds().size());
    }

    @Subcommand("info")
    @Syntax("<Node>")
    public void onInfoNode(CommandSender commandSender, String nodeName){
        commandSender.sendMessage("Loading node...");
        CloudAPI.getInstance().getNodeManager().getNodeAsync(nodeName)
            .onFailure(e -> commandSender.sendMessage("Can't find node " + nodeName))
            .onSuccess((Consumer<IRBucketHolder<CloudNode>>) nodeHolder -> {
                CloudNode node = nodeHolder.get();
                if(node.isConnected()){
                    commandSender.sendMessage(NodeLauncher.getInstance().getConsole().getTextColor() + node.getName() + " §7[§f" + node.getUniqueId() + "§7]: " + NodeLauncher.getInstance().getConsole().getHighlightColor() + node.getName());
                    commandSender.sendMessage("§8   » " + NodeLauncher.getInstance().getConsole().getTextColor() + "Last-IP: " + NodeLauncher.getInstance().getConsole().getHighlightColor() + node.getHostname());
                    commandSender.sendMessage("§8   » " + NodeLauncher.getInstance().getConsole().getTextColor() + "Up-Time: " + NodeLauncher.getInstance().getConsole().getHighlightColor() + node.getUpTime());
                    commandSender.sendMessage("§8   » " + NodeLauncher.getInstance().getConsole().getTextColor() + "Services: " + NodeLauncher.getInstance().getConsole().getHighlightColor() + node.getStartedServiceUniqueIds().size());
                }else{
                    commandSender.sendMessage("§8➤ " + NodeLauncher.getInstance().getConsole().getTextColor() + node.getName() + " §7[§f" + node.getUniqueId() + "§7]: " + NodeLauncher.getInstance().getConsole().getHighlightColor() + "Offline");
                }
            });
    }

    @Subcommand("shutdown")
    public void onShutdown(CommandSender commandSender){
        NodeLauncher.getInstance().getNodeManager().shutdownClusterAsync();
    }

    @Subcommand("nodes")
    public void onNodes(CommandSender commandSender){
        commandSender.sendMessage("Loading nodes...");
        CloudAPI.getInstance().getNodeManager().getNodesAsync()
                .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to get nodes", (Throwable) e))
                .onSuccess((Consumer<Collection<IRBucketHolder<CloudNode>>>) nodes -> {
                    List<CloudNode> connectedNodes = nodes.parallelStream().map(IRBucketHolder::get).filter(CloudNode::isConnected).collect(Collectors.toList());
                    List<CloudNode> offlineNodes = nodes.parallelStream().map(IRBucketHolder::get).filter(node -> !node.isConnected()).collect(Collectors.toList());
                    commandSender.sendMessage("");
                    commandSender.sendMessage("Connected nodes: " + NodeLauncher.getInstance().getConsole().getHighlightColor() + connectedNodes.size());
                    commandSender.sendMessage("Offline nodes: " + NodeLauncher.getInstance().getConsole().getHighlightColor() + offlineNodes.size());
                    for(CloudNode node : connectedNodes){
                        commandSender.sendMessage(NodeLauncher.getInstance().getConsole().getTextColor() + node.getName() + " §7[§f" + node.getUniqueId() + "§7]: " + NodeLauncher.getInstance().getConsole().getHighlightColor() + node.getName());
                        commandSender.sendMessage("§8   » " + NodeLauncher.getInstance().getConsole().getTextColor() + "Last-IP: " + NodeLauncher.getInstance().getConsole().getHighlightColor() + node.getHostname());
                        commandSender.sendMessage("§8   » " + NodeLauncher.getInstance().getConsole().getTextColor() + "Up-Time: " + NodeLauncher.getInstance().getConsole().getHighlightColor() + node.getUpTime());
                        commandSender.sendMessage("§8   » " + NodeLauncher.getInstance().getConsole().getTextColor() + "Services: " + NodeLauncher.getInstance().getConsole().getHighlightColor() + node.getStartedServiceUniqueIds().size());
                    }
                    commandSender.sendMessage("");
                });
    }

    @Subcommand("template push-all")
    public void onTemplatePushAll(CommandSender commandSender){
        commandSender.sendMessage("Pushing all templates to all nodes...");
    }

    @Subcommand("template push")
    @Syntax("<Template>")
    public void onTemplatePush(CommandSender commandSender, String template){
        commandSender.sendMessage("Pushing template " + template + " to all nodes...");
    }

}
