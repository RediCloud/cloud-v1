package net.suqatri.cloud.node.commands;

import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.impl.node.CloudNode;
import net.suqatri.cloud.api.impl.node.CloudNodeManager;
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

    /*
        * /cluster nodes
        * /cluster info [Node]
        * /cluster shutdown
     */

    @HelpCommand
    @Default
    public void onHelp(CommandHelp commandHelp){
        commandHelp.showHelp();
    }

    @Subcommand("info")
    public void onInfo(CommandSender commandSender){
        CloudNode node = NodeLauncher.getInstance().getNode();
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
            .onSuccess(nodeHolder -> {
                CloudNode node = nodeHolder.getImpl(CloudNode.class);
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
        ((CloudNodeManager)NodeLauncher.getInstance().getNodeManager()).shutdownClusterAsync();
    }

    @Subcommand("nodes")
    public void onNodes(CommandSender commandSender){
        commandSender.sendMessage("Loading nodes...");
        CloudAPI.getInstance().getNodeManager().getNodesAsync()
                .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to get nodes", (Throwable) e))
                .onSuccess(nodes -> {
                    List<ICloudNode> connectedNodes = nodes.parallelStream().map(IRBucketHolder::get).filter(ICloudNode::isConnected).collect(Collectors.toList());
                    List<ICloudNode> offlineNodes = nodes.parallelStream().map(IRBucketHolder::get).filter(node -> !node.isConnected()).collect(Collectors.toList());
                    commandSender.sendMessage("");
                    commandSender.sendMessage("Connected nodes: " + NodeLauncher.getInstance().getConsole().getHighlightColor() + connectedNodes.size());
                    commandSender.sendMessage("Offline nodes: " + NodeLauncher.getInstance().getConsole().getHighlightColor() + offlineNodes.size());
                    for(ICloudNode node : connectedNodes){
                        commandSender.sendMessage(NodeLauncher.getInstance().getConsole().getTextColor() + node.getName() + " §7[§f" + node.getUniqueId() + "§7]: " + NodeLauncher.getInstance().getConsole().getHighlightColor() + node.getName());
                        commandSender.sendMessage("§8   » " + NodeLauncher.getInstance().getConsole().getTextColor() + "Last-IP: " + NodeLauncher.getInstance().getConsole().getHighlightColor() + node.getHostname());
                        commandSender.sendMessage("§8   » " + NodeLauncher.getInstance().getConsole().getTextColor() + "Up-Time: " + NodeLauncher.getInstance().getConsole().getHighlightColor() + node.getUpTime());
                        commandSender.sendMessage("§8   » " + NodeLauncher.getInstance().getConsole().getTextColor() + "Services: " + NodeLauncher.getInstance().getConsole().getHighlightColor() + node.getStartedServicesCount());
                    }
                    commandSender.sendMessage("");
                });
    }

}
