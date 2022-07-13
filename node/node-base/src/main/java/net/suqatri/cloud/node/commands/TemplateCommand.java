package net.suqatri.cloud.node.commands;

import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.group.ICloudGroup;
import net.suqatri.cloud.api.node.ICloudNode;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.template.ICloudServiceTemplate;
import net.suqatri.cloud.commons.function.future.FutureAction;
import net.suqatri.cloud.commons.function.future.FutureActionCollection;
import net.suqatri.cloud.node.NodeLauncher;
import net.suqatri.cloud.node.file.FileTransferProcessThread;
import net.suqatri.commands.CommandHelp;
import net.suqatri.commands.CommandSender;
import net.suqatri.commands.ConsoleCommand;
import net.suqatri.commands.annotation.*;

import java.util.UUID;

@CommandAlias("template|templates")
public class TemplateCommand extends ConsoleCommand {

    /*
    /template list
    /template create <name>
    /template delete <name>
    /template info <name>
    /template push <name>
    /template pull <name> <Node> //TODO
     */

    @HelpCommand
    @Default
    @Subcommand("help")
    public void onHelp(CommandHelp commandHelp){
        commandHelp.showHelp();
    }

    @Subcommand("list")
    public void onList(CommandSender commandSender){
        commandSender.sendMessage("Listing templates...");
        NodeLauncher.getInstance().getServiceTemplateManager().getAllTemplatesAsync()
                .onFailure(throwable -> CloudAPI.getInstance().getConsole().error("Error while getting templates!", throwable))
                .onSuccess(templateHolders -> {
                    commandSender.sendMessage("");
                    commandSender.sendMessage("Templates: " + NodeLauncher.getInstance().getConsole().getHighlightColor() + templateHolders.size());
                    for (IRBucketHolder<ICloudServiceTemplate> templateHolder : templateHolders) {
                        commandSender.sendMessage(" §8- " + NodeLauncher.getInstance().getConsole().getHighlightColor() + templateHolder.get().getName());
                    }
                    commandSender.sendMessage("");
                });
    }

    @Subcommand("create")
    @Syntax("<Name>")
    public void onCreate(CommandSender commandSender, String name){
        commandSender.sendMessage("Creating template " + NodeLauncher.getInstance().getConsole().getHighlightColor() + name + "...");
        NodeLauncher.getInstance().getServiceTemplateManager().existsTemplateAsync(name)
            .onFailure(throwable -> CloudAPI.getInstance().getConsole().error("Error while checking template existence!", throwable))
            .onSuccess(exists -> {
                if (exists) {
                    commandSender.sendMessage("Template already exists!");
                } else {
                    NodeLauncher.getInstance().getServiceTemplateManager().createTemplateAsync(name)
                            .onFailure(throwable -> CloudAPI.getInstance().getConsole().error("Error while creating template!", throwable))
                            .onSuccess(templateHolder -> {
                                commandSender.sendMessage("Template created with name " + templateHolder.get().getName());
                                commandSender.sendMessage("Template folder: " + templateHolder.get().getTemplateFolder().getAbsolutePath());
                            });
                }
            });
    }

    @Subcommand("delete")
    @Syntax("<Template>")
    @CommandCompletion("@templates")
    public void onDelete(CommandSender commandSender, String name){
        commandSender.sendMessage("Deleting template " + NodeLauncher.getInstance().getConsole().getHighlightColor() + name + "...");
        NodeLauncher.getInstance().getServiceTemplateManager().existsTemplateAsync(name)
            .onFailure(throwable -> CloudAPI.getInstance().getConsole().error("Error while checking template existence!", throwable))
            .onSuccess(exists -> {
                if (exists) {
                    NodeLauncher.getInstance().getServiceTemplateManager().deleteTemplateAsync(name)
                            .onFailure(throwable -> CloudAPI.getInstance().getConsole().error("Error while deleting template!", throwable))
                            .onSuccess(s -> commandSender.sendMessage("Template deleted with name " + name));
                } else {
                    commandSender.sendMessage("Template does not exist!");
                }
            });
    }

