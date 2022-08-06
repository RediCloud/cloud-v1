package net.suqatri.redicloud.node.commands;

import net.suqatri.commands.CommandHelp;
import net.suqatri.commands.CommandSender;
import net.suqatri.commands.ConsoleCommand;
import net.suqatri.commands.annotation.*;
import net.suqatri.redicloud.api.CloudAPI;
import net.suqatri.redicloud.api.group.ICloudGroup;
import net.suqatri.redicloud.api.node.ICloudNode;
import net.suqatri.redicloud.api.template.ICloudServiceTemplate;
import net.suqatri.redicloud.commons.function.future.FutureAction;
import net.suqatri.redicloud.commons.function.future.FutureActionCollection;
import net.suqatri.redicloud.node.NodeLauncher;
import net.suqatri.redicloud.node.file.FileTransferProcessThread;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@CommandAlias("template|templates")
public class TemplateCommand extends ConsoleCommand {

    /*
    /template list
    /template create <name>
    /template delete <name>
    /template info <name>
    /template push <name>
    /template pull <Node>
     */

    @HelpCommand
    @Default
    @Subcommand("help")
    @Description("Show help for template command")
    @Syntax("[Page]")
    public void onHelp(CommandHelp commandHelp) {
        commandHelp.showHelp();
    }

    @Subcommand("list")
    @Description("List all templates")
    public void onList(CommandSender commandSender) {
        commandSender.sendMessage("Listing templates...");
        NodeLauncher.getInstance().getServiceTemplateManager().getAllTemplatesAsync()
                .onFailure(throwable -> CloudAPI.getInstance().getConsole().error("§cError while getting templates!", throwable))
                .onSuccess(templateHolders -> {
                    if (templateHolders.isEmpty()) {
                        commandSender.sendMessage("No templates found!");
                        return;
                    }
                    commandSender.sendMessage("");
                    commandSender.sendMessage("Templates: %hc" + templateHolders.size());
                    for (ICloudServiceTemplate templateHolder : templateHolders) {
                        commandSender.sendMessage(" §8- %hc" + templateHolder.getName());
                    }
                    commandSender.sendMessage("");
                });
    }

    @Subcommand("create")
    @Syntax("<Name>")
    @Description("Create a new template")
    public void onCreate(CommandSender commandSender, String name) {
        commandSender.sendMessage("Creating template %hc" + name + "...");
        NodeLauncher.getInstance().getServiceTemplateManager().existsTemplateAsync(name)
                .onFailure(throwable -> CloudAPI.getInstance().getConsole().error("§cError while checking template existence!", throwable))
                .onSuccess(exists -> {
                    if (exists) {
                        commandSender.sendMessage("Template already exists!");
                    } else {
                        NodeLauncher.getInstance().getServiceTemplateManager().createTemplateAsync(name)
                                .onFailure(throwable -> CloudAPI.getInstance().getConsole().error("§cError while creating template!", throwable))
                                .onSuccess(templateHolder -> {
                                    commandSender.sendMessage("Template created with name %hc" + templateHolder.getName());
                                    commandSender.sendMessage("Template folder: %hc" + templateHolder.getTemplateFolder().getAbsolutePath());
                                });
                    }
                });
    }

    @Subcommand("delete")
    @Syntax("<Template>")
    @CommandCompletion("@service_templates")
    @Description("Delete a template")
    public void onDelete(CommandSender commandSender, String name) {
        commandSender.sendMessage("Deleting template %hc" + name + "...");
        NodeLauncher.getInstance().getServiceTemplateManager().existsTemplateAsync(name)
                .onFailure(throwable -> CloudAPI.getInstance().getConsole().error("§cError while checking template existence!", throwable))
                .onSuccess(exists -> {
                    if (exists) {
                        NodeLauncher.getInstance().getServiceTemplateManager().deleteTemplateAsync(name)
                                .onFailure(throwable -> CloudAPI.getInstance().getConsole().error("§cError while deleting template!", throwable))
                                .onSuccess(s -> commandSender.sendMessage("Template deleted with name %hc" + name));
                    } else {
                        commandSender.sendMessage("Template doesn't exist!");
                    }
                });
    }

    @Subcommand("info")
    @Syntax("<Template>")
    @CommandCompletion("@service_templates")
    @Description("Show info for a template")
    public void onInfo(CommandSender commandSender, String name) {
        commandSender.sendMessage("Loading information of template %hc" + name + "...");
        NodeLauncher.getInstance().getServiceTemplateManager().existsTemplateAsync(name)
                .onFailure(throwable -> CloudAPI.getInstance().getConsole().error("§cError while checking template existence!", throwable))
                .onSuccess(exists -> {
                    if (exists) {
                        NodeLauncher.getInstance().getServiceTemplateManager().getTemplateAsync(name)
                                .onFailure(throwable -> CloudAPI.getInstance().getConsole().error("§cError while getting template!", throwable))
                                .onSuccess(templateHolder -> {
                                    CloudAPI.getInstance().getGroupManager().getGroupsAsync()
                                            .onFailure(throwable -> CloudAPI.getInstance().getConsole().error("§cError while getting groups!", throwable))
                                            .onSuccess(groupHolders -> {
                                                StringBuilder builder = new StringBuilder();
                                                for (ICloudGroup groupHolder : groupHolders) {
                                                    if (!builder.toString().isEmpty()) builder.append("§7, ");
                                                    builder.append(NodeLauncher.getInstance().getConsole().getHighlightColor() + groupHolder.getName());
                                                }
                                                commandSender.sendMessage("Template » %hc" + templateHolder.getName());
                                                commandSender.sendMessage("Template folder » %hc" + templateHolder.getTemplateFolder().getAbsolutePath());
                                                commandSender.sendMessage("Groups » %hc" + builder);
                                            });
                                });
                    } else {
                        commandSender.sendMessage("Template doesn't exist!");
                    }
                });
    }

