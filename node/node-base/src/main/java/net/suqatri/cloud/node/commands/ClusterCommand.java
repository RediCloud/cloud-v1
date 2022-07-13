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
    @Syntax("[Page]")
    @Subcommand("help")
    @Description("Show help for cluster commands")
    public void onHelp(CommandHelp commandHelp){
        commandHelp.showHelp();
    }

    @Subcommand("info")
    @Description("Show information about the cluster")
    public void onInfo(CommandSender commandSender){
        CloudNode node = NodeLauncher.getInstance().getNode();
        commandSender.sendMessage("%tc" + node.getName() + " §7[%hc" + node.getUniqueId() + "§7]: %hc" + node.getName());
        commandSender.sendMessage("§8   » %tcLast-IP: %hc"+ node.getHostname());
        commandSender.sendMessage("§8   » %tcUp-Time: %hc" + node.getUpTime());
        commandSender.sendMessage("§8   » %tcServices: %hc" + node.getStartedServiceUniqueIds().size());
    }

    @Subcommand("info")
    @Syntax("<Node>")
    @Description("Show information about a specific node")
    public void onInfoNode(CommandSender commandSender, String nodeName){
        commandSender.sendMessage("Loading node...");
        CloudAPI.getInstance().getNodeManager().getNodeAsync(nodeName)
            .onFailure(e -> commandSender.sendMessage("Can't find node " + nodeName))
            .onSuccess(nodeHolder -> {
                CloudNode node = nodeHolder.getImpl(CloudNode.class);
                if(node.isConnected()){
                    commandSender.sendMessage("%tc" + node.getName() + " §7[§f" + node.getUniqueId() + "§7]: " + NodeLauncher.getInstance().getConsole().getHighlightColor() + node.getName());
                    commandSender.sendMessage("§8   » %tcLast-IP: " + NodeLauncher.getInstance().getConsole().getHighlightColor() + node.getHostname());
                    commandSender.sendMessage("§8   » %tcUp-Time: " + NodeLauncher.getInstance().getConsole().getHighlightColor() + node.getUpTime());
                    commandSender.sendMessage("§8   » %tcServices: " + NodeLauncher.getInstance().getConsole().getHighlightColor() + node.getStartedServiceUniqueIds().size());
                }else{
                    commandSender.sendMessage("§8➤ %tc" + node.getName() + " §7[%hc" + node.getUniqueId() + "§7]: %hcOffline");
                }
            });
    }

    @Subcommand("shutdown")
    @Description("Shutdown the cluster")
    public void onShutdown(CommandSender commandSender){
        ((CloudNodeManager)NodeLauncher.getInstance().getNodeManager()).shutdownClusterAsync();
    }

    @Subcommand("nodes")
    @Description("Show all nodes in the cluster")
    public void onNodes(CommandSender commandSender){
        commandSender.sendMessage("Loading nodes...");
        CloudAPI.getInstance().getNodeManager().getNodesAsync()
                .onFailure(e -> CloudAPI.getInstance().getConsole().error("§cFailed to get nodes", (Throwable) e))
                .onSuccess(nodes -> {
                    List<ICloudNode> connectedNodes = nodes.parallelStream().map(IRBucketHolder::get).filter(ICloudNode::isConnected).collect(Collectors.toList());
                    List<ICloudNode> offlineNodes = nodes.parallelStream().map(IRBucketHolder::get).filter(node -> !node.isConnected()).collect(Collectors.toList());
                    commandSender.sendMessage("");
                    commandSender.sendMessage("Connected nodes: %hc" + connectedNodes.size());
                    commandSender.sendMessage("Offline nodes: %hc" + offlineNodes.size());
                    for(ICloudNode node : connectedNodes){
                        commandSender.sendMessage("%tc" + node.getName() + " §7[%hc" + node.getUniqueId() + "§7]: %hc" + node.getName());
                        commandSender.sendMessage("§8   » %tcLast-IP: %gc" + node.getHostname());
                        commandSender.sendMessage("§8   » %tcUp-Time: %hc" + node.getUpTime());
                        commandSender.sendMessage("§8   » %tcServices: %hc" + node.getStartedServicesCount());
                    }
                    commandSender.sendMessage("");
                });
    }

}