    @Subcommand("info")
    @Syntax("[Template]")
    @CommandCompletion("@templates")
    public void onInfo(CommandSender commandSender, @Optional String name){
        commandSender.sendMessage("Loading information of template " + NodeLauncher.getInstance().getConsole().getHighlightColor() + name + "...");
        NodeLauncher.getInstance().getServiceTemplateManager().existsTemplateAsync(name)
            .onFailure(throwable -> CloudAPI.getInstance().getConsole().error("Error while checking template existence!", throwable))
            .onSuccess(exists -> {
                if (exists) {
                    NodeLauncher.getInstance().getServiceTemplateManager().getTemplateAsync(name)
                            .onFailure(throwable -> CloudAPI.getInstance().getConsole().error("Error while getting template!", throwable))
                            .onSuccess(templateHolder -> {
                                CloudAPI.getInstance().getGroupManager().getGroupsAsync()
                                        .onFailure(throwable -> CloudAPI.getInstance().getConsole().error("Error while getting groups!", throwable))
                                        .onSuccess(groupHolders -> {
                                            StringBuilder builder = new StringBuilder();
                                            for (IRBucketHolder<ICloudGroup> groupHolder : groupHolders) {
                                                if(!builder.toString().isEmpty()) builder.append("§7, ");
                                                builder.append(NodeLauncher.getInstance().getConsole().getHighlightColor() + groupHolder.get().getName());
                                            }
                                            commandSender.sendMessage("Template » " + NodeLauncher.getInstance().getConsole().getHighlightColor() + templateHolder.get().getName());
                                            commandSender.sendMessage("Template folder » " + NodeLauncher.getInstance().getConsole().getHighlightColor() + templateHolder.get().getTemplateFolder().getAbsolutePath());
                                            commandSender.sendMessage("Groups » " + NodeLauncher.getInstance().getConsole().getHighlightColor() + builder);
                                        });
                            });
                } else {
                    commandSender.sendMessage("Template does not exist!");
                }
            });
    }

    @Subcommand("push")
    @Syntax("<Node>")
    @CommandCompletion("@nodes")
    public void onPush(CommandSender commandSender, String nodeName){
        if(nodeName.equalsIgnoreCase(NodeLauncher.getInstance().getNode().getName())){
            commandSender.sendMessage("You can´t push from this node to itself!");
            return;
        }
        if(FileTransferProcessThread.getCurrentSentProcess() != null){
            commandSender.sendMessage("There is already a file transfer process running!");
            commandSender.sendMessage("Please wait until the current process is finished!");
            return;
        }
        CloudAPI.getInstance().getNodeManager().getNodeAsync(nodeName)
                .onFailure(throwable -> CloudAPI.getInstance().getConsole().error("Error while getting node!", throwable))
                .onSuccess(nodeHolder -> {
                    if(!nodeHolder.get().isConnected()){
                        commandSender.sendMessage("Node is not connected!");
                        return;
                    }
                    commandSender.sendMessage("Pushing template to node " + NodeLauncher.getInstance().getConsole().getHighlightColor() + nodeHolder.get().getName() + "...");
                    NodeLauncher.getInstance().getServiceTemplateManager().pushAllTemplates(nodeHolder)
                            .onFailure(throwable -> CloudAPI.getInstance().getConsole().error("Error while pushing templates!", throwable))
                            .onSuccess(s -> commandSender.sendMessage("Templates pushed to node " + nodeHolder.get().getName()));
                });
    }

    @Subcommand("pushall")
    public void onPushAll(CommandSender commandSender){
        if(!FileTransferProcessThread.getSentProcesses().isEmpty() || FileTransferProcessThread.getCurrentSentProcess() != null){
            commandSender.sendMessage("There is already a file transfer process running!");
            commandSender.sendMessage("Please wait until the current process is finished!");
            return;
        }
        commandSender.sendMessage("Pushing all templates...");
        CloudAPI.getInstance().getNodeManager().getNodesAsync()
                .onFailure(throwable -> CloudAPI.getInstance().getConsole().error("Error while getting nodes!", throwable))
                .onSuccess(nodeHolders -> {
                    FutureActionCollection<UUID, IRBucketHolder<ICloudNode>> futureActionCollection = new FutureActionCollection<>();
                    for (IRBucketHolder<ICloudNode> nodeHolder : nodeHolders) {
                        if(nodeHolder.get().getUniqueId().equals(NodeLauncher.getInstance().getNode().getUniqueId())) continue;
                        if(nodeHolder.get().isConnected()){
                            commandSender.sendMessage("Pushing templates to node " + NodeLauncher.getInstance().getConsole().getHighlightColor() + nodeHolder.get().getName() + "...");
                            FutureAction<IRBucketHolder<ICloudNode>> futureAction = NodeLauncher.getInstance().getServiceTemplateManager().pushAllTemplates(nodeHolder);
                            futureAction.whenComplete((s, throwable) -> {
                                if(throwable != null) commandSender.sendMessage("Error while pushing templates to node " + NodeLauncher.getInstance().getConsole().getHighlightColor() + nodeHolder.get().getName());
                                else commandSender.sendMessage("Templates pushed to node " + NodeLauncher.getInstance().getConsole().getHighlightColor() + nodeHolder.get().getName());
                            });
                            futureActionCollection.addToProcess(nodeHolder.get().getUniqueId(), futureAction);
                        }else{
                            commandSender.sendMessage("Node " + nodeHolder.get().getName() + " is not connected!");
                        }
                    }
                    futureActionCollection.process()
                            .onFailure(throwable -> CloudAPI.getInstance().getConsole().error("Error while pushing templates!", throwable))
                            .onSuccess(s -> commandSender.sendMessage("Templates pushed to all nodes!"));
                });
    }

}