    @Subcommand("push")
    @Syntax("<Node>")
    @CommandCompletion("@connected_nodes")
    @Description("Push a template to a node")
    public void onPush(CommandSender commandSender, String nodeName) {
        if (nodeName.equalsIgnoreCase(NodeLauncher.getInstance().getNode().getName())) {
            commandSender.sendMessage("You can´t push from this node to itself!");
            return;
        }
        if (FileTransferProcessThread.getCurrentSentProcess() != null) {
            commandSender.sendMessage("There is already a file transfer process running!");
            commandSender.sendMessage("Please wait until the current process is finished!");
            return;
        }
        CloudAPI.getInstance().getNodeManager().getNodeAsync(nodeName)
                .onFailure(throwable -> CloudAPI.getInstance().getConsole().error("cError while getting node!", throwable))
                .onSuccess(nodeHolder -> {
                    if (!nodeHolder.isConnected()) {
                        commandSender.sendMessage("Node is not connected!");
                        return;
                    }
                    commandSender.sendMessage("Pushing template to node %hc" + nodeHolder.getName() + "...");
                    commandSender.sendMessage("This may take a while...");
                    NodeLauncher.getInstance().getServiceTemplateManager().pushAllTemplates(nodeHolder)
                            .onFailure(throwable -> CloudAPI.getInstance().getConsole().error("§cError while pushing templates!", throwable))
                            .onSuccess(s -> commandSender.sendMessage("Templates pushed to node %hc" + nodeHolder.getName()));
                });
    }

    @Subcommand("pushall")
    @Description("Push all templates to all nodes")
    public void onPushAll(CommandSender commandSender) {
        if (!FileTransferProcessThread.getSentProcesses().isEmpty() || FileTransferProcessThread.getCurrentSentProcess() != null) {
            commandSender.sendMessage("There is already a file transfer process running!");
            commandSender.sendMessage("Please wait until the current process is finished!");
            return;
        }
        commandSender.sendMessage("Pushing all templates...");
        commandSender.sendMessage("This may take a while...");
        CloudAPI.getInstance().getNodeManager().getNodesAsync()
                .onFailure(throwable -> CloudAPI.getInstance().getConsole().error("§cError while getting nodes!", throwable))
                .onSuccess(nodeHolders -> {
                    FutureActionCollection<UUID, ICloudNode> futureActionCollection = new FutureActionCollection<>();
                    for (ICloudNode nodeHolder : nodeHolders) {
                        if (nodeHolder.getUniqueId().equals(NodeLauncher.getInstance().getNode().getUniqueId()))
                            continue;
                        if (nodeHolder.isConnected()) {
                            commandSender.sendMessage("Pushing templates to node %hc" + nodeHolder.getName() + "...");
                            FutureAction<ICloudNode> futureAction = NodeLauncher.getInstance().getServiceTemplateManager().pushAllTemplates(nodeHolder);
                            futureAction.whenComplete((s, throwable) -> {
                                if (throwable != null)
                                    commandSender.sendMessage("Error while pushing templates to node %hc" + nodeHolder.getName());
                                else
                                    commandSender.sendMessage("Templates pushed to node %hc" + nodeHolder.getName());
                            });
                            futureActionCollection.addToProcess(nodeHolder.getUniqueId(), futureAction);
                        } else {
                            commandSender.sendMessage("Node %hc" + nodeHolder.getName() + " is not connected!");
                        }
                    }
                    futureActionCollection.process()
                            .onFailure(throwable -> CloudAPI.getInstance().getConsole().error("§cError while pushing templates!", throwable))
                            .onSuccess(s -> CloudAPI.getInstance().getScheduler().runTaskLater(() -> commandSender.sendMessage("Templates pushed to all nodes!"), 1, TimeUnit.SECONDS));
                });
    }

    @Subcommand("pull")
    @Syntax("<Node>")
    @Description("Pull all templates from a node")
    @CommandCompletion("@connected_nodes")
    public void onPull(CommandSender commandSender, String nodeName) {
        CloudAPI.getInstance().getNodeManager().existsNodeAsync(nodeName)
                .onFailure(throwable -> CloudAPI.getInstance().getConsole().error("§cError while checking node existence!", throwable))
                .onSuccess(exists -> {
                    if (!exists) {
                        commandSender.sendMessage("Node doesn't exist!");
                        return;
                    }
                    CloudAPI.getInstance().getNodeManager().getNodeAsync(nodeName)
                            .onFailure(throwable -> CloudAPI.getInstance().getConsole().error("§cError while getting node!", throwable))
                            .onSuccess(nodeHolder -> {
                                if (!nodeHolder.isConnected()) {
                                    commandSender.sendMessage("Node is not connected!");
                                    return;
                                }
                                commandSender.sendMessage("Pulling templates from node %hc" + nodeHolder.getName() + "...");
                                commandSender.sendMessage("This may take a while...");
                                NodeLauncher.getInstance().getServiceTemplateManager().pullTemplates(nodeHolder)
                                        .onFailure(throwable -> CloudAPI.getInstance().getConsole().error("§cError while pulling templates!", throwable))
                                        .onSuccess(s -> commandSender.sendMessage("Templates pulled from node %hc" + nodeHolder.getName()));
                            });
                });
    }

}
