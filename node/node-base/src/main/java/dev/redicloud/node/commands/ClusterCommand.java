package dev.redicloud.node.commands;

import dev.redicloud.commands.CommandHelp;
import dev.redicloud.commands.CommandSender;
import dev.redicloud.commands.ConsoleCommand;
import dev.redicloud.commands.annotation.*;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.impl.configuration.ConfigurationsReloadPacket;
import dev.redicloud.api.impl.node.CloudNode;
import dev.redicloud.api.impl.node.CloudNodeManager;
import dev.redicloud.api.node.ICloudNode;
import dev.redicloud.api.packet.PacketChannel;
import dev.redicloud.api.service.configuration.IServiceStartConfiguration;
import dev.redicloud.node.node.packet.NodePingPacket;
import dev.redicloud.node.NodeLauncher;
import org.redisson.api.RPriorityBlockingDeque;

import java.util.List;
import java.util.concurrent.TimeoutException;
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
    public void onHelp(CommandHelp commandHelp) {
        commandHelp.showHelp();
    }

    @Subcommand("info")
    @Description("Show information about this node")
    public void onInfo(CommandSender commandSender) {
        CloudNode node = NodeLauncher.getInstance().getNode();
        commandSender.sendMessage("%tc" + node.getName() + " §7[%hc" + node.getUniqueId() + "§7]: %hc" + node.getName());
        commandSender.sendMessage("§8   » %tcLast-IP: %hc" + node.getHostname());
        commandSender.sendMessage("§8   » %tcUp-Time: %hc" + node.getUpTime());
        commandSender.sendMessage("§8   » %tcServices: %hc" + node.getStartedServiceUniqueIds().size());
        commandSender.sendMessage("§8   » %tcRAM: %hc" + node.getMemoryUsage() + "/" + node.getMaxMemory() + "MB");
        commandSender.sendMessage("§8   » %tcVersion: %hc" + node.getVersion());
    }

    @Subcommand("reload config|configuration|configs|configurations")
    @Description("Reload all configurations")
    public void onReloadConfigs(CommandSender commandSender){
        ConfigurationsReloadPacket packet = new ConfigurationsReloadPacket();
        CloudAPI.getInstance().getNetworkComponentManager().getAllComponentInfoAsync()
            .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to get all components", e))
            .onSuccess(components -> {
                packet.publishAllAsync();
                commandSender.sendMessage("Reloaded all configurations");
            });
    }

    @Subcommand("queue")
    @Description("Show the factory queue of the cluster")
    public void onQueue(CommandSender commandSender){
        RPriorityBlockingDeque<IServiceStartConfiguration> queue = NodeLauncher.getInstance().getServiceFactory().getThread().getQueue();
        commandSender.sendMessage("§8   » %tcQueue: %hc" + queue.size());
        commandSender.sendMessage("§8   » %tcQueue-Items: %hc" + queue.stream().map(IServiceStartConfiguration::getGroupName).collect(Collectors.toList()));
    }

    @Subcommand("ping")
    @Description("Ping a node")
    @Syntax("<Node>")
    @CommandCompletion("@connected_nodes")
    public void onPing(CommandSender commandSender, String nodeName) {
        CloudAPI.getInstance().getNodeManager().existsNodeAsync(nodeName)
                .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to check existence of node " + nodeName, e))
                .onSuccess(exists -> {
                    if (!exists) {
                        commandSender.sendMessage("Node not found!");
                        return;
                    }
                    CloudAPI.getInstance().getNodeManager().getNodeAsync(nodeName)
                            .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to get node " + nodeName, e))
                            .onSuccess(node -> {
                                if (!node.isConnected()) {
                                    commandSender.sendMessage("Node not connected!");
                                    return;
                                }
                                NodePingPacket packet = new NodePingPacket();
                                packet.getPacketData().setChannel(PacketChannel.NODE);
                                packet.getPacketData().addReceiver(node.getNetworkComponentInfo());
                                packet.getPacketData().waitForResponse()
                                        .onFailure(e -> {
                                            if (e instanceof TimeoutException) {
                                                CloudAPI.getInstance().getConsole().error("Ping timeout after 30 seconds for node " + nodeName);
                                            } else {
                                                CloudAPI.getInstance().getConsole().error("Ping timeout after 30 seconds for node " + nodeName, e);
                                            }
                                        })
                                        .onSuccess(response -> {
                                            commandSender.sendMessage("Ping successful!");
                                            commandSender.sendMessage("Ping time: " + (System.currentTimeMillis() - packet.getTime()) + "ms");
                                        });
                                packet.publishAsync();
                            });
                });
    }

    @Subcommand("info")
    @Syntax("<Node>")
    @Description("Show information about a specific node")
    @CommandCompletion("@nodes")
    public void onInfoNode(CommandSender commandSender, String nodeName) {
        commandSender.sendMessage("Loading node...");
        CloudAPI.getInstance().getNodeManager().getNodeAsync(nodeName)
                .onFailure(e -> commandSender.sendMessage("Can't find node " + nodeName))
                .onSuccess(nodeToCast -> {
                    CloudNode node = (CloudNode) nodeToCast;
                    if (node.isConnected()) {
                        commandSender.sendMessage("%tc" + node.getName() + " §7[§f" + node.getUniqueId() + "§7]: %hc" + node.getName());
                        commandSender.sendMessage("§8   » %tcLast-IP: %hc" + node.getHostname());
                        commandSender.sendMessage("§8   » %tcUp-Time: %hc" + node.getUpTime());
                        commandSender.sendMessage("§8   » %tcServices: %hc" + node.getStartedServiceUniqueIds().size());
                        commandSender.sendMessage("§8   » %tcRAM: %hc" + node.getMemoryUsage() + "/" + node.getMaxMemory() + "MB");
                        commandSender.sendMessage("§8   » %tcVersion: %hc" + node.getVersion());
                    } else {
                        commandSender.sendMessage("§8➤ %tc" + node.getName() + " §7[%hc" + node.getUniqueId() + "§7]: %hcOffline");
                    }
                });
    }

    @Subcommand("shutdown")
    @Description("Shutdown the cluster")
    public void onShutdown(CommandSender commandSender) {
        ((CloudNodeManager) NodeLauncher.getInstance().getNodeManager()).shutdownClusterAsync();
    }

    @Subcommand("nodes")
    @Description("Show all nodes in the cluster")
    public void onNodes(CommandSender commandSender) {
        commandSender.sendMessage("Loading nodes...");
        CloudAPI.getInstance().getNodeManager().getNodesAsync()
                .onFailure(e -> CloudAPI.getInstance().getConsole().error("§cFailed to get nodes", (Throwable) e))
                .onSuccess(nodes -> {
                    List<ICloudNode> connectedNodes = nodes.parallelStream().filter(ICloudNode::isConnected).collect(Collectors.toList());
                    List<ICloudNode> offlineNodes = nodes.parallelStream().filter(node -> !node.isConnected()).collect(Collectors.toList());
                    commandSender.sendMessage("");
                    commandSender.sendMessage("Connected nodes: %hc" + connectedNodes.size());
                    commandSender.sendMessage("Offline nodes: %hc" + offlineNodes.size());
                    for (ICloudNode node : connectedNodes) {
                        commandSender.sendMessage("%tc" + node.getName() + " §7[%hc" + node.getUniqueId() + "§7]: %hc" + node.getName());
                        commandSender.sendMessage("§8   » %tcLast-IP: %hc" + node.getHostname());
                        commandSender.sendMessage("§8   » %tcUp-Time: %hc" + node.getUpTime());
                        commandSender.sendMessage("§8   » %tcServices: %hc" + node.getStartedServicesCount());
                        commandSender.sendMessage("§8   » %tcRAM: %hc" + node.getMemoryUsage() + "/" + node.getMaxMemory() + "MB");
                        commandSender.sendMessage("§8   » %tcVersion: %hc" + node.getVersion());
                    }
                    commandSender.sendMessage("");
                });
    }

}
